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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs a series of simulations.
 */
public class ExampleExperiment {

	private ExampleExperiment() {}

	private static class SimulationWrapper<R> implements Callable<R> {

		private final Callable<R> simulation;
		private final int simulationIndex;
		private final int numSimulations;
		private final long startTimeMillis;

		SimulationWrapper(final Callable<R> simulation, final int simulationIndex, final int numSimulations, final long startTimeMillis) {

			this.simulation = simulation;
			this.simulationIndex = simulationIndex;
			this.numSimulations = numSimulations;
			this.startTimeMillis = startTimeMillis;
		}

		@Override
		public R call() throws Exception {

			final R result = simulation.call();

			final long currentMillis = System.currentTimeMillis();
			System.out.println("Compleeted simulation " + (simulationIndex + 1) + "/" + numSimulations + " after " + (currentMillis - startTimeMillis) + "ms");

			return result;
		}
	}

	public static void main(final String[] args) throws IOException, PropertiesException {

		final int numThreads = 2;
		final int simulationSteps = 1500;
		final String simPropsFilePath = "src/main/resources/sim.properties";
		final String outputFileNameTemplate = System.getProperty("user.home")
				+ "/indBenchSimRes_"
				+ "SetPoint${STATIONARY_SETPOINT}_"
				+ "Seed${SEED}_"
				+ "Time${time:yyyy-MM-dd_HH:mm:ss:SSS}.csv"; // for time format documentation, see: http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html

		final Properties props = PropertiesUtil.setpointProperties(new File(simPropsFilePath));

		final ExecutorService simulationThreadPool = Executors.newFixedThreadPool(numThreads);
		int experimentIndex = 0;
		final long startTimeMillis = System.currentTimeMillis();

		final int numExperiments = 100 * 100;
		for (int setPoint = 0; setPoint < 100; setPoint++) {
			for (int seed = 0; seed < 100; seed++) {
				props.setProperty("STATIONARY_SETPOINT", String.valueOf(setPoint));
				props.setProperty("SEED", String.valueOf(seed));

				final String outputFileName = TrialGuiMain.formatSaveFileName(outputFileNameTemplate, props);
				final RandomSimulation randomSimulation = new RandomSimulation(simulationSteps, props, null, new File(outputFileName));
				simulationThreadPool.submit(new SimulationWrapper<>(randomSimulation, experimentIndex, numExperiments, startTimeMillis));
				experimentIndex++;
			}
		}
	}
}
