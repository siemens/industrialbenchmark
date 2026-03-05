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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.siemens.industrialbenchmark.datavector.action.ActionDelta;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.datavector.state.ObservableStateDescription;
import com.siemens.industrialbenchmark.dynamics.IndustrialBenchmarkDynamics;
import com.siemens.industrialbenchmark.externaldrivers.setpointgen.SetPointGenerator;
import com.siemens.industrialbenchmark.externaldrivers.setpointgen.SetPointGeneratorStateDescription;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.rl.interfaces.DataVector;
import com.siemens.rl.interfaces.ExternalDriver;

/**
 * Test class for dynamics repeatability.
 *
 * @author Michel Tokic
 */
class TestDynamics {

	static final int INIT_STEPS = 10000;
	static final int MEM_STEPS = 10000;
	static final long ACTION_SEED = 12345;

	/**
	 * Tests that dynamics are repeatable (required by Particle-Swarm-Optimization).
	 */
	@Test
	void testRepeatableDynamics() throws IOException, PropertiesException {

		var props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		var lg = new SetPointGenerator(props);
		var externalDrivers = new ArrayList<ExternalDriver>();
		externalDrivers.add(lg);
		var d = new IndustrialBenchmarkDynamics(props, externalDrivers);
		var actionRand = new Random(System.currentTimeMillis());

		// 1) initialize dynamics with random trajectory
		var action = new ActionDelta(0.001f, 0.001f, 0.001f);
		for (int i = 0; i < INIT_STEPS; i++) {
			action.setDeltaGain(2.f * (actionRand.nextFloat() - 0.5f));
			action.setDeltaVelocity(2.f * (actionRand.nextFloat() - 0.5f));
			action.setDeltaShift(2.f * (actionRand.nextFloat() - 0.5f));
			d.step(action);
		}

		// 2) memorize states
		final var ms = d.getInternalMarkovState();

		// 3) perform and memorize test trajectory
		actionRand.setSeed(ACTION_SEED);
		var oStates = new DataVector[MEM_STEPS];
		var mStates = new DataVector[MEM_STEPS];

		for (int i = 0; i < MEM_STEPS; i++) {
			action.setDeltaGain(2.f * (actionRand.nextFloat() - 0.5f));
			action.setDeltaVelocity(2.f * (actionRand.nextFloat() - 0.5f));
			action.setDeltaShift(2.f * (actionRand.nextFloat() - 0.5f));
			d.step(action);
			oStates[i] = d.getState();
			mStates[i] = d.getInternalMarkovState();
		}

		// 4) reset and restore
		d.reset();
		d.setInternalMarkovState(ms);

		// 5) replay and verify
		actionRand.setSeed(ACTION_SEED);
		for (int i = 0; i < MEM_STEPS; i++) {
			action.setDeltaGain(2.f * (actionRand.nextFloat() - 0.5f));
			action.setDeltaVelocity(2.f * (actionRand.nextFloat() - 0.5f));
			action.setDeltaShift(2.f * (actionRand.nextFloat() - 0.5f));

			d.step(action);
			var oState = d.getState();
			var mState = d.getInternalMarkovState();

			assertEquals(oStates[i].getValue(ObservableStateDescription.SetPoint),
					oState.getValue(ObservableStateDescription.SetPoint), 0.0001);
			assertEquals(oStates[i].getValue(ObservableStateDescription.Fatigue),
					oState.getValue(ObservableStateDescription.Fatigue), 0.0001);
			assertEquals(oStates[i].getValue(ObservableStateDescription.Consumption),
					oState.getValue(ObservableStateDescription.Consumption), 0.0001);
			assertEquals(oStates[i].getValue(ObservableStateDescription.RewardTotal),
					oState.getValue(ObservableStateDescription.RewardTotal), 0.0001);

			assertEquals(mStates[i].getValue(MarkovianStateDescription.CurrentOperationalCost),
					mState.getValue(MarkovianStateDescription.CurrentOperationalCost), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.FatigueLatent2),
					mState.getValue(MarkovianStateDescription.FatigueLatent2), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.FatigueLatent1),
					mState.getValue(MarkovianStateDescription.FatigueLatent1), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.EffectiveActionGainBeta),
					mState.getValue(MarkovianStateDescription.EffectiveActionGainBeta), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.EffectiveActionVelocityAlpha),
					mState.getValue(MarkovianStateDescription.EffectiveActionVelocityAlpha), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.EffectiveShift),
					mState.getValue(MarkovianStateDescription.EffectiveShift), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.MisCalibration),
					mState.getValue(MarkovianStateDescription.MisCalibration), 0.0001);

			assertEquals(mStates[i].getValue(SetPointGeneratorStateDescription.SetPointChangeRatePerStep),
					mState.getValue(SetPointGeneratorStateDescription.SetPointChangeRatePerStep), 0.0001);
			assertEquals(mStates[i].getValue(SetPointGeneratorStateDescription.SetPointCurrentSteps),
					mState.getValue(SetPointGeneratorStateDescription.SetPointCurrentSteps), 0.0001);
			assertEquals(mStates[i].getValue(SetPointGeneratorStateDescription.SetPointLastSequenceSteps),
					mState.getValue(SetPointGeneratorStateDescription.SetPointLastSequenceSteps), 0.0001);

			assertEquals(mStates[i].getValue(MarkovianStateDescription.RewardFatigue),
					mState.getValue(MarkovianStateDescription.RewardFatigue), 0.0001);
			assertEquals(mStates[i].getValue(MarkovianStateDescription.RewardConsumption),
					mState.getValue(MarkovianStateDescription.RewardConsumption), 0.0001);
		}
	}

	@Test
	void testHistoryLength() throws IOException, PropertiesException {

		var props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		var lg = new SetPointGenerator(props);
		var externalDrivers = new ArrayList<ExternalDriver>();
		externalDrivers.add(lg);
		var d = new IndustrialBenchmarkDynamics(props, externalDrivers);

		var expHistSize = 0;
		for (var key : d.getInternalMarkovState().getKeys()) {
			if (key.startsWith("OPERATIONALCOST_")) {
				expHistSize++;
			}
		}

		assertEquals(expHistSize, d.getOperationalCostsHistoryLength());
	}
}
