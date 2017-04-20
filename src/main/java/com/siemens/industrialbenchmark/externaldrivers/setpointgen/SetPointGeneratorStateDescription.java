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
package com.siemens.industrialbenchmark.externaldrivers.setpointgen;

import com.siemens.industrialbenchmark.datavector.DataVectorDescriptionImpl;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;

/**
 * State description for the setpoint generator.
 */
public class SetPointGeneratorStateDescription extends DataVectorDescriptionImpl {

	// Definition of the observable variables from the markov state ...
	// the variable we drive
	public static final String SET_POINT = MarkovianStateDescription.SET_POINT;

	// internal state variables
	public static final String SET_POINT_LAST_SEQUENCE_STEPS = "SetPointLastSequenceSteps";
	public static final String SET_POINT_CURRENT_STEPS = "SetPointCurrentSteps";
	public static final String SET_POINT_CHANGE_RATE_PER_STEP = "SetPointChangeRatePerStep";

	private static final String[] STATE_VARS = new String[] {
		SET_POINT,
		SET_POINT_LAST_SEQUENCE_STEPS,
		SET_POINT_CURRENT_STEPS,
		SET_POINT_CHANGE_RATE_PER_STEP
	};

	public SetPointGeneratorStateDescription() {
		super(STATE_VARS);
	}
}
