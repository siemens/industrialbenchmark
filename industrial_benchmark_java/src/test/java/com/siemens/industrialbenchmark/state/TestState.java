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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.siemens.industrialbenchmark.datavector.state.ObservableState;
import com.siemens.industrialbenchmark.datavector.state.ObservableStateDescription;

class TestState {

	@Test
	void testRuntimeException() {
		var s = new ObservableState();
		assertThrows(IllegalArgumentException.class, () -> s.getValue("NOT_DEFINED"));
	}

	@Test
	void testNAN() {
		var s = new ObservableState();
		var gainValue = s.getValue(ObservableStateDescription.Action_Gain);
		assertTrue(Double.isNaN(gainValue));
	}

	@Test
	void testSetAndCheckValues() {
		var s = new ObservableState();

		var value = 1.2345;
		s.setValue(ObservableStateDescription.SetPoint, value);
		assertEquals(value, s.getValue(ObservableStateDescription.SetPoint), 0.0001);

		value = value * 2.0f;
		s.setValue(ObservableStateDescription.SetPoint, value);
		assertEquals(value, s.getValue(ObservableStateDescription.SetPoint), 0.0001);
	}
}
