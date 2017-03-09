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
package com.siemens.industrialbenchmark.datavector.state;

import com.siemens.industrialbenchmark.datavector.DataVectorDescription;

/**
 * State description for the industrial benchmark.
 */
public class ObservableStateDescription extends DataVectorDescription {

	// Definition of the observable variables from the markov state ...
	public static final String SetPoint = MarkovianStateDescription.SetPoint;
	public static final String Action_Velocity = MarkovianStateDescription.Action_Velocity;
	public static final String Action_Gain = MarkovianStateDescription.Action_Gain;
	public static final String Action_Shift = MarkovianStateDescription.Action_Shift;
	public static final String Fatigue = MarkovianStateDescription.Fatigue;
	public static final String Consumption = MarkovianStateDescription.Consumption;
	public static final String RewardTotal = MarkovianStateDescription.RewardTotal;

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
