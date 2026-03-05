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

import com.siemens.industrialbenchmark.dynamics.goldstone.PenaltyFunction;

class TestGoldstoneReward {

	@Test
	void rewardRegressionTest() throws IOException {

		var classLoader = getClass().getClassLoader();
		var f = new File(classLoader.getResource("reward_function_regression_data.xa").getFile());

		var data = new ArrayList<ArrayList<Double>>();
		var posIdx = new ArrayList<Double>();
		var header = new ArrayList<Double>();

		int columns = 0;
		int rows = 0;

		try (var reader = new BufferedReader(new FileReader(f))) {
			while (reader.ready()) {
				var line = reader.readLine();

				if (line.startsWith("#\t") || line.startsWith("# ")) {
					// comment line — skip
				} else if (line.startsWith("#!")) {
					line = line.replaceAll("#!\\t", "");
					var fields = line.split(" ");
					for (int i = 1; i < fields.length; i++) {
						header.add(Double.parseDouble(fields[i]));
						data.add(new ArrayList<>());
						columns++;
					}
				} else {
					var fields = line.split(" ");
					posIdx.add(Double.parseDouble(fields[0]));
					for (int i = 1; i < fields.length; i++) {
						data.get(i - 1).add(Double.parseDouble(fields[i]));
					}
					rows++;
				}
			}
		}

		System.out.println("Goldstone reward regression test: read " + rows + " rows and " + columns + " columns.");

		int tests = 0;
		for (int i = 0; i < header.size(); i++) {
			var h = header.get(i);
			var expectedPenalties = data.get(i);
			var r = new PenaltyFunction(h, 0.25);

			for (int row = 0; row < expectedPenalties.size(); row++) {
				var controlValue = posIdx.get(i) * 0.001 - 2;
				var actualPenalty = r.reward(controlValue);
				assertEquals(expectedPenalties.get(i), actualPenalty, 1e-8);
				tests++;
			}
		}

		System.out.println("Goldstone reward regression test: performed tests: " + tests);
	}
}
