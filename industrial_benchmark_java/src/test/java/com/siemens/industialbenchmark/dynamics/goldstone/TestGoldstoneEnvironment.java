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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.google.common.base.Preconditions;
import com.siemens.industrialbenchmark.dynamics.goldstone.GoldstoneEnvironment;

public class TestGoldstoneEnvironment {

	@Test
	public void test() {

		// parse regression data file
		ClassLoader classLoader = getClass().getClassLoader();
		File f = new File (classLoader.getResource("environment_class_regression_data.xa").getFile());

		ArrayList<Double> action = new ArrayList<Double>();			
		ArrayList<Double> penalty = new ArrayList<Double>();

		try {

			BufferedReader reader = new BufferedReader(new FileReader(f));

			while (reader.ready()) {
				String line = reader.readLine();

				// only catch non-comment lines
				if (!line.startsWith("#")){
					String[] fields = line.split(" ");

					Preconditions.checkArgument(fields.length == 3, 
							"three fields (step, action, penalty) are expected, but " + fields.length + " fields were parsed.");

					action.add(Double.parseDouble(fields[1]));
					penalty.add(Double.parseDouble(fields[2]));	
				}
			}

			reader.close();
			System.out.println ("Goldstone environment class regression test: read " + action.size() + " rows.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int numberOfSteps = 24;
		double maxRequiredStep = 0.25;
		double safeZone = 0.5 * maxRequiredStep;
		GoldstoneEnvironment env = new GoldstoneEnvironment(numberOfSteps, maxRequiredStep, safeZone);
		for (int step=0; step< action.size(); step++) {

			double reward = env.stateTransition(action.get(step));

//			System.out.println ("step: " + step + ", pos=" + env.getControlPosition() + ", reward=" + reward);
			if(Math.abs(env.getControlPosition())<=1.5) {
				assertEquals (-penalty.get(step), reward, 1e-8);
			}
		}
	}

}
