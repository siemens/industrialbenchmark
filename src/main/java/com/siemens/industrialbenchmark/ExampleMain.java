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
import java.io.IOException;
import java.util.Properties;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.industrialbenchmark.util.PlotCurve;
import java.util.List;
import java.util.Map;

public class ExampleMain {

	public static final String DEFALT_SIM_PROPS_FILE_PATH = "src/main/resources/sim.properties";

	/**
	 * Run example benchmark with random actions for data generation purposes.
	 *
	 * @param args
	 * @throws IOException
	 * @throws PropertiesException
	 */
	public static void main(final String[] args) throws IOException, PropertiesException {

		final int nSteps = 1500;
		final String outputVar = MarkovianStateDescription.RewardTotal;

		// configuration of the properties file
		String simPropsFilePath = DEFALT_SIM_PROPS_FILE_PATH;
		if (args.length >= 1) { // if filepath was given to main()
			simPropsFilePath = args[0];
			System.out.println("Using config file: '" + simPropsFilePath + "'");
		} else {
			System.out.println("Using default config file: '" + simPropsFilePath + "'. A custom config file can be passed as an additional parameter.");
		}

		// setpoint configuration parameters
		Properties props = PropertiesUtil.setpointProperties(new File(simPropsFilePath));

		final RandomSimulation randomSimulation = new RandomSimulation(nSteps, props, null, new File("dyn-markov.csv"));
		final Map<String, List<Double>> states = randomSimulation.call();
		final List<Double> data = states.get(outputVar);

		// plot the data
		PlotCurve.plot(outputVar, "t", outputVar, data);
	}
}
