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
package com.siemens.industrialbenchmark.datavector;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.siemens.rl.interfaces.DataVectorDescription;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;


/**
 * state/action description for the industrial benchmark
 */
public abstract class DataVectorDescriptionImpl implements DataVectorDescription {

	private final List<String> varNames;

	/**
	 * Constructs a new description based on names in a <code>List</code>.
	 * @param names state/action description names
	 */
	public DataVectorDescriptionImpl(final List<String> names) {
		Preconditions.checkNotNull(names, "name list must not be null.");
		Preconditions.checkArgument(!names.isEmpty(), "name list has size 0");
		Preconditions.checkArgument(new HashSet<>(names).size() == names.size(), "name list contains duplicates");

		final Builder<String> lb = ImmutableList.builder();
		for (final String key : names) {
			lb.add(key);
		}
		this.varNames = lb.build();
	}

	/**
	 * Constructs a new description based on names in a <code>String</code> array.
	 * @param names state/action description names
	 */
	public DataVectorDescriptionImpl(final String... names) {
		this(Arrays.asList(names));
	}

	@Override
	public int getNumberVariables() {
		return varNames.size();
	}

	@Override
	public List<String> getVarNames() {
		return varNames;
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
		final DataVectorDescriptionImpl other = (DataVectorDescriptionImpl) obj;
		return Objects.equals(this.varNames, other.varNames);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + Objects.hashCode(this.varNames);
		return hash;
	}

	@Override
	public String toString() {
		return varNames.toString();
	}
}
