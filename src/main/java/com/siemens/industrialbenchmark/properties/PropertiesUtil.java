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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties util to retrieve typed property values from a properties object general pattern of methods:
 *
 * <pre>
 * {@code
 * public type getType(Properties aProperties, String aKey)
 * public type getType(Properties aProperties, String aKey, Type aDefault)
 * public type getType(Properties aProperties, String aKey, booelan aIsRequiered)
 * public type getType(Properties aProperties, String aKey, booelan aIsRequiered, Type aDefault)
 * }
 * </pre>
 *
 * where <br>
 * <li>type is the requested value of type Type <li>aProperties is the properties object that should contain the property <li>aKey is the
 * key to the requested property <li>aDefault value is a possible default value. If the property is not available from the properties
 * object, the default value is returned instead <li>aIsRequiered if true, the property is expected to be available, a
 * {@link PropertiesException} is thrown if unavailable <br>
 * <br>
 * Default behavior: the method
 *
 * <pre>
 * {@code
 * public type getType(aProperties, aKey)
 * }
 * </pre>
 *
 * shall be equivalent to:
 *
 * <pre>
 * {@code
 * public type getType(aProperties, aKey, false) // false: not required
 * }
 * </pre>
 *
 * resulting in
 *
 * <pre>
 * {@code
 * public type getType(aProperties, aKey, false, javaDefault)// javaDefault is 0 for number, null for objects
 * }
 * </pre>
 *
 * @author duell
 */
public final class PropertiesUtil {

	private PropertiesUtil() {}

	public static float getFloat(final Properties aProperties, final String aTag)
			throws PropertiesException
	{
		return getFloat(aProperties, aTag, false);
	}

	public static float getFloat(final Properties aProperties, final String aTag, final boolean aIsRequiered)
			throws PropertiesException
	{
		return getFloat(aProperties, aTag, aIsRequiered, 0);
	}

	public static long getLong(final Properties aProperties, final String aTag, final long aDefault)
			throws PropertiesException
	{
		return getLong(aProperties, aTag, false, aDefault);
	}

	public static long getLong(final Properties aProperties, final String aTag)
			throws PropertiesException
	{
		return getLong(aProperties, aTag, false, 0L);
	}

	public static long getLong(final Properties aProperties, final String aTag, final boolean aIsRequiered)
			throws PropertiesException
	{
		return getLong(aProperties, aTag, aIsRequiered, 0L);
	}

	public static long getLong(final Properties aProperties, final String aTag, final boolean aIsRequiered, final long aDefault)
			throws PropertiesException
	{
		final String value;
		if (aIsRequiered) {
			value = getRequiredProperty(aProperties, aTag);
		} else {
			value = aProperties.getProperty(aTag, Long.toString(aDefault));
		}

		try {
			return Long.parseLong(value.trim());
		} catch (final NumberFormatException e) {
			throw new PropertiesException("Could not map " + aTag + " to a double value: ", e, aProperties, aTag);
		}
	}

	public static double getDouble(final Properties aProperties, final String aTag)
			throws PropertiesException
	{
		try {
			return Double.parseDouble(aProperties.getProperty(aTag).trim());
		} catch (final NumberFormatException e) {
			throw new PropertiesException("Could not map " + aTag + " to a double value: ", e, aProperties, aTag);
		}
	}

	public static boolean getBoolean(final Properties aProperties, final String aTag)
			throws PropertiesException
	{
		return getBoolean(aProperties, aTag, false);
	}

	public static boolean getBoolean(final Properties aProperties, final String aTag, final boolean aIsRequiered)
			throws PropertiesException
	{
		return getBoolean(aProperties, aTag, aIsRequiered, false);
	}

	public static boolean getBoolean(final Properties aProperties, final String aTag, final boolean aIsRequiered, final boolean aDefault)
			throws PropertiesException
	{
		final String value;
		if (aIsRequiered) {
			value = getRequiredProperty(aProperties, aTag);
		} else {
			value = aProperties.getProperty(aTag, Boolean.toString(aDefault));
		}
		try {
			return Boolean.parseBoolean(value.trim());
		} catch (final Exception e) {
			throw new PropertiesException("Could not map " + aTag + " to an integer value: ", e, aProperties, aTag);
		}
	}

	public static float getFloat(final Properties aProperties, final String aTag, final float aDefault)
			throws PropertiesException
	{
		return getFloat(aProperties, aTag, false, aDefault);
	}

	public static float getFloat(final Properties aProperties, final String aTag, final boolean aIsRequiered, final float aDefault)
			throws PropertiesException
	{
		final String value;
		if (aIsRequiered) {
			value = getRequiredProperty(aProperties, aTag);
		} else {
			value = aProperties.getProperty(aTag, Float.toString(aDefault));
		}
		try {
			return Float.parseFloat(value.trim());
		} catch (NumberFormatException e) {
			throw new PropertiesException("Could not map " + aTag + " to a float value: ", e, aProperties, aTag);
		}
	}

	public static int getInt(final Properties aProperties, final String aTag, final int aDefault)
			throws PropertiesException
	{
		return getInt(aProperties, aTag, false, aDefault);
	}

	public static int getInt(final Properties aProperties, final String aTag)
			throws PropertiesException
	{
		return getInt(aProperties, aTag, false, 0);
	}

	public static int getInt(final Properties aProperties, final String aTag, final boolean aIsRequiered)
			throws PropertiesException
	{
		return getInt(aProperties, aTag, aIsRequiered, 0);
	}

	public static int getInt(final Properties aProperties, final String aTag, final boolean aIsRequiered, final int aDefault)
			throws PropertiesException
	{
		final String value;
		if (aIsRequiered) {
			value = getRequiredProperty(aProperties, aTag);
		} else {
			value = aProperties.getProperty(aTag, Integer.toString(aDefault));
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (final NumberFormatException e) {
			throw new PropertiesException("Could not map " + aTag + " to an integer value: ", e, aProperties, aTag);
		}
	}

	public static Properties getProperties(final String aFilename) throws IOException {
		return getProperties(new File(aFilename));
	}

	public static Properties getProperties(final File aFile) throws IOException {

		final Properties p = new Properties();

		try (final FileInputStream in = new FileInputStream(aFile)) {
			p.load(in);
		}
		return p;
	}

	/**
	 * @param aProp a property object
	 * @param aKey the key of the desired property
	 * @return the property value
	 * @throws PropertiesException if the property is not contained in the given
	 *   {@link Properties} object
	 */
	public static String getRequiredProperty(final Properties aProp, final String aKey)
			throws PropertiesException
	{
		final String ret = aProp.getProperty(aKey);
		if (ret == null) {
			throw new MissingPropertyException(aProp, aKey);
		}
		return ret;
	}

	public static Properties setpointProperties(final File aFile) throws IOException {

		if (!aFile.exists()) {
			throw new FileNotFoundException("Properties file '" + aFile.getAbsolutePath() + "' does not exist");
		}
		final Properties p = new Properties();
		try (final FileInputStream in = new FileInputStream(aFile)) {
			p.load(in);
		}
		return p;
	}
}

