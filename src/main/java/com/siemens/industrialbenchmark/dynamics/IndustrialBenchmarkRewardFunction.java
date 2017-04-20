/*
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
package com.siemens.industrialbenchmark.dynamics;

import java.util.Properties;

import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.rl.interfaces.DataVector;

/**
 * Reward function for the industrial benchmark.
 *
 * @author Siegmund Duell, Michel Tokic
 */
public class IndustrialBenchmarkRewardFunction {

	private final double crd;
	private final double cre;

	public IndustrialBenchmarkRewardFunction(final Properties aProperties)
			throws PropertiesException
	{
		crd = PropertiesUtil.getFloat(aProperties, "CRD", true);
		cre = PropertiesUtil.getFloat(aProperties, "CRE", true);
	}

	/**
	 * Calculates the reward for a given state. Values are updated in the State object.
	 * @param mState The state to calculate the reward for
	 */
	public void calcReward(final DataVector mState) {

		// Dynamics
		final double rD = -mState.getValue(MarkovianStateDescription.FATIGUE);

		// Goldstone reward
		//double rGS = mState.getValue(MarkovianStateDescription.RewardGS);

		// OperationalCost
		final double rE = -mState.getValue(MarkovianStateDescription.CONSUMPTION);

		mState.setValue(MarkovianStateDescription.REWARD_CONSUMPTION_WEIGHTED, cre * rE);
		mState.setValue(MarkovianStateDescription.REWARD_FATIGUE_WEIGHTED, crd * rD);
		mState.setValue(MarkovianStateDescription.REWARD_CONSUMPTION, rE);
		mState.setValue(MarkovianStateDescription.REWARD_FATIGUE, rD);
		mState.setValue(MarkovianStateDescription.REWARD_TOTAL, crd * rD + cre * rE);
	}
}

