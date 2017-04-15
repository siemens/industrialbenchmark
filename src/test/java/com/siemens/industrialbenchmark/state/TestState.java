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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.siemens.industrialbenchmark.datavector.state.ObservableState;
import com.siemens.industrialbenchmark.datavector.state.ObservableStateDescription;

public class TestState {

	@Test(expected=IllegalArgumentException.class)
	public void testRuntimeException() {
		ObservableState s = new ObservableState();
		// the following should throw the RuntimeException, because key "NOT_DEFINED" does not exists
		System.out.println("value=" + s.getValue("NOT_DEFINED"));
	}

	@Test
	public void testNAN() {
		ObservableState s = new ObservableState();
		// read unset value
		double gainValue = s.getValue(ObservableStateDescription.Action_Gain);
		System.out.println("gain: actual=" + gainValue + ", expected=NaN");
		assertTrue(Double.isNaN(gainValue));
	}

	@Test
	public void testSetAndCheckValues() {

		ObservableState s = new ObservableState();

		double value = 1.2345;
		s.setValue(ObservableStateDescription.SetPoint, value);
		assertEquals(value, s.getValue(ObservableStateDescription.SetPoint), 0.0001);

		value = value * 2.0f;
		s.setValue(ObservableStateDescription.SetPoint, value);
		assertEquals(value, s.getValue(ObservableStateDescription.SetPoint), 0.0001);
	}
}

