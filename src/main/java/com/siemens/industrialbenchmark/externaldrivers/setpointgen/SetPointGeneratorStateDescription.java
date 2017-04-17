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
package com.siemens.industrialbenchmark.externaldrivers.setpointgen;

import com.siemens.industrialbenchmark.datavector.DataVectorDescription;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;

/**
 * state description for the setpoint generator
 */
public class SetPointGeneratorStateDescription extends DataVectorDescription {

	// Definition of the observable variables from the markov state
	public static final String SetPoint = MarkovianStateDescription.SetPoint; // the variable we drive

	// internal state variables
	public static final String SetPointLastSequenceSteps="SetPointLastSequenceSteps";
	public static final String SetPointCurrentSteps="SetPointCurrentSteps";
	public static final String SetPointChangeRatePerStep="SetPointChangeRatePerStep";


	private final static String[] stateVars = new String[] {
			SetPoint, SetPointLastSequenceSteps, SetPointCurrentSteps, SetPointChangeRatePerStep
	};

	public SetPointGeneratorStateDescription() {
		super (stateVars);
	}
}
