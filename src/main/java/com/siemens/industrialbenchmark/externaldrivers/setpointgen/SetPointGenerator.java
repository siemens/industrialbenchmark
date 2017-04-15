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

/**
 * The seedable setpoint generator
 *
 * @author Siegmund Duell, Michel Tokic
 *
 */
public class SetPointGenerator implements ExternalDriver {

	private final float SETPOINT_STEP_SIZE;
	private final float MAX_CHANGE_RATE_PER_STEP_SETPOINT;
	private final int MAX_SEQUENCE_LENGTH;
	private final float MINSETPOINT;
	private final float MAXSETPOINT;

	private int mCurrentSteps;
	private int mLastSequenceSteps;
	private double mChangeRatePerStep;
	private boolean mIsStationary;
	private double mSetPoint;

	private RandomDataGenerator mRandom = new RandomDataGenerator();


	/**
	 Constructor with given seed and properties file
	 * @param seed The seed for the random number generator
	 * @param aProperties The properties file to parse
	 * @throws PropertiesException
	 */
	public SetPointGenerator(long seed, Properties aProperties)
			throws PropertiesException {

		mIsStationary = aProperties.containsKey("STATIONARY_SETPOINT");
		if (mIsStationary) {
			mSetPoint = PropertiesUtil.getFloat(aProperties, "STATIONARY_SETPOINT", true);
			Preconditions.checkArgument(mSetPoint >= 0.0f && mSetPoint <= 100.0f, "setpoint must be in range [0, 100]");
		}
		MAX_CHANGE_RATE_PER_STEP_SETPOINT = PropertiesUtil.getFloat(aProperties, "MAX_CHANGE_RATE_PER_STEP_SETPOINT", true);
		MAX_SEQUENCE_LENGTH = PropertiesUtil.getInt(aProperties, "MAX_SEQUENCE_LENGTH", true);
		MINSETPOINT = PropertiesUtil.getFloat(aProperties, "SetPoint_MIN", true);
		MAXSETPOINT = PropertiesUtil.getFloat(aProperties, "SetPoint_MAX", true);
		SETPOINT_STEP_SIZE = PropertiesUtil.getFloat(aProperties, "SETPOINT_STEP_SIZE", true);

		this.mRandom = new RandomDataGenerator();
		this.mRandom.reSeed(seed);
		defineNewSequence();
	}


	/**
	 * returns the current steps
	 * @return the current steps
	 */
	public int getCurrentSteps() {
		return this.mCurrentSteps;
	}

	/**
	 * returns the change rate per step
	 * @return the change rate per step
	 */
	public double getChangeRatePerStep() {
		return this.mChangeRatePerStep;
	}

	/**
	 * sets the current state of the setpoint generation engine
	 * @param currentSteps
	 * @param lastSequenceSteps
	 * @param changeRatePerStep
	 */
	public void setState(double setpoint, int currentSteps, int lastSequenceSteps, double changeRatePerStep) {
		this.mSetPoint = setpoint;
		this.mCurrentSteps = currentSteps;
		this.mLastSequenceSteps = lastSequenceSteps;
		this.mChangeRatePerStep = changeRatePerStep;
	}

	/**
	 * returns the current setpoint
	 * @return the last sequence steps
	 */
	public double getSetPoint() {
		return this.mSetPoint;
	}

	/**
	 * returns the last sequence steps
	 * @return the last sequence steps
	 */
	public int getLastSequenceSteps() {
		return this.mLastSequenceSteps;
	}


	/**
	 * Default constructor with seed=System.currentTimeMillis()
	 * @param aProperties The properties file to parse
	 * @throws PropertiesException
	 */
	public SetPointGenerator(Properties aProperties) throws PropertiesException {
		this(System.currentTimeMillis(), aProperties);
	}


	/**
	 * Returns the next setpoint and on the internal memorized old setpoint
	 * @return the next setpoint
	 */
	public double step() {
		double newSetPoint = step(mSetPoint);
		mSetPoint = newSetPoint;
		return newSetPoint;
	}

	/**
	 * Returns the next setpoint
	 * @param aSetPoint
	 * @return
	 */
	private double step(double aSetPoint) {
		if (mIsStationary)
			return aSetPoint;

		double setpointLevel = aSetPoint;
		if (mCurrentSteps >= mLastSequenceSteps) {
			defineNewSequence();
		}

		mCurrentSteps++;
		setpointLevel += mChangeRatePerStep * SETPOINT_STEP_SIZE;

		if (setpointLevel > MAXSETPOINT) {
			setpointLevel = MAXSETPOINT;
			if (mRandom.nextBinomial(1, 0.5) == 1) {
				mChangeRatePerStep *= (-1);
			}
		}
		if (setpointLevel < MINSETPOINT) {
			setpointLevel = MINSETPOINT;
			if (mRandom.nextBinomial(1, 0.5) == 1) {
				mChangeRatePerStep *= (-1);
			}
		}

		assert setpointLevel <= MAXSETPOINT;
		return setpointLevel;
	}

	/**
	 * Defines a new setpoint trajectory sequence
	 */
	private void defineNewSequence() {
		//mLastSequenceSteps = mRandom.nextIntFromTo(0, MAX_SEQUENCE_LENGTH) + 1;
		mLastSequenceSteps = mRandom.nextInt(1, MAX_SEQUENCE_LENGTH);
		mCurrentSteps = 0;
		mChangeRatePerStep = mRandom.nextUniform(0, 1) * MAX_CHANGE_RATE_PER_STEP_SETPOINT;
		double r = mRandom.nextUniform(0, 1);
		if (r < 0.45f) {
			mChangeRatePerStep *= (-1);
		}
		if (r > 0.9f) {
			mChangeRatePerStep = 0;
		}
	}

	/**
	 * plots a setpoint trajectory for 10000 points
	 *
	 * @throws IOException
	 * @throws PropertiesException
	 */
	public static void main(String[] args) throws IOException, PropertiesException {

		final int episodeLength = 10000;
		double data[] = new double[episodeLength];

		Properties props = null;
		props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));

		SetPointGenerator lg = new SetPointGenerator(props);

		for (int i=0; i<episodeLength; i++) {
			data[i] = lg.step();
		}

		PlotCurve.plot("SetPoint Trajectory", "Time", "SetPoint [%]", data);
	}


	@Override
	public void setSeed(long seed) {
		this.mRandom.reSeed(seed);
	}

	@Override
	public void filter(DataVector state) {
		state.setValue(SetPointGeneratorStateDescription.SetPoint, this.step());
		state.setValue(SetPointGeneratorStateDescription.SetPointChangeRatePerStep, this.mChangeRatePerStep);
		state.setValue(SetPointGeneratorStateDescription.SetPointCurrentSteps, this.mCurrentSteps);
		state.setValue(SetPointGeneratorStateDescription.SetPointLastSequenceSteps, this.mLastSequenceSteps);
	}

	@Override
	public void setConfiguration(DataVector state) {
		this.mSetPoint = state.getValue(SetPointGeneratorStateDescription.SetPoint);
		this.mChangeRatePerStep = state.getValue(SetPointGeneratorStateDescription.SetPointChangeRatePerStep);
		this.mCurrentSteps = state.getValue(SetPointGeneratorStateDescription.SetPointCurrentSteps).intValue();
		this.mLastSequenceSteps = state.getValue(SetPointGeneratorStateDescription.SetPointLastSequenceSteps).intValue();
	}


	@Override
	public DataVector getState() {
		DataVectorImpl s = new DataVectorImpl(new SetPointGeneratorStateDescription());
		s.setValue(SetPointGeneratorStateDescription.SetPoint, mSetPoint);
		s.setValue(SetPointGeneratorStateDescription.SetPointChangeRatePerStep, mChangeRatePerStep);
		s.setValue(SetPointGeneratorStateDescription.SetPointCurrentSteps, mCurrentSteps);
		s.setValue(SetPointGeneratorStateDescription.SetPointLastSequenceSteps, mLastSequenceSteps);

		return s;
	}
}

