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
 * This class holds a HashMap of state-dimension/action-dimension -value pairs.
 *
 * @author Michel Tokic
 *
 */
public class DataVectorImpl implements Cloneable, DataVector {

	/**
	 *
	 */
	private static final long serialVersionUID = 4956886314253943518L;

	private HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
	private double values[];

	DataVectorDescription description = null;
	ImmutableList<String> keys;

	/**
	 * Initializes the state with a given StateVectorDescription. All associated values are set to NaN initially.
	 * @param desc The StateVectorDescription
	 */
	public DataVectorImpl (DataVectorDescription desc) {
		Preconditions.checkNotNull(desc, "Description must not be null.");
		this.description = desc;
		this.keys = ImmutableList.copyOf(desc.getVarNames());
		values = new double[this.keys.size()];
		for (int i=0; i<this.keys.size(); i++) {
			indexMap.put(this.keys.get(i), i);
			values[i] = Double.NaN;
		}
	}

	/**
	 * Initializes the state with a given list of state dimension names. All associated values are set to NaN initially.
	 * @param keys A list of keys.
	 */
	public DataVectorImpl (List<String> keys) {
		Preconditions.checkNotNull(keys, "Description must not be null.");
		this.keys = ImmutableList.copyOf(keys);
		values = new double[this.keys.size()];
		for (int i=0; i<this.keys.size(); i++) {
			indexMap.put(this.keys.get(i), i);
			values[i] = Double.NaN;
		}
	}

	/**
	 * Returns the value for a given state/action dimension
	 * @param key The state dimension
	 * @return The value
	 */
	public Double getValue(String key) {
		Preconditions.checkArgument(this.getKeys().contains(key), "%s is not a valid variable", key);

		if (!indexMap.containsKey(key)) {
			return Double.NaN;
		} else {
			return values[indexMap.get(key)];
		}
	}

	/**
	 * Sets the current value of a given state/action dimension
	 * @param key The state/action dimension
	 * @param value The value
	 */
	public void setValue (String key, double value) {
		Preconditions.checkNotNull(this.getKeys(), "keySet is null!!");
		Preconditions.checkArgument(this.getKeys().contains(key), "%s is not a valid variable. Available names are: %s", key, this.getKeys());
		values[indexMap.get(key)] = value;
	}

	/**
	 * returns a list containing the state/action dimension names
	 * @return a list containing the state/action dimension names
	 */
	public List<String> getKeys() {
		return this.keys;
	}

	/**
	 * returns a list of current values
	 * @return a list of current values
	 */
	public List<Double> getValues() {
		Builder<Double> valueBuilder = new ImmutableList.Builder<Double>();
		for (String key : keys) {
			valueBuilder.add(values[indexMap.get(key)]);
		}
		return valueBuilder.build();
	}

	/**
	 * returns a double[] array containing the values
	 * @return a double[] array containing the values
	 */
	public double[] getValuesArray() {
		double values[] = new double[keys.size()];
		for (int i=0; i<this.keys.size(); i++) {
			values[i] = this.values[indexMap.get(keys.get(i))];
		}
		return values;
	}

    @Override
    public String toString() {
    	String output = "{";
    	String key;
    	for (int i=0; i<this.keys.size(); i++) {
    		key=this.keys.get(i);
			// last element with "]" instead of ", "
    		if (i==this.keys.size()-1) {
        		output += key + "=" + values[indexMap.get(key)] + "}";
			} else {
				output += key + "=" + values[indexMap.get(key)] + ", ";
			}
		}
    	return output;
    }

    public DataVector clone() {
		DataVector s = new DataVectorImpl (this.getKeys());
    	for (String key : this.getKeys()) {
    		s.setValue(key,  this.getValue(key));
    	}

        return s;
    }
}

