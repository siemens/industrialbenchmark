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
package com.siemens.industrialbenchmark.setpointgen;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import com.siemens.industrialbenchmark.externaldrivers.setpointgen.SetPointGenerator;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;

/**
 * This class tests whether the following functions work as expected:
 * 		- setState()
 * 		- setSeed()
 * 		- getSetPoint()
 * 		- getChangeRatePerStep()
 * 		- getCurrentSteps()
 * 		- getLastSequenceSteps()
 *
 * For this the following procedure is performed:
 *
 *  1) a random walk of 100000 random steps is taken (=> initialized several parameters)
 *  2) the internal parameters at t=100000 are memorized
 *  3) a setpoint trajectory is captured for M steps
 *  4) the parameters are reset to the parameters from (2)
 *  5) a test is performed that compares if the regenerated setpoint is
 *     identical to the setpoint at same timestep in (3)
 *
 * @author Michel Tokic
 */
public class TestSetPointRepeat {

	@Test
	public void testSetPointRepeat() throws IOException, PropertiesException, InstantiationException, IllegalAccessException {

		double setpointTrajectory[] = new double[100000];

		// instantiate objects
		Properties props = null;
		props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		SetPointGenerator setpointgen = new SetPointGenerator(props);

		// 1) do a few random steps
		for (int i=0; i<100000; i++) {
			setpointgen.step();
		}

		// 2) memorize setpointgen parameters before first step
		final double changeRate = setpointgen.getChangeRatePerStep();
		final double startSetPoint = setpointgen.getSetPoint();
		final int steps = setpointgen.getCurrentSteps();
		final int sequenceSteps = setpointgen.getLastSequenceSteps();

		// 3) perform M steps of a random walk
		setpointgen.setSeed(1234);
		for (int i=0; i<setpointTrajectory.length; i++) {
			setpointTrajectory[i] = setpointgen.step();
		}

		// 4) reset setpoint parameters
		setpointgen.setSeed(1234);
		setpointgen.setState(startSetPoint, steps, sequenceSteps, changeRate);
		double step;
		// 5) compare if setpoint is equivalent
		for (int i=0; i<setpointTrajectory.length; i++) {
			step = setpointgen.step();
			assertEquals (step, setpointTrajectory[i], 0.0001);
		}
	}

	@Test
	public void testStationarySetPoint() throws PropertiesException, IOException {

		final float stationarySetPoint = 53.23f;
		// instantiate objects
		Properties props = null;
		props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		props.setProperty("STATIONARY_SETPOINT", "" +stationarySetPoint);
		SetPointGenerator setpointgen = new SetPointGenerator(props);

		for (int i=0; i<100000; i++) {
			assertEquals (setpointgen.step(), stationarySetPoint, 0.0001);
		}
	}
}

