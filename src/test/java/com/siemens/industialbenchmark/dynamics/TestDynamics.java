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
package com.siemens.industialbenchmark.dynamics;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.junit.Test;

import com.siemens.industrialbenchmark.datavector.action.ActionDelta;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.datavector.state.ObservableState;
import com.siemens.industrialbenchmark.datavector.state.ObservableStateDescription;
import com.siemens.industrialbenchmark.dynamics.IndustrialBenchmarkDynamics;
import com.siemens.industrialbenchmark.externaldrivers.setpointgen.SetPointGenerator;
import com.siemens.industrialbenchmark.externaldrivers.setpointgen.SetPointGeneratorStateDescription;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.rl.interfaces.DataVector;
import com.siemens.rl.interfaces.ExternalDriver;

/**
 * This is a test class for the Dynamics.
 *
 * @author Michel Tokic
 */
public class TestDynamics {

	final int INIT_STEPS = 10000;
	final int MEM_STEPS = 10000;
	final long ACTION_SEED = 12345;

	/**
	 * This class tests that dynamics are repeatable, which is required by Particle-Swarm-Optimization.
	 *
	 *  1) 100000 steps are taken => initializes dynamics with a random trajectory
	 *  2) observable and markov state are memorized
	 *  3) a random trajectory is performed and memorized
	 *  4) reset of states from (2) and call Environment.reset()
	 *  5) test if replay produces the same dynamics
	 *
	 * @throws IOException
	 * @throws PropertiesException
	 */
	@Test
	public void testRepeatibleDynamics() throws IOException, PropertiesException {


		// INSTANTIATE benchmark
		Properties props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		SetPointGenerator lg = new SetPointGenerator(props);
		List<ExternalDriver> externalDrivers = new ArrayList<ExternalDriver>();
		externalDrivers.add(lg);
		IndustrialBenchmarkDynamics d = new IndustrialBenchmarkDynamics(props, externalDrivers);
		Random actionRand = new Random(System.currentTimeMillis());

        // 1) do 100000 random steps, in order to initialize dynamics
		final ActionDelta action = new ActionDelta(0.001f, 0.001f, 0.001f);
		for (int i=0; i<INIT_STEPS; i++) {
			action.setDeltaGain(2.f*(actionRand.nextFloat()-0.5f));
			action.setDeltaVelocity(2.f*(actionRand.nextFloat()-0.5f));
			action.setDeltaShift(2.f*(actionRand.nextFloat()-0.5f));
			d.step(action);
		}

		// 2) memorize current observable state and current markov state
		final ObservableState os = d.getState();
		final DataVector ms = d.getInternalMarkovState();
		System.out.println("init o-state: " + os.toString());
		System.out.println("init m-state: " + ms.toString());


		// 3) perform test trajectory and memorize states
		actionRand.setSeed(ACTION_SEED);
		DataVector oStates[] = new DataVector[MEM_STEPS];
		DataVector mStates[] = new DataVector[MEM_STEPS];

		for (int i=0; i<MEM_STEPS; i++) {
			action.setDeltaGain(2.f*(actionRand.nextFloat()-0.5f));
			action.setDeltaVelocity(2.f*(actionRand.nextFloat()-0.5f));
			action.setDeltaShift(2.f*(actionRand.nextFloat()-0.5f));
			d.step(action);
			oStates[i] = d.getState();
			mStates[i] = d.getInternalMarkovState();
		}

		// 4) reset dynamics & parameters and internal markov state
		d.reset();
		d.setInternalMarkovState(ms);

		// 5) reperform test and check if values are consistent
		actionRand.setSeed(ACTION_SEED); // reproduce action sequence
		DataVector oState = null;
		DataVector mState = null;
		for (int i=0; i<MEM_STEPS; i++) {
			action.setDeltaGain(2.f*(actionRand.nextFloat()-0.5f));
			action.setDeltaVelocity(2.f*(actionRand.nextFloat()-0.5f));
			action.setDeltaShift(2.f*(actionRand.nextFloat()-0.5f));

			d.step(action);
			oState = d.getState();
			mState = d.getInternalMarkovState();

			// check observable state
			assertEquals(oStates[i].getValue(ObservableStateDescription.SetPoint), oState.getValue(ObservableStateDescription.SetPoint), 0.0001);
			assertEquals(oStates[i].getValue(ObservableStateDescription.Fatigue), oState.getValue(ObservableStateDescription.Fatigue), 0.0001);
			assertEquals(oStates[i].getValue(ObservableStateDescription.Consumption), oState.getValue(ObservableStateDescription.Consumption), 0.0001);
			assertEquals(oStates[i].getValue(ObservableStateDescription.RewardTotal), oState.getValue(ObservableStateDescription.RewardTotal), 0.0001);

			//
			assertEquals(mStates[i].getValue(MarkovianStateDescription.CurrentOperationalCost), mState.getValue(MarkovianStateDescription.CurrentOperationalCost), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.FatigueLatent2), mState.getValue(MarkovianStateDescription.FatigueLatent2), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.FatigueLatent1), mState.getValue(MarkovianStateDescription.FatigueLatent1), 0.0001);

			assertEquals(mStates[i].getValue(MarkovianStateDescription.EffectiveActionGainBeta), mState.getValue(MarkovianStateDescription.EffectiveActionGainBeta), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.EffectiveActionVelocityAlpha), mState.getValue(MarkovianStateDescription.EffectiveActionVelocityAlpha), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.EffectiveShift), mState.getValue(MarkovianStateDescription.EffectiveShift), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.MisCalibration), mState.getValue(MarkovianStateDescription.MisCalibration), 0.0001);

			assertEquals(mStates[i].getValue(SetPointGeneratorStateDescription.SetPointChangeRatePerStep), mState.getValue(SetPointGeneratorStateDescription.SetPointChangeRatePerStep), 0.0001);
			assertEquals(mStates[i].getValue(SetPointGeneratorStateDescription.SetPointCurrentSteps), mState.getValue(SetPointGeneratorStateDescription.SetPointCurrentSteps), 0.0001);
			assertEquals(mStates[i].getValue(SetPointGeneratorStateDescription.SetPointLastSequenceSteps), mState.getValue(SetPointGeneratorStateDescription.SetPointLastSequenceSteps), 0.0001);

			assertEquals(mStates[i].getValue(MarkovianStateDescription.RewardFatigue), mState.getValue(MarkovianStateDescription.RewardFatigue), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.RewardConsumption), mState.getValue(MarkovianStateDescription.RewardConsumption), 0.0001);
		}

		System.out.println("last o-state 1st trajectory: " + oStates[oStates.length-1]);
		System.out.println("last o-state 2nd trajectory: " + oState);

		System.out.println("last m-state 1st trajectory: " + mStates[oStates.length-1]);
		System.out.println("last m-state 2nd trajectory: " + mState);
	}

	@Test
	public void testHistoryLength() throws IOException, PropertiesException {

		// INSTANTIATE benchmark
		Properties props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		SetPointGenerator lg = new SetPointGenerator(props);
		List<ExternalDriver> externalDrivers = new ArrayList<ExternalDriver>();
		externalDrivers.add(lg);
		IndustrialBenchmarkDynamics d = new IndustrialBenchmarkDynamics(props, externalDrivers);

		int expHistSize = 0;
		for (String key : d.getInternalMarkovState().getKeys()) {
			if (key.startsWith("OPERATIONALCOST_")) {
				expHistSize++;
			}
		}

		assertEquals(expHistSize, d.getOperationalCostsHistoryLength());
	}
}

