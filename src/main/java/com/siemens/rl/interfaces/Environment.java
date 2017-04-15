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
package com.siemens.rl.interfaces;

/**
 * This interface describes all relevant methods for implementing the dynamics of an environment.
 *
 * @author Michel Tokic
 *
 */
public interface Environment {

	/**
	 * Returns the reward.
	 * @return the reward
	 */
	double getReward();

	/**
	 * Returns the observable state.
	 * @return the observable state
	 */
	DataVector getState();

	/**
	 * Returns the internal Markovian state.
	 * @return the internal Markovian state
	 */
	DataVector getInternalMarkovState();

	/**
	 * Performs an action within the environment and returns the reward
	 * @param action The action to perform
	 * @return The reward
	 */
	double step (DataVector action);

	/**
	 * Function for resetting the environment.
	 */
	void reset();
}

