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
package com.siemens.industrialbenchmark.properties;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;

public class TestProperties {

	@Test (expected=PropertiesException.class)
	public void testExceptions() throws IOException, PropertiesException {
		Properties props = PropertiesUtil.setpointProperties(new File ("src/main/resources/sim.properties"));
		PropertiesUtil.getBoolean(props, "NOT_IN_PROPERTIES_FILES", true);
	}

	@Test
	public void testReadProperty() throws IOException, PropertiesException {
		Properties props = PropertiesUtil.setpointProperties(new File ("src/main/resources/sim.properties"));
		PropertiesUtil.getFloat(props, "CRD", true);
	}
}

