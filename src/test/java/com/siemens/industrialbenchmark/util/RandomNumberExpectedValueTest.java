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
package com.siemens.industrialbenchmark.util;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Test;

public class RandomNumberExpectedValueTest {

	@Test
	public void testExpectedValues() {

		long seed = 0;
		Random rand = new Random(seed);
		RandomDataGenerator randomData = new RandomDataGenerator();

		double uniformAverage = 0.0;
		double binomialAverage = 0.0;
		double normalAverage = 0.0;
		double exponentialAverage = 0.0;

		for(int i=0; i<1e6; i++){

			// set current seed
			randomData.reSeed(seed);

			// draw random numbers
			double n = randomData.nextGaussian(0, 1);
			double u = randomData.nextUniform(0, 1);
			double b = randomData.nextBinomial(1, 0.5);
			double e = randomData.nextExponential(0.25);

			// average mean random number
			uniformAverage += (1. / (1.+i))*(u - uniformAverage);
			binomialAverage += (1. / (1.+i))*(b - binomialAverage);
			normalAverage += (1. / (1.+i))*(n - normalAverage);
			exponentialAverage += (1. / (1.+i))*(e - exponentialAverage);

			// draw new seed from global random generator
			seed = rand.nextLong();
		}

		assertEquals(0.5, uniformAverage, 0.001);
		assertEquals(0.5, binomialAverage, 0.001);
		assertEquals(0.0, normalAverage, 0.001);
		assertEquals(0.25, exponentialAverage, 0.001);
	}
}

