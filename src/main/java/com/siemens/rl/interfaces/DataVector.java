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
package com.siemens.rl.interfaces;

import java.io.Serializable;
import java.util.List;

/**
 * This implements a data vector,
 * which might be a state- or action-vector.
 *
 * @author Michel Tokic
 */
public interface DataVector extends Serializable, Cloneable {

	DataVectorDescription getDescription();

	/**
	 * Returns the value for a given data-vector dimension.
	 * @param key The state dimension.
	 * @return The value
	 */
	Double getValue(String key);

	/**
	 * Sets the current value of a given data-vector dimension.
	 * @param key The state or action dimension.
	 * @param value The associated value.
	 */
	void setValue(String key, double value);

	/**
	 * Returns a list containing the data-vector dimension names.
	 * The keys are ordered in the way as they are associated when
	 * calling getValuesArray().
	 *
	 * @return dimension names
	 */
	List<String> getKeys();

	/**
	 * Returns the values.
	 * @return dimension values
	 */
	double[] getValuesArray();

	/**
	 * Creates and returns a copy of this object.
	 *
	 * @return     a clone of this instance.
	 * @throws  CloneNotSupportedException  if the object's class does not
	 *               support the {@code Cloneable} interface. Subclasses
	 *               that override the {@code clone} method can also
	 *               throw this exception to indicate that an instance cannot
	 *               be cloned.
	 * @see java.lang.Cloneable
	 * @see java.lang.Object#clone()
	 */
	DataVector clone() throws CloneNotSupportedException;
}

