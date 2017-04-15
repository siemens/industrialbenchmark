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
package com.siemens.industialbenchmark.dynamics;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.junit.Test;

import com.siemens.industrialbenchmark.datavector.action.ActionDelta;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.dynamics.IndustrialBenchmarkDynamics;
import com.siemens.industrialbenchmark.externaldrivers.setpointgen.SetPointGenerator;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.rl.interfaces.DataVector;
import com.siemens.rl.interfaces.Environment;
import com.siemens.rl.interfaces.ExternalDriver;

public class DynamicsRegressionTest {

	@Test
	public void test() {

		// TODO: fix w.r.t. zero action in IndustrialBenchmarkDynamics constructor
		/*
		// parse regression data file
		ClassLoader classLoader = getClass().getClassLoader();
		File f = new File (classLoader.getResource("dynamics/dyn-markov-old.csv").getFile());
		HashMap<String,Integer> keyMap = new HashMap<String,Integer>();

		try {

			BufferedReader reader = new BufferedReader(new FileReader(f));

			// parse CSV keys
			if (reader.ready()) {
				String[] fields = reader.readLine().split(" ");

				for (int i=0; i<fields.length; i++) {
					keyMap.put(fields[i], i);
				}
			}

			// Instantiate benchmark
			// setpoint configuration parameters
			Properties props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
			// instantiate setpoint generator (external driver)
			SetPointGenerator lg = new SetPointGenerator(props);
			List<ExternalDriver> externalDrivers = new ArrayList<ExternalDriver>();
			externalDrivers.add(lg);
			// instantiate industrial benchmark
			Environment db = new IndustrialBenchmarkDynamics(props, externalDrivers);

			// seed PRNG from configured seed in configuration file
			long seed = PropertiesUtil.getLong(props, "SEED", System.currentTimeMillis());
			System.out.println("==================================\nStarting DynamicsRegressionTest with main seed: " + seed);
			Random rand = new Random(seed);

			// apply constant action (gain and velocity transitions from 0 => 100)
			final ActionDelta action = new ActionDelta(0.1f, 0.1f, 0.1f);

			//parse data rows and compare current dynamics to the test data
			while (reader.ready()) {
				String values[] = reader.readLine().split(" ");

				// apply random action and retrieve markov state
				action.setDeltaGain(2.f * (rand.nextFloat() - 0.5f));
				action.setDeltaVelocity(2.f * (rand.nextFloat() - 0.5f));
				db.step(action);
				DataVector markovState = db.getInternalMarkovState();

				// compare values
				assertEquals(Double.parseDouble(values[keyMap.get("Velocity")]), markovState.getValue(MarkovianStateDescription.Action_Velocity), 0.01);
				assertEquals(Double.parseDouble(values[keyMap.get("Gain")]), markovState.getValue(MarkovianStateDescription.Action_Gain), 0.01);
				assertEquals(Double.parseDouble(values[keyMap.get("HiddenDynVelocity")]), markovState.getValue(MarkovianStateDescription.FatigueLatent1), 0.01);
				assertEquals(Double.parseDouble(values[keyMap.get("HiddenDynGain")]), markovState.getValue(MarkovianStateDescription.FatigueLatent2), 0.01);
				assertEquals(Double.parseDouble(values[keyMap.get("Dynamics")]), markovState.getValue(MarkovianStateDescription.Fatigue), 0.01);

				// check goldstone variables
				assertEquals(Double.parseDouble(values[keyMap.get("HiddenGSDomain")]), markovState.getValue(MarkovianStateDescription.MisCalibrationDomain), 0.01);
				assertEquals(Double.parseDouble(values[keyMap.get("HiddenGSSystemResponse")]), markovState.getValue(MarkovianStateDescription.MisCalibrationSystemResponse), 0.01);
				assertEquals(Double.parseDouble(values[keyMap.get("HiddenGSPhiIdx")]), markovState.getValue(MarkovianStateDescription.MisCalibrationPhiIdx), 0.01);

				// check operationalcosts
				assertEquals(Double.parseDouble(values[keyMap.get("OperationalCostsConv")]), markovState.getValue(MarkovianStateDescription.OperationalCostsConv), 0.01);
				assertEquals(Double.parseDouble(values[keyMap.get("OperationalCosts")]), markovState.getValue(MarkovianStateDescription.CurrentOperationalCost), 0.01);
				for (int i=0; i<10; i++) {
					String e = "OPERATIONALCOST_" + i;
					assertEquals(Double.parseDouble(values[keyMap.get(e)]), markovState.getValue(e), 0.01);
				}
				assertEquals(Double.parseDouble(values[keyMap.get("RewardOperationalCosts")]), markovState.getValue(MarkovianStateDescription.RewardConsumption), 0.01);
				assertEquals(Double.parseDouble(values[keyMap.get("RewardTotal")]), markovState.getValue(MarkovianStateDescription.RewardTotal), 0.01);
			}

			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PropertiesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
}

