/**
Copyright 2017 Siemens AG.

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

import com.siemens.industrialbenchmark.datavector.action.ActionDelta;
import com.siemens.industrialbenchmark.dynamics.IndustrialBenchmarkDynamics;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.rl.interfaces.DataVector;
import com.siemens.rl.interfaces.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.BoundedRangeModel;

public class RandomSimulation implements Callable<Map<String, List<Double>>> {

	private final int nSteps;
	private final Properties props;
	private final BoundedRangeModel progressListener;
	private final File output;

	public RandomSimulation(
			final int nSteps,
			final Properties props,
			final BoundedRangeModel progressListener,
			final File output)
			throws IOException, PropertiesException
	{
		this.nSteps = nSteps;
		this.props = props;
		this.progressListener = progressListener;
		this.output = output;
	}

	@Override
	public Map<String, List<Double>> call() throws IOException, PropertiesException {

		/**
		 * Instantiate benchmark
		 */
		// instantiate industrial benchmark
		Environment db = new IndustrialBenchmarkDynamics(props);

		// seed PRNG from configured seed in configuration file
		final long randomSeed = PropertiesUtil.getLong(props, "SEED", System.currentTimeMillis());
		System.out.println("main seed: " + randomSeed);
		final Random rand = new Random(randomSeed);

		// apply constant action (gain and velocity transitions from 0 => 100)
		final ActionDelta deltaAction = new ActionDelta(0.1f, 0.1f, 0.1f);

		final DataVector internalMarkovState = db.getInternalMarkovState();
		final LinkedHashMap<String, List<Double>> internalStates = new LinkedHashMap<>(internalMarkovState.getKeys().size());
		for (final String key : internalMarkovState.getKeys()) {
			internalStates.put(key, new ArrayList<>(nSteps));
		}

		try (final FileWriter outputFW = (output == null) ? null : new FileWriter(output)) {
			// write column headers
			if (outputFW != null) {
				for (String key : db.getInternalMarkovState().getKeys()) {
					outputFW.write(key);
					outputFW.write(',');
					outputFW.write(' ');
				}
				outputFW.write('\n');
			}

			/*
			 * Perform random actions and write markov state to text file
			 */
			// data array that stores the reward
			for (int si = 0; si < nSteps; si++) {
				// set random action from the interval [-1, 1]
				deltaAction.setDeltaGain(2.f * (rand.nextFloat() - 0.5f));
				deltaAction.setDeltaVelocity(2.f * (rand.nextFloat() - 0.5f));

				db.step(deltaAction);
				final DataVector markovState = db.getInternalMarkovState();
				final double[] markovStateValues = markovState.getValuesArray();
				final Iterator<List<Double>> stateValueLists = internalStates.values().iterator();
				for (int msvi = 0; msvi < markovStateValues.length; msvi++) {
					stateValueLists.next().add(markovStateValues[msvi]);
				}

				// write data
				if (outputFW != null) {
					for (String key : markovState.getKeys()) {
						outputFW.write(String.valueOf(markovState.getValue(key)));
						outputFW.write(' ');
					}
					outputFW.write('\n');
				}

				if (progressListener != null) {
					progressListener.setValue(si);
				}
			}
		}

		return internalStates;
	}
}
