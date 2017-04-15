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
package com.siemens.industrialbenchmark.state;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.datavector.state.ObservableStateDescription;

public class TestStateDescription {

	@Test
	public void testObservableStateDescription() {

		// test ObservableStateDescription
		ObservableStateDescription o1 = new ObservableStateDescription();
		ObservableStateDescription o2 = new ObservableStateDescription();
		assertTrue(o1.equals(o2));

	}
	@Test
	public void testMarkovianStateDescription() {


		// test MarkovianStateDescription
		List <String> mNames = new ArrayList<String>();
		mNames.add("TestVar");

		MarkovianStateDescription m1 = new MarkovianStateDescription(mNames);
		MarkovianStateDescription m2 = new MarkovianStateDescription(mNames);

		assertTrue(m1.toString().equals(m2.toString()));
	}

	@Test
	public void testSizes() {

		ObservableStateDescription o1 = new ObservableStateDescription();
		ObservableStateDescription o2 = new ObservableStateDescription();

		assertTrue(o1.getNumberVariables() == o2.getVarNames().size());

	}

}

