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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.siemens.industrialbenchmark.externaldrivers.setpointgen.SetPointGenerator;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;

/**
 * Tests setpoint generator repeatability: setState(), setSeed(), getSetPoint(),
 * getChangeRatePerStep(), getCurrentSteps(), getLastSequenceSteps().
 *
 * @author Michel Tokic
 */
class TestSetPointRepeat {

	@Test
	void testSetPointRepeat() throws IOException, PropertiesException {

		var setpointTrajectory = new double[100000];

		var props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		var setpointgen = new SetPointGenerator(props);

		// 1) warm-up random walk
		for (int i = 0; i < 100000; i++) {
			setpointgen.step();
		}

		// 2) memorize parameters
		final var changeRate = setpointgen.getChangeRatePerStep();
		final var startSetPoint = setpointgen.getSetPoint();
		final var steps = setpointgen.getCurrentSteps();
		final var sequenceSteps = setpointgen.getLastSequenceSteps();

		// 3) capture trajectory
		setpointgen.setSeed(1234);
		for (int i = 0; i < setpointTrajectory.length; i++) {
			setpointTrajectory[i] = setpointgen.step();
		}

		// 4) reset and replay
		setpointgen.setSeed(1234);
		setpointgen.setState(startSetPoint, steps, sequenceSteps, changeRate);

		// 5) verify identical trajectory
		for (int i = 0; i < setpointTrajectory.length; i++) {
			assertEquals(setpointgen.step(), setpointTrajectory[i], 0.0001);
		}
	}

	@Test
	void testStationarySetPoint() throws PropertiesException, IOException {

		final float stationarySetPoint = 53.23f;
		var props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		props.setProperty("STATIONARY_SETPOINT", "" + stationarySetPoint);
		var setpointgen = new SetPointGenerator(props);

		for (int i = 0; i < 100000; i++) {
			assertEquals(setpointgen.step(), stationarySetPoint, 0.0001);
		}
	}
}
