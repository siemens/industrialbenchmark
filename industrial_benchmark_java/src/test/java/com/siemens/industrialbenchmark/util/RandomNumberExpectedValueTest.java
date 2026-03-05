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
package com.siemens.industrialbenchmark.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.jupiter.api.Test;

class RandomNumberExpectedValueTest {

	@Test
	void testExpectedValues() {

		long seed = 0;
		var rand = new Random(seed);
		var randomData = new RandomDataGenerator();

		var uniformAverage = 0.0;
		var binomialAverage = 0.0;
		var normalAverage = 0.0;
		var exponentialAverage = 0.0;

		for (int i = 0; i < 1e6; i++) {
			randomData.reSeed(seed);

			var n = randomData.nextGaussian(0, 1);
			var u = randomData.nextUniform(0, 1);
			var b = (double) randomData.nextBinomial(1, 0.5);
			var e = randomData.nextExponential(0.25);

			uniformAverage += (1.0 / (1.0 + i)) * (u - uniformAverage);
			binomialAverage += (1.0 / (1.0 + i)) * (b - binomialAverage);
			normalAverage += (1.0 / (1.0 + i)) * (n - normalAverage);
			exponentialAverage += (1.0 / (1.0 + i)) * (e - exponentialAverage);

			seed = rand.nextLong();
		}

		assertEquals(0.5, uniformAverage, 0.001);
		assertEquals(0.5, binomialAverage, 0.001);
		assertEquals(0.0, normalAverage, 0.001);
		assertEquals(0.25, exponentialAverage, 0.001);
	}
}
