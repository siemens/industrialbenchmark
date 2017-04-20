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
package com.siemens.industrialbenchmark.datavector.state;

import com.siemens.industrialbenchmark.datavector.DataVectorDescriptionImpl;

/**
 * State description for the industrial benchmark.
 */
public class ObservableStateDescription extends DataVectorDescriptionImpl {

	// Definition of the observable variables from the markov state ...
	public static final String SetPoint = MarkovianStateDescription.SET_POINT;
	public static final String Action_Velocity = MarkovianStateDescription.ACTION_VELOCITY;
	public static final String Action_Gain = MarkovianStateDescription.ACTION_GAIN;
	public static final String Action_Shift = MarkovianStateDescription.ACTION_SHIFT;
	public static final String Fatigue = MarkovianStateDescription.FATIGUE;
	public static final String Consumption = MarkovianStateDescription.CONSUMPTION;
	public static final String RewardTotal = MarkovianStateDescription.REWARD_TOTAL;

	private static final String[] STATE_VARS = new String[] {
		SetPoint,
		Action_Velocity,
		Action_Gain,
		Action_Shift,
		Fatigue,
		RewardTotal,
		Consumption
	};

	public ObservableStateDescription() {
		super(STATE_VARS);
	}
}
