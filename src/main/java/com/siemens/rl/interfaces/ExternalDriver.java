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
 * Abstract interface for attaching external drivers to the
 * Environment, that affect/filter certain state dimensions
 * (e.g. such as setpoint).
 *
 * @author Michel Tokic
 */
public interface ExternalDriver {

	/**
	 * Sets the random seed.
	 * @param seed The random seed to set.
	 */
	public void setSeed (long seed);

	/**
	 * Applies "in-place" the external drivers to the given data vector.
	 * @param state The data vector to apply the external drivers to.
	 */
	public void filter (DataVector state);

	/**
	 * Sets the external driver configuration from within the given data vector.
	 * @param state The data vector containing the configuration variables.
	 */
	public void setConfiguration (DataVector state);

	/**
	 * Returns the current configuration.
	 * @return The current configuration.
	 */
	public DataVector getState();
}

