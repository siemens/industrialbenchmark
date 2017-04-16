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
 * Indicates properties that are not available within a given property object.
 *
 * @author duell
 */
public class PropertiesException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 4372660914816539735L;
	private final Properties properties;
	private final String key;

	/**
	 * @param properties the property object searched for a property
	 * @param key key of the missing property
	 */
	public PropertiesException(Properties properties, String key) {
		this("Error while retrieving property '" + key + "' from configuration.", properties, key);
	}

	/**
	 * @param aMessage error message
	 * @param aProperties the property object searched for a property
	 * @param aKey key of the missing property
	 */
	public PropertiesException(String aMessage, Properties aProperties, String aKey) {
		super(aMessage);
		properties = aProperties;
		key = aKey;
	}

	/**
	 * @param aMessage error message
	 * @param aThrowable cause of the exception
	 * @param aProperties the property object searched for a property
	 * @param aKey key of the missing property
	 */
	public PropertiesException(String aMessage, Throwable aThrowable, Properties aProperties, String aKey) {
		super(aMessage, aThrowable);
		properties = aProperties;
		key = aKey;
	}

	/**
	 * @return property object missing a property and therefore causing the exception
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @return property key causing the exception
	 */
	public String getKey() {
		return key;
	}
}

