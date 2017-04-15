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

import java.io.Serializable;
import java.util.List;

/**
 * This interface lists all necessary methods to implement a data vector,
 * which might be a state- or action-vector.
 *
 * @author Michel Tokic
 *
 */
public interface DataVector extends Serializable {

	/**
	 * Returns the value for a given data-vector dimension.
	 * @param key The state dimension.
	 * @return The value
	 */
	public Double getValue(String key);

	/**
	 * Sets the current value of a given data-vector dimension.
	 * @param key The state or action dimension.
	 * @param value The associated value.
	 */
	public void setValue (String key, double value);

	/**
	 * Returns a list containing the data-vector dimension names.
	 * The keys are ordered in the way as they are associated when
	 * calling getValuesArray().
	 *
	 * @return A list containing the data-vector dimension names.
	 */
	public List<String> getKeys();

	/**
	 * Returns a double[] array containing the values.
	 * @return A double[] array containing the values.
	 */
	public double[] getValuesArray();

	/**
	 * Returns a copy of the data vector.
	 * @return A copy of the data vector.
	 */
	public DataVector clone();
}
