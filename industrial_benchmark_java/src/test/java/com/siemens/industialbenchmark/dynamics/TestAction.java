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
package com.siemens.industialbenchmark.dynamics;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.siemens.industrialbenchmark.datavector.action.ActionAbsolute;
import com.siemens.industrialbenchmark.datavector.action.ActionDelta;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;

class TestAction {

	@Test
	void testActionAbsolute() throws IOException, PropertiesException {

		float b = 0;
		float a = 0;
		float c = 0;

		var props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
		var aa = new ActionAbsolute(b, a, c, props);
		var ad = new ActionDelta(0, 0, 0);
		var actionRand = new Random(System.currentTimeMillis());

		float deltaB = 0;
		float deltaA = 0;
		float deltaC = 0;

		for (int i = 0; i < 100000; i++) {
			do { deltaB = 2.f * (actionRand.nextFloat() - 0.5f); } while ((b + deltaB) < 0 || (b + deltaB) > 100);
			do { deltaA = 2.f * (actionRand.nextFloat() - 0.5f); } while ((a + deltaA) < 0 || (a + deltaA) > 100);
			do { deltaC = 2.f * (actionRand.nextFloat() - 0.5f); } while ((c + deltaC) < 0 || (c + deltaC) > 100);

			aa.setGain(b + deltaB);
			aa.setVelocity(a + deltaA);
			aa.setShift(c + deltaC);

			ad.setDeltaGain(deltaB);
			ad.setDeltaVelocity(deltaA);
			ad.setDeltaShift(deltaC);

			assertEquals(aa.getDeltaGain(), ad.getDeltaGain(), 0.0001);
			assertEquals(aa.getDeltaVelocity(), ad.getDeltaVelocity(), 0.0001);
			assertEquals(aa.getDeltaShift(), ad.getDeltaShift(), 0.0001);

			b += deltaB;
			a += deltaA;
			c += deltaC;

			assertEquals(aa.getGain(), b, 0.0001);
			assertEquals(aa.getVelocity(), a, 0.0001);
			assertEquals(aa.getShift(), c, 0.0001);
		}
	}
}
