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

import java.util.Properties;

/**
 * Exception thrown when a property was expected but not found within a {@link Properties} object
 */
public class MissingPropertyException extends PropertiesException {

	private static final long serialVersionUID = -2126707120438673909L;

	public MissingPropertyException(String aMessage, Throwable aThrowable, Properties aProperties, String aKey) {
        super(aMessage, aThrowable, aProperties, aKey);
    }

    public MissingPropertyException(Properties aProperties, String aKey) {
        super("property '" + aKey + "' missing from configuration.", aProperties, aKey);
    }
}
