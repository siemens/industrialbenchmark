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
package com.siemens.industrialbenchmark.dynamics;

import java.util.Properties;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.rl.interfaces.DataVector;

/**
 * Reward function for the industrial benchmark
 *
 * @author Siegmund Duell, Michel Tokic
 */
public class IndustrialBenchmarkRewardFunction {

    private final double CRD;
    private final double CRE;

    public IndustrialBenchmarkRewardFunction (Properties aProperties) throws PropertiesException{
        CRD = PropertiesUtil.getFloat(aProperties, "CRD", true);
        CRE = PropertiesUtil.getFloat(aProperties, "CRE", true);
	}

	/**
	 * Calculates the reward for a given state. Values are updated in the State object.
	 * @param mState The state to calculate the reward for
	 */
	public void calcReward (DataVector mState) {

		// Dynamics
		double rD = -mState.getValue(MarkovianStateDescription.Fatigue);

		// Goldstone reward
		//double rGS = mState.getValue(MarkovianStateDescription.RewardGS);

		// OperationalCost
		double rE = -mState.getValue(MarkovianStateDescription.Consumption);

        mState.setValue(MarkovianStateDescription.RewardConsumptionWeighted, CRE * rE);
        mState.setValue(MarkovianStateDescription.RewardFatigueWeighted, CRD * rD);
        mState.setValue(MarkovianStateDescription.RewardConsumption, rE);
        mState.setValue(MarkovianStateDescription.RewardFatigue, rD);
        mState.setValue(MarkovianStateDescription.RewardTotal, CRD * rD + CRE * rE);
	}
}

