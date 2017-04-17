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

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;


/**
 * state/action description for the industrial benchmark
 */
public abstract class DataVectorDescription {

	private final List<String> names;

	/**
	 * Constructor with a given List of state/action dimension names
	 * @param names
	 */
	public DataVectorDescription(final List<String> names) {
		Preconditions.checkNotNull(names, "name list must not be null.");
		Preconditions.checkArgument(names.size() > 0, "name list has size 0");
		Preconditions.checkArgument(new HashSet<>(names).size() == names.size(), "name list contains duplicates");

		final Builder<String> lb = ImmutableList.builder();
		for (String key : names) {
			lb.add(key);
		}
		this.names = lb.build();
	}

	/**
	 * Constructor with a given set of state/action description names
	 * @param names
	 */
	public DataVectorDescription(final String[] names) {
		Preconditions.checkNotNull(names, "name list must not be null.");
		Preconditions.checkArgument(names.length > 0, "name list has size 0");
		Preconditions.checkArgument(new HashSet<>(Arrays.asList(names)).size() == names.length, "name list contains duplicates");

		final Builder<String> lb = ImmutableList.builder();
		for (String key : names) {
			lb.add(key);
		}
		this.names = lb.build();
	}

	/**
	 * Returns the number of variables.
	 * @return
	 */
	public int getNumberVariables() {
		return names.size();
	}

	/**
	 * Returns a list containing the variable names.
	 * @return
	 */
	public List<String> getVarNames() {
		return names;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DataVectorDescription other = (DataVectorDescription) obj;
		return Objects.equals(this.names, other.names);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + Objects.hashCode(this.names);
		return hash;
	}

	@Override
	public String toString() {
		return names.toString();
	}
}
