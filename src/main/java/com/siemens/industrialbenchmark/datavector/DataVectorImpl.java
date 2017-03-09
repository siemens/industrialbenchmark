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
package com.siemens.industrialbenchmark.datavector;

import java.util.HashMap;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.siemens.rl.interfaces.DataVector;

/**
 * This holds a map of state-dimension/action-dimension -value pairs.
 *
 * @author Michel Tokic
 */
public class DataVectorImpl implements Cloneable, DataVector {

	/**
	 *
	 */
	private static final long serialVersionUID = 4956886314253943518L;

	private final HashMap<String, Integer> indexMap;
	private double[] values;

	private final DataVectorDescription description;
	private final ImmutableList<String> keys;

	private DataVectorImpl(final DataVectorDescription description, final List<String> keys) {
		this.description = description;
		this.keys = ImmutableList.copyOf(keys);
		this.indexMap = new HashMap<>();
		this.values = new double[this.keys.size()];
		for (int i = 0; i < this.keys.size(); i++) {
			this.indexMap.put(this.keys.get(i), i);
			this.values[i] = Double.NaN;
		}
	}

	/**
	 * Initializes the state with a given StateVectorDescription.
	 * All associated values are set to <code>NaN</code> initially.
	 * @param description The StateVectorDescription
	 */
	public DataVectorImpl(final DataVectorDescription description) {
		this(description, description.getVarNames());
		Preconditions.checkNotNull(description, "Description must not be null.");
	}

	/**
	 * Initializes the state with a given list of state dimension names.
	 * All associated values are set to <code>NaN</code> initially.
	 * @param keys A list of keys.
	 */
	public DataVectorImpl(final List<String> keys) {
		this(null, keys);
		Preconditions.checkNotNull(keys, "Keys must not be null.");
	}

	/**
	 * Returns the value for a given state/action dimension
	 * @param key The state dimension
	 * @return The value
	 */
	@Override
	public Double getValue(String key) {
		Preconditions.checkArgument(this.getKeys().contains(key),
				"%s is not a valid variable", key);

		if (indexMap.containsKey(key)) {
			return values[indexMap.get(key)];
		} else {
			return Double.NaN;
		}
	}

	/**
	 * Sets the current value of a given state/action dimension
	 * @param key The state/action dimension
	 * @param value The value
	 */
	@Override
	public void setValue(String key, double value) {
		Preconditions.checkNotNull(this.getKeys(), "keySet is null!!");
		Preconditions.checkArgument(this.getKeys().contains(key),
				"%s is not a valid variable. Available names are: %s", key, this.getKeys());
		values[indexMap.get(key)] = value;
	}

	/**
	 * @return a list containing the state/action dimension names
	 */
	@Override
	public List<String> getKeys() {
		return this.keys;
	}

	/**
	 * @return a list of current values
	 */
	public List<Double> getValues() {
		final Builder<Double> valueBuilder = new ImmutableList.Builder<>();
		for (String key : keys) {
			valueBuilder.add(values[indexMap.get(key)]);
		}
		return valueBuilder.build();
	}

	/**
	 * @return a double[] array containing the values
	 */
	@Override
	public double[] getValuesArray() {
		final double[] valuesCopy = new double[keys.size()];
		for (int ki = 0; ki < keys.size(); ki++) {
			valuesCopy[ki] = values[indexMap.get(keys.get(ki))];
		}
		return valuesCopy;
	}

	@Override
	public String toString() {
		final StringBuilder output = new StringBuilder("{");
		for (int ki = 0; ki < keys.size(); ki++) {
			final String key = keys.get(ki);
			output.append(key).append("=").append(values[indexMap.get(key)]);
			// last element with "]" instead of ", "
			if (ki == keys.size() - 1) {
				output.append("}");
			} else {
				output.append(", ");
			}
		}
		return output.toString();
	}

	@Override
	public DataVector clone() {
		DataVector s = new DataVectorImpl(this.getKeys());
		for (String key : this.getKeys()) {
			s.setValue(key, this.getValue(key));
		}

		return s;
	}
}

