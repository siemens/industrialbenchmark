/*
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
package com.siemens.industrialbenchmark.externaldrivers.setpointgen;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.google.common.base.Preconditions;
import com.siemens.industrialbenchmark.datavector.DataVectorImpl;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.industrialbenchmark.util.PlotCurve;
import com.siemens.rl.interfaces.DataVector;
import com.siemens.rl.interfaces.ExternalDriver;
import java.util.ArrayList;
import java.util.List;

/**
 * The seedable setpoint generator.
 *
 * @author Siegmund Duell, Michel Tokic
 */
public class SetPointGenerator implements ExternalDriver {

	private final float setPointStepSize;
	private final float maxChangeRatePerStepSetPoint;
	private final int maxSequenceLength;
	private final float minSetPoint;
	private final float maxSetPoint;
	private int currentSteps;
	private int lastSequenceSteps;
	private double changeRatePerStep;
	private boolean stationary;
	private double setPoint;
	private final RandomDataGenerator random;

	/**
	 * Constructor with given seed and properties file
	 * @param seed The seed for the random number generator
	 * @param aProperties The properties file to parse
	 * @throws PropertiesException if property values are badly formatted
	 */
	public SetPointGenerator(final long seed, final Properties aProperties)
			throws PropertiesException
	{
		this.stationary = aProperties.containsKey("STATIONARY_SETPOINT");
		if (stationary) {
			this.setPoint = PropertiesUtil.getFloat(aProperties, "STATIONARY_SETPOINT", true);
			Preconditions.checkArgument(setPoint >= 0.0f && setPoint <= 100.0f, "setpoint must be in range [0, 100]");
		}
		this.maxChangeRatePerStepSetPoint = PropertiesUtil.getFloat(aProperties, "MAX_CHANGE_RATE_PER_STEP_SETPOINT", true);
		this.maxSequenceLength = PropertiesUtil.getInt(aProperties, "MAX_SEQUENCE_LENGTH", true);
		this.minSetPoint = PropertiesUtil.getFloat(aProperties, "SetPoint_MIN", true);
		this.maxSetPoint = PropertiesUtil.getFloat(aProperties, "SetPoint_MAX", true);
		this.setPointStepSize = PropertiesUtil.getFloat(aProperties, "SETPOINT_STEP_SIZE", true);

		this.random = new RandomDataGenerator();
		this.random.reSeed(seed);
		defineNewSequence();
	}

	/**
	 * Constructs a new object using the current time in milliseconds as a seed.
	 * @param aProperties The properties file to parse
	 * @throws PropertiesException if property values are badly formatted
	 */
	public SetPointGenerator(final Properties aProperties) throws PropertiesException {
		this(System.currentTimeMillis(), aProperties);
	}

	/**
	 * @return the current steps
	 */
	public int getCurrentSteps() {
		return this.currentSteps;
	}

	/**
	 * @return the change rate per step
	 */
	public double getChangeRatePerStep() {
		return this.changeRatePerStep;
	}

	/**
	 * Sets the current state of the setpoint generation engine.
	 * @param setpoint see {@link #getSetPoint()}
	 * @param currentSteps see {@link #getCurrentSteps()}
	 * @param lastSequenceSteps see {@link #getLastSequenceSteps()}
	 * @param changeRatePerStep see {@link #getChangeRatePerStep()}
	 */
	public void setState(final double setpoint, final int currentSteps, final int lastSequenceSteps, final double changeRatePerStep) {
		this.setPoint = setpoint;
		this.currentSteps = currentSteps;
		this.lastSequenceSteps = lastSequenceSteps;
		this.changeRatePerStep = changeRatePerStep;
	}

	/**
	 * returns the current setpoint
	 * @return the last sequence steps
	 */
	public double getSetPoint() {
		return this.setPoint;
	}

	/**
	 * returns the last sequence steps
	 * @return the last sequence steps
	 */
	public int getLastSequenceSteps() {
		return this.lastSequenceSteps;
	}

	/**
	 * Returns the next setpoint and on the internal memorized old setpoint
	 * @return the next setpoint
	 */
	public double step() {
		final double newSetPoint = step(setPoint);
		setPoint = newSetPoint;
		return newSetPoint;
	}

	/**
	 * Returns the next setpoint
	 * @param aSetPoint
	 * @return
	 */
	private double step(final double aSetPoint) {
		if (stationary) {
			return aSetPoint;
		}

		if (currentSteps >= lastSequenceSteps) {
			defineNewSequence();
		}

		currentSteps++;
		double setpointLevel = aSetPoint + changeRatePerStep * setPointStepSize;

		if (setpointLevel > maxSetPoint) {
			setpointLevel = maxSetPoint;
			if (random.nextBinomial(1, 0.5) == 1) {
				changeRatePerStep *= (-1);
			}
		}
		if (setpointLevel < minSetPoint) {
			setpointLevel = minSetPoint;
			if (random.nextBinomial(1, 0.5) == 1) {
				changeRatePerStep *= (-1);
			}
		}

		assert setpointLevel <= maxSetPoint;
		return setpointLevel;
	}

	/**
	 * Defines a new setpoint trajectory sequence
	 */
	private void defineNewSequence() {
		//mLastSequenceSteps = mRandom.nextIntFromTo(0, MAX_SEQUENCE_LENGTH) + 1;
		lastSequenceSteps = random.nextInt(1, maxSequenceLength);
		currentSteps = 0;
		changeRatePerStep = random.nextUniform(0, 1) * maxChangeRatePerStepSetPoint;
		final double r = random.nextUniform(0, 1);
		if (r < 0.45f) {
			changeRatePerStep *= (-1);
		}
		if (r > 0.9f) {
			changeRatePerStep = 0;
		}
	}

	/**
	 * Plots a setpoint trajectory for 10000 points.
	 *
	 * @param args command-line arguments
	 * @throws IOException when there is an error reading the configuration file
	 * @throws PropertiesException if the configuration file is badly formatted
	 */
	public static void main(final String[] args) throws IOException, PropertiesException {

		final int episodeLength = 10000;
		final List<Double> data = new ArrayList<>(episodeLength);

		final Properties props = PropertiesUtil.loadSetPointProperties(new File("src/main/resources/sim.properties"));

		final SetPointGenerator lg = new SetPointGenerator(props);

		for (int si = 0; si < episodeLength; si++) {
			data.add(lg.step());
		}

		PlotCurve.plot("SetPoint Trajectory", "Time", "SetPoint [%]", data);
	}

	@Override
	public void setSeed(final long seed) {
		this.random.reSeed(seed);
	}

	@Override
	public void filter(final DataVector state) {
		state.setValue(SetPointGeneratorStateDescription.SET_POINT, step());
		state.setValue(SetPointGeneratorStateDescription.SET_POINT_CHANGE_RATE_PER_STEP, changeRatePerStep);
		state.setValue(SetPointGeneratorStateDescription.SET_POINT_CURRENT_STEPS, currentSteps);
		state.setValue(SetPointGeneratorStateDescription.SET_POINT_LAST_SEQUENCE_STEPS, lastSequenceSteps);
	}

	@Override
	public void setConfiguration(final DataVector state) {
		this.setPoint = state.getValue(SetPointGeneratorStateDescription.SET_POINT);
		this.changeRatePerStep = state.getValue(SetPointGeneratorStateDescription.SET_POINT_CHANGE_RATE_PER_STEP);
		this.currentSteps = state.getValue(SetPointGeneratorStateDescription.SET_POINT_CURRENT_STEPS).intValue();
		this.lastSequenceSteps = state.getValue(SetPointGeneratorStateDescription.SET_POINT_LAST_SEQUENCE_STEPS).intValue();
	}

	@Override
	public DataVector getState() {
		final DataVectorImpl s = new DataVectorImpl(new SetPointGeneratorStateDescription());
		s.setValue(SetPointGeneratorStateDescription.SET_POINT, setPoint);
		s.setValue(SetPointGeneratorStateDescription.SET_POINT_CHANGE_RATE_PER_STEP, changeRatePerStep);
		s.setValue(SetPointGeneratorStateDescription.SET_POINT_CURRENT_STEPS, currentSteps);
		s.setValue(SetPointGeneratorStateDescription.SET_POINT_LAST_SEQUENCE_STEPS, lastSequenceSteps);

		return s;
	}
}

