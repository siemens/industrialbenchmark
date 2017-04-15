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
package com.siemens.industrialbenchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import com.siemens.industrialbenchmark.datavector.action.ActionDelta;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.dynamics.IndustrialBenchmarkDynamics;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.industrialbenchmark.util.PlotCurve;
import com.siemens.rl.interfaces.DataVector;
import com.siemens.rl.interfaces.Environment;

public class ExampleMain {

	/**
	 * Run example benchmark with random actions for data generation purposes.
	 *
	 * @param args
	 * @throws IOException
	 * @throws PropertiesException
	 */
	public static void main(String[] args) throws IOException, PropertiesException {

		// configuration of the properties file
		String filename = "src/main/resources/sim.properties"; // default filepath
		if (args.length >= 1) {  // if filepath was given to main()
			filename = args[0];
			System.out.println("Using config file: '" + filename + "'");
		} else {
			System.out.println("Using default config file: '" + filename + "'. A custom config file can be passed as an additional parameter.");
		}

		/**
		 * Instantiate benchmark
		 */
		// setpoint configuration parameters
		Properties props = PropertiesUtil.setpointProperties( new File (filename));

		// instantiate industrial benchmark
		Environment db = new IndustrialBenchmarkDynamics(props);

		// seed PRNG from configured seed in configuration file
		long seed = PropertiesUtil.getLong(props, "SEED", System.currentTimeMillis());
		System.out.println("main seed: " + seed);
		Random rand = new Random(seed);

		DataVector markovState = db.getInternalMarkovState();
		DataVector observableState = db.getState();

		// apply constant action (gain and velocity transitions from 0 => 100)
		final ActionDelta deltaAction = new ActionDelta(0.1f, 0.1f, 0.1f);

		// write column headers
		FileWriter fwm = new FileWriter("dyn-markov.csv");
		fwm.write("time ");
		for (String key : db.getInternalMarkovState().getKeys()) {
			fwm.write(key + " ");
		}
		fwm.write("\n");

		FileWriter fw = new FileWriter("dyn-observable.csv");
		fw.write("time ");
		for (String key : db.getState().getKeys()) {
			fw.write(key + " ");
		}
		fw.write("\n");


		// data array for memorizing the reward
		final int steps = PropertiesUtil.getInt(props, "SIM_STEPS", 1500);
		double data[] = new double[steps];

		/*************************************************************
		 * Perform random actions and write markov state to text file
		 *************************************************************/
		for (int i = 0; i < steps; i++) {

			// set random action from the interval [-1, 1]
			deltaAction.setDeltaGain(2.f * (rand.nextFloat() - 0.5f));
			deltaAction.setDeltaVelocity(2.f * (rand.nextFloat() - 0.5f));
			deltaAction.setDeltaShift(2.f * (rand.nextFloat() - 0.5f));

			db.step(deltaAction);
			markovState = db.getInternalMarkovState();
			observableState = db.getState();

			// write data
			fw.write(Integer.toString(i+1) + " ");
			for (String key : observableState.getKeys()) {
				fw.write(observableState.getValue(key) + " ");
			}
			fw.write("\n");

			fwm.write(Integer.toString(i+1) + " ");
			for (String key : markovState.getKeys()) {
				fwm.write(markovState.getValue(key) + " ");
			}
			fwm.write("\n");

			data[i] = db.getState().getValue(MarkovianStateDescription.RewardTotal);
		}

		fw.close();
		fwm.close();

		// plot reward
		PlotCurve.plot("RewardTotal", "t", "reward", data);
	}

}
