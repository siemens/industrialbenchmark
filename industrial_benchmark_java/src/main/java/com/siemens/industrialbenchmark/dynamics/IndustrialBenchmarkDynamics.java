/**
Copyright 2016 Siemens AG.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.siemens.industrialbenchmark.dynamics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.siemens.industrialbenchmark.datavector.action.ActionAbsolute;
import com.siemens.industrialbenchmark.datavector.action.ActionDelta;
import com.siemens.industrialbenchmark.datavector.action.EffectiveAction;
import com.siemens.industrialbenchmark.datavector.state.MarkovianState;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.datavector.state.ObservableState;
import com.siemens.industrialbenchmark.datavector.state.ObservableStateDescription;
import com.siemens.industrialbenchmark.dynamics.goldstone.GoldstoneEnvironment;
import com.siemens.industrialbenchmark.externaldrivers.setpointgen.SetPointGenerator;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.rl.interfaces.DataVector;
import com.siemens.rl.interfaces.Environment;
import com.siemens.rl.interfaces.ExternalDriver;

/**
 * Basic dynamics of the industrial benchmark.
 *
 * <p>The following steps are performed each tick:
 * <ul>
 *   <li>Update setpoint (random walk via {@link SetPointGenerator})</li>
 *   <li>Apply actions to the state</li>
 *   <li>Compute operational costs</li>
 *   <li>Delay and convolve operational cost levels</li>
 * </ul>
 *
 * @author Siegmund Duell, Alexander Hentschel, Michel Tokic
 */
public class IndustrialBenchmarkDynamics implements Environment {

    private static final Logger logger = LoggerFactory.getLogger(IndustrialBenchmarkDynamics.class);

    protected final float stepSizeVelocity;
    protected final float stepSizeGain;

    /** Ring buffer of fixed size implementing a FIFO queue (replaces legacy CircularFifoBuffer). */
    protected final ArrayDeque<Double> operationalCostsBuffer = new ArrayDeque<>();
    private int operationalCostsBufferCapacity;

    private float[] convWeights;
    private boolean convToInit = true;

    private GoldstoneEnvironment gsEnvironment;
    private final float maxRequiredStep = (float) Math.sin(15.0f / 180.0f * Math.PI);
    private final float gsBound = 1.5f;
    private final float gsSetPointDependency = 0.02f;

    private enum C {
        DGain, DVelocity, DSetPoint,
        CostSetPoint, CostGain, CostVelocity, DBase,
        STEP_SIZE_GAIN, STEP_SIZE_VELOCITY
    }

    protected MarkovianState markovState;
    protected MarkovianState max;
    protected MarkovianState min;
    protected final Properties properties;

    private IndustrialBenchmarkRewardFunction rewardCore;
    private RandomDataGenerator rda = new RandomDataGenerator();
    private long randomSeed = 0;
    private float crgs;

    private List<String> markovStateAdditionalNames;
    private List<ExternalDriver> externalDrivers = new ArrayList<>();
    private final ActionDelta zeroAction = new ActionDelta(0, 0, 0);

    /**
     * Constructor with configuration Properties.
     *
     * @param properties the properties object
     * @throws PropertiesException if required properties are missing
     */
    public IndustrialBenchmarkDynamics(Properties properties) throws PropertiesException {
        this.properties = properties;
        this.rewardCore = new IndustrialBenchmarkRewardFunction(properties);
        this.stepSizeGain = PropertiesUtil.getFloat(properties, C.STEP_SIZE_GAIN.name(), true);
        this.stepSizeVelocity = PropertiesUtil.getFloat(properties, C.STEP_SIZE_VELOCITY.name(), true);

        externalDrivers.add(new SetPointGenerator(properties));

        init();
        step(zeroAction);
    }

    /**
     * Constructor with configuration Properties and external driver list.
     *
     * @param properties      the properties object
     * @param externalDrivers the list containing external drivers
     * @throws PropertiesException if required properties are missing
     */
    public IndustrialBenchmarkDynamics(Properties properties, List<ExternalDriver> externalDrivers)
            throws PropertiesException {
        this(properties);
        this.externalDrivers = externalDrivers;
        init();
        step(zeroAction);
    }

    /**
     * Initialize the industrial benchmark.
     */
    protected void init() throws PropertiesException {

        // configure convolution variables
        crgs = PropertiesUtil.getFloat(properties, "CRGS", true);
        convWeights = getFloatArray(properties.getProperty("ConvArray"));
        operationalCostsBufferCapacity = convWeights.length;
        markovStateAdditionalNames = new ArrayList<>();

        operationalCostsBuffer.clear();
        IntStream.range(0, convWeights.length).forEach(i -> {
            operationalCostsBuffer.add(0.0);
            markovStateAdditionalNames.add("OPERATIONALCOST_" + i);
        });

        markovStateAdditionalNames.addAll(MarkovianStateDescription.getNonConvolutedInternalVariables());

        // add variables from external drivers
        var extNames = new ArrayList<String>();
        for (var driver : externalDrivers) {
            for (var name : driver.getState().getKeys()) {
                if (!extNames.contains(name)) {
                    extNames.add(name);
                }
            }
        }
        markovStateAdditionalNames.addAll(extNames);

        // instantiate markov state with additional convolution variable names
        markovState = new MarkovianState(markovStateAdditionalNames);
        min = new MarkovianState(markovStateAdditionalNames);
        max = new MarkovianState(markovStateAdditionalNames);

        // extract variable bounds + initial values from Properties
        for (var v : markovState.getKeys()) {
            var initVal = PropertiesUtil.getFloat(properties, v + "_INIT", 0);
            var maxVal = PropertiesUtil.getFloat(properties, v + "_MAX", Float.MAX_VALUE);
            var minVal = PropertiesUtil.getFloat(properties, v + "_MIN", -Float.MAX_VALUE);
            Preconditions.checkArgument(maxVal > minVal,
                    "variable=%s: max=%s must be > than min=%s", v, maxVal, minVal);
            Preconditions.checkArgument(initVal >= minVal && initVal <= maxVal,
                    "variable=%s: init=%s must be between min=%s and max=%s", v, initVal, minVal, maxVal);
            max.setValue(v, maxVal);
            min.setValue(v, minVal);
            markovState.setValue(v, initVal);
        }

        // seed all random number generators for reproducibility
        randomSeed = PropertiesUtil.getLong(properties, "SEED", System.currentTimeMillis());
        logger.debug("init seed: {}", randomSeed);
        rda.reSeed(randomSeed);

        for (var driver : externalDrivers) {
            driver.setSeed(rda.nextLong(0, Long.MAX_VALUE));
            driver.filter(markovState);
        }

        this.gsEnvironment = new GoldstoneEnvironment(24, maxRequiredStep, maxRequiredStep / 2.0);

        // set all NaN values to 0.0
        for (var key : markovState.getKeys()) {
            if (Double.isNaN(markovState.getValue(key))) {
                markovState.setValue(key, 0.0);
            }
        }
    }

    /**
     * Converts a float array represented as a string (e.g. "0.01, 0.2, 0.9") to a Java float[].
     */
    private float[] getFloatArray(String floatArrayAsString) {
        var components = floatArrayAsString.replaceAll("( |\t|\n)", "");
        var split = components.split(",");
        var result = new float[split.length];
        for (var i = 0; i < result.length; i++) {
            result[i] = Float.parseFloat(split[i]);
        }
        return result;
    }

    /**
     * Returns the observable components from the markovian state.
     */
    public ObservableState getState() {
        var state = new ObservableState();
        for (var key : state.getKeys()) {
            state.setValue(key, this.markovState.getValue(key));
        }
        return state;
    }

    /**
     * Applies an action to the industrial benchmark.
     *
     * @param action the action to apply
     * @return the reward of the successor state
     */
    @Override
    public double step(DataVector action) {

        // apply randomSeed to PRNGs and external drivers
        this.rda.reSeed(randomSeed);
        for (var driver : externalDrivers) {
            driver.setSeed(rda.nextLong(0, Long.MAX_VALUE));
            driver.filter(this.markovState);
        }

        // add actions to state
        addAction((ActionDelta) action);

        try {
            updateFatigue();
            updateCurrentOperationalCost();
        } catch (PropertiesException e) {
            e.printStackTrace();
        }

        updateOperationalCostConvolution();
        updateGS();
        updateOperationalCosts();

        // update reward
        rewardCore.calcReward(markovState);

        // set random seed for next iteration
        this.randomSeed = rda.nextLong(0, Long.MAX_VALUE);
        this.markovState.setValue(MarkovianStateDescription.RandomSeed,
                Double.longBitsToDouble(this.randomSeed));

        return this.markovState.getValue(ObservableStateDescription.RewardTotal);
    }

    private void updateOperationalCosts() {
        var rGS = markovState.getValue(MarkovianStateDescription.MisCalibration);
        var eNewHidden = markovState.getValue(MarkovianStateDescription.OperationalCostsConv)
                - (crgs * (rGS - 1.0));
        var operationalcosts = eNewHidden - rda.nextGaussian(0, 1) * (1 + 0.005 * eNewHidden);
        markovState.setValue(MarkovianStateDescription.Consumption, operationalcosts);
    }

    private void addAction(ActionDelta action) {

        var velocityMax = max.getValue(MarkovianStateDescription.Action_Velocity);
        var velocityMin = min.getValue(MarkovianStateDescription.Action_Velocity);
        var velocity = Math.min(velocityMax, Math.max(velocityMin,
                markovState.getValue(MarkovianStateDescription.Action_Velocity)
                        + action.getDeltaVelocity() * stepSizeVelocity));

        if (action instanceof ActionAbsolute abs) {
            var diff = Math.max(-stepSizeVelocity,
                    Math.min(stepSizeVelocity,
                            abs.getVelocity() - markovState.getValue(MarkovianStateDescription.Action_Velocity)));
            velocity = Math.min(velocityMax, Math.max(velocityMin,
                    markovState.getValue(MarkovianStateDescription.Action_Velocity) + diff));
        }

        var gainMax = max.getValue(MarkovianStateDescription.Action_Gain);
        var gainMin = min.getValue(MarkovianStateDescription.Action_Gain);
        var gain = Math.min(gainMax, Math.max(gainMin,
                markovState.getValue(MarkovianStateDescription.Action_Gain)
                        + action.getDeltaGain() * stepSizeGain));

        if (action instanceof ActionAbsolute abs) {
            var diff = Math.max(-stepSizeGain,
                    Math.min(stepSizeGain,
                            abs.getGain() - markovState.getValue(MarkovianStateDescription.Action_Gain)));
            gain = Math.min(gainMax, Math.max(gainMin,
                    markovState.getValue(MarkovianStateDescription.Action_Gain) + diff));
        }

        final var gsScale = 2.0f * gsBound + 100.0f * gsSetPointDependency;
        var shift = Math.min(100.0f, Math.max(0.0f,
                markovState.getValue(MarkovianStateDescription.Action_Shift)
                        + action.getDeltaShift() * (maxRequiredStep / 0.9f) * 100.0f / gsScale));

        if (action instanceof ActionAbsolute abs) {
            var diff = Math.max(-((maxRequiredStep / 0.9f) * 100.0f / gsScale),
                    Math.min((maxRequiredStep / 0.9f) * 100.0f / gsScale,
                            abs.getShift() - markovState.getValue(MarkovianStateDescription.Action_Shift)));
            shift = Math.min(100.0f, Math.max(0.0f,
                    markovState.getValue(MarkovianStateDescription.Action_Shift) + diff));
        }

        var hiddenShift = Math.min(gsBound, Math.max(-gsBound,
                gsScale * shift / 100.0f
                        - gsSetPointDependency * markovState.getValue(MarkovianStateDescription.SetPoint)
                        - gsBound));

        markovState.setValue(MarkovianStateDescription.Action_Velocity, velocity);
        markovState.setValue(MarkovianStateDescription.Action_Gain, gain);
        markovState.setValue(MarkovianStateDescription.Action_Shift, shift);
        markovState.setValue(MarkovianStateDescription.EffectiveShift, hiddenShift);
    }

    /**
     * Updates the spiking fatigue dynamics.
     */
    private void updateFatigue() throws PropertiesException {
        final var expLambda = 0.1f;
        final var actionTolerance = 0.05f;
        final var fatigueAmplification = 1.1f;
        final var fatigueAmplificationMax = 5.0f;
        final var fatigueAmplificationStart = 1.2f;

        var velocity = markovState.getValue(MarkovianStateDescription.Action_Velocity);
        var gain = markovState.getValue(MarkovianStateDescription.Action_Gain);
        var setpoint = markovState.getValue(MarkovianStateDescription.SetPoint);

        var hiddenStateVelocity = markovState.getValue(MarkovianStateDescription.FatigueLatent1);
        var hiddenStateGain = markovState.getValue(MarkovianStateDescription.FatigueLatent2);

        var effAction = new EffectiveAction(
                new ActionAbsolute(velocity, gain, 0.0, this.properties), setpoint);
        var effActionVelocity = effAction.getEffectiveVelocity();
        var effActionGain = effAction.getEffectiveGain();

        // base noise
        var noiseGain = 2.0 * (1.0 / (1.0 + Math.exp(-rda.nextExponential(expLambda))) - 0.5);
        var noiseVelocity = 2.0 * (1.0 / (1.0 + Math.exp(-rda.nextExponential(expLambda))) - 0.5);

        // add spikes (keep within [0.001, 0.999] for Binomial)
        noiseGain += (1 - noiseGain) * rda.nextUniform(0, 1)
                * rda.nextBinomial(1, Math.min(Math.max(0.001, effActionGain), 0.999)) * effActionGain;
        noiseVelocity += (1 - noiseVelocity) * rda.nextUniform(0, 1)
                * rda.nextBinomial(1, Math.min(Math.max(0.001, effActionVelocity), 0.999)) * effActionVelocity;

        // compute internal dynamics
        if (effActionVelocity <= actionTolerance) {
            hiddenStateVelocity = effActionVelocity;
        } else if (hiddenStateGain >= fatigueAmplificationStart) {
            hiddenStateGain = Math.min(fatigueAmplificationMax, hiddenStateGain * fatigueAmplification);
        } else {
            hiddenStateGain = (hiddenStateGain * 0.9f) + ((float) noiseGain / 3.0f);
        }

        if (effActionGain <= actionTolerance) {
            hiddenStateGain = effActionGain;
        } else if (hiddenStateVelocity >= fatigueAmplificationStart) {
            hiddenStateVelocity = Math.min(fatigueAmplificationMax, hiddenStateVelocity * fatigueAmplification);
        } else {
            hiddenStateVelocity = (hiddenStateVelocity * 0.9f) + ((float) noiseVelocity / 3.0f);
        }

        double alpha;
        if (Math.max(hiddenStateVelocity, hiddenStateGain) == fatigueAmplificationMax) {
            alpha = 1.0 / (1.0 + Math.exp(-rda.nextGaussian(2.4, 0.4)));
        } else {
            alpha = Math.max(noiseGain, noiseVelocity);
        }

        final var cDGain = getConst(C.DGain);
        final var cDVelocity = getConst(C.DVelocity);
        final var cDSetPoint = getConst(C.DSetPoint);
        final var cDynBase = getConst(C.DBase);

        var fb = (cDynBase / ((cDVelocity * velocity) + cDSetPoint)) - cDGain * gain * gain;
        if (fb < 0) fb = 0;
        var f = ((2.0 * alpha + 1.0) * fb) / 3.0;

        markovState.setValue(MarkovianStateDescription.Fatigue, f);
        markovState.setValue(MarkovianStateDescription.FatigueBase, fb);
        markovState.setValue(MarkovianStateDescription.FatigueLatent1, hiddenStateVelocity);
        markovState.setValue(MarkovianStateDescription.FatigueLatent2, hiddenStateGain);
        markovState.setValue(MarkovianStateDescription.EffectiveActionVelocityAlpha, effActionVelocity);
        markovState.setValue(MarkovianStateDescription.EffectiveActionGainBeta, effActionGain);
    }

    private void updateCurrentOperationalCost() throws PropertiesException {

        final var costSetPoint = getConst(C.CostSetPoint);
        final var costGain = getConst(C.CostGain);
        final var costVelocity = getConst(C.CostVelocity);
        var setpoint = markovState.getValue(MarkovianStateDescription.SetPoint);
        var gain = markovState.getValue(MarkovianStateDescription.Action_Gain);
        var velocity = markovState.getValue(MarkovianStateDescription.Action_Velocity);
        var costs = costSetPoint * setpoint + costGain * gain + costVelocity * velocity;

        var operationalcosts = Math.exp(costs / 100.0);
        markovState.setValue(MarkovianStateDescription.CurrentOperationalCost, operationalcosts);

        // Maintain fixed-size FIFO behavior with ArrayDeque
        if (operationalCostsBuffer.size() >= operationalCostsBufferCapacity) {
            operationalCostsBuffer.pollFirst();
        }
        operationalCostsBuffer.addLast(operationalcosts);

        if (convToInit) {
            while (operationalCostsBuffer.size() < operationalCostsBufferCapacity) {
                operationalCostsBuffer.addLast(operationalcosts);
            }
            convToInit = false;
        }
    }

    private void updateOperationalCostConvolution() {
        var aggregatedCosts = 0.0;
        var i = 0;
        for (var cost : operationalCostsBuffer) {
            aggregatedCosts += convWeights[i] * cost;
            markovState.setValue("OPERATIONALCOST_" + i, cost);
            i++;
        }
        markovState.setValue(MarkovianStateDescription.OperationalCostsConv, aggregatedCosts);
    }

    private void updateGS() {
        gsEnvironment.setControlPosition(markovState.getValue(MarkovianStateDescription.EffectiveShift));
        markovState.setValue(MarkovianStateDescription.MisCalibration, (float) gsEnvironment.reward());
        markovState.setValue(MarkovianStateDescription.MisCalibrationDomain, gsEnvironment.getDomain());
        markovState.setValue(MarkovianStateDescription.MisCalibrationSystemResponse, gsEnvironment.getSystemResponse());
        markovState.setValue(MarkovianStateDescription.MisCalibrationPhiIdx, gsEnvironment.getPhiIdx());
    }

    private float getConst(C constant) throws PropertiesException {
        return PropertiesUtil.getFloat(properties, constant.name());
    }

    /**
     * Returns the operational costs history length.
     */
    public int getOperationalCostsHistoryLength() {
        return operationalCostsBuffer.size();
    }

    /**
     * Returns a copy of the current markovian state.
     */
    public DataVector getInternalMarkovState() {
        return this.markovState.clone();
    }

    /**
     * Sets the current markovian state.
     * Also updates the setpoint generator and reconvolves operational costs.
     */
    public void setInternalMarkovState(DataVector markovState) {

        // 1) import all key/value pairs
        for (var key : markovState.getKeys()) {
            this.markovState.setValue(key, markovState.getValue(key));
        }

        // 2) set random number generator states
        this.randomSeed = Double.doubleToLongBits(
                this.markovState.getValue(MarkovianStateDescription.RandomSeed));

        this.gsEnvironment.setControlPosition(markovState.getValue(MarkovianStateDescription.EffectiveShift));
        this.gsEnvironment.setDomain(markovState.getValue(MarkovianStateDescription.MisCalibrationDomain));
        this.gsEnvironment.setSystemResponse(markovState.getValue(MarkovianStateDescription.MisCalibrationSystemResponse));
        this.gsEnvironment.setPhiIdx(markovState.getValue(MarkovianStateDescription.MisCalibrationPhiIdx));

        // 3) reconstruct operational cost convolution + reward computation
        var aggregatedCosts = 0.0;
        operationalCostsBuffer.clear();
        for (var i = 0; i < convWeights.length; i++) {
            var key = "OPERATIONALCOST_" + i;
            var cost = markovState.getValue(key);
            aggregatedCosts += cost * convWeights[i];
            operationalCostsBuffer.addLast(cost);
        }
        markovState.setValue(MarkovianStateDescription.OperationalCostsConv, aggregatedCosts);
        rewardCore.calcReward(markovState);

        // 4) set state variables to external drivers
        for (var driver : externalDrivers) {
            driver.setConfiguration(markovState);
        }
    }

    @Override
    public void reset() {
        try {
            this.init();
        } catch (PropertiesException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getReward() {
        return this.markovState.getValue(ObservableStateDescription.RewardTotal);
    }
}
