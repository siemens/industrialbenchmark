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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.siemens.industrialbenchmark.dynamics.goldstone.PenaltyFunction;

public class TestGoldstoneReward {

	@Test
	public void rewardRegressionTest() {

		// Open File
		ClassLoader classLoader = getClass().getClassLoader();
		File f = new File(classLoader.getResource("reward_function_regression_data.xa").getFile());

		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> posIdx = new ArrayList<Double>();
		ArrayList<Double> header = new ArrayList<Double>();

		int columns=0;
		int rows=0;

		try {

			BufferedReader reader = new BufferedReader(new FileReader(f));

			while (reader.ready()) {
				String line = reader.readLine();

				// catch comment lines
				if (line.startsWith("#\t") || line.startsWith("# ")) {

				// catch column headers
				} else if (line.startsWith("#!")) {

					line = line.replaceAll("#!\\t", "");

					String[] fields = line.split(" ");
					for (int i=1; i<fields.length; i++) {
						header.add(Double.parseDouble(fields[i]));
						data.add(new ArrayList<Double>());
						columns++;
					}

				// catch data
				} else {
					String[] fields = line.split(" ");
					posIdx.add(Double.parseDouble(fields[0]));

					for (int i=1; i<fields.length; i++) {
						data.get(i-1).add(Double.parseDouble(fields[i]));
					}
					rows++;
				}
			}

			reader.close();
			System.out.println("Goldstone reward regression test: read " + rows + " rows and " + columns + " columns.");
			//System.out.println("Column headers: " + header.toString());
			//System.out.println("PosIdx: " + posIdx.toString());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// # convert PosIdx to numerical position via function:
		// #    pos(i) = i* 0.001 - 2
		// #!      PosIdx 0.0 0.350637827502 1.39497578049 1.35727168795 1.57079632679 2.06468250258 1.57991996765 2.42169220333 3.14159265359 3.59335211815 3.60989362992 3.66581391529 4.71238898038 5.83470249569 5.54879877481 6.20301420392

		/*
		for each column:

			h = column_header, e.g. 0.350637827502
			expected_penalties = value_array(column)

			Initialize RewardFunction r:
				r = new RewardFunction(h, 0.25 <= constant for test data!)

			for i in range (0, len(expected_penalties):
			 	ControlValue = PosIdx[i] * 0.001 - 2
			 	ActualPenalty= r.reward(ControlValue)
			 	Assert.assertEquals(ActualPanelty, expected_penalties[i], 0.000001)
		*/

		int tests=0;
		for (int i=0; i<header.size(); i++) {
			double h = header.get(i);
			ArrayList<Double> expected_penalties = data.get(i);
			PenaltyFunction r = new PenaltyFunction(h, 0.25);

			for (int row=0; row<expected_penalties.size(); row++) {
				double controlValue = posIdx.get(i) * 0.001 - 2;
				double actualPenalty = r.reward(controlValue);
				Assert.assertEquals(expected_penalties.get(i), actualPenalty, 1e-8);
				tests++;
			}
		}

		System.out.println("Goldstone reward regression test: performed tests: " + tests);
	}
}

