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
package com.siemens.industialbenchmark.dynamics.goldstone;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.base.Preconditions;
import com.siemens.industrialbenchmark.dynamics.goldstone.GoldstoneEnvironment;

class TestGoldstoneEnvironment {

	@Test
	void test() throws IOException {

		var classLoader = getClass().getClassLoader();
		var f = new File(classLoader.getResource("environment_class_regression_data.xa").getFile());

		var action = new ArrayList<Double>();
		var penalty = new ArrayList<Double>();

		try (var reader = new BufferedReader(new FileReader(f))) {
			while (reader.ready()) {
				var line = reader.readLine();
				if (!line.startsWith("#")) {
					var fields = line.split(" ");
					Preconditions.checkArgument(fields.length == 3,
							"three fields expected, but %s parsed.", fields.length);
					action.add(Double.parseDouble(fields[1]));
					penalty.add(Double.parseDouble(fields[2]));
				}
			}
		}

		System.out.println("Goldstone environment class regression test: read " + action.size() + " rows.");

		int numberOfSteps = 24;
		double maxRequiredStep = 0.25;
		double safeZone = 0.5 * maxRequiredStep;
		var env = new GoldstoneEnvironment(numberOfSteps, maxRequiredStep, safeZone);

		for (int step = 0; step < action.size(); step++) {
			var reward = env.stateTransition(action.get(step));
			if (Math.abs(env.getControlPosition()) <= 1.5) {
				assertEquals(-penalty.get(step), reward, 1e-8);
			}
		}
	}
}
