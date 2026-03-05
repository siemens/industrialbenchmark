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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.function.Function;

/**
 * Properties utility for typed property retrieval.
 *
 * <p>A single generic {@link #get} method replaces the former per-type
 * overloads ({@code getInt}, {@code getFloat}, {@code getDouble}, etc.),
 * cutting ~220 lines of boilerplate. Convenience shortcuts for the most
 * common types are retained for call-site readability.
 *
 * @author duell
 */
public class PropertiesUtil {

    /**
     * Generic typed property retrieval.
     *
     * @param props      the properties object
     * @param key        the property key
     * @param isRequired throw if the key is missing
     * @param defaultVal the default value (as a string)
     * @param parser     converts the string value to the target type
     * @param typeName   human-readable type name for error messages
     * @param <T>        the target type
     * @return the parsed value
     * @throws PropertiesException if parsing fails or a required key is missing
     */
    public static <T> T get(Properties props, String key, boolean isRequired,
                            String defaultVal, Function<String, T> parser,
                            String typeName) throws PropertiesException {
        String value;
        if (isRequired) {
            value = getRequiredProperty(props, key);
        } else {
            value = props.getProperty(key, defaultVal);
        }
        try {
            return parser.apply(value.trim());
        } catch (Exception e) {
            throw new PropertiesException(
                    "Could not map " + key + " to a " + typeName + " value: ", e, props, key);
        }
    }

    // ---- float convenience methods ----

    public static float getFloat(Properties p, String key) throws PropertiesException {
        return getFloat(p, key, false, 0f);
    }

    public static float getFloat(Properties p, String key, boolean required) throws PropertiesException {
        return getFloat(p, key, required, 0f);
    }

    public static float getFloat(Properties p, String key, float def) throws PropertiesException {
        return getFloat(p, key, false, def);
    }

    public static float getFloat(Properties p, String key, boolean required, float def) throws PropertiesException {
        return get(p, key, required, Float.toString(def), Float::parseFloat, "float");
    }

    // ---- int convenience methods ----

    public static int getInt(Properties p, String key) throws PropertiesException {
        return getInt(p, key, false, 0);
    }

    public static int getInt(Properties p, String key, boolean required) throws PropertiesException {
        return getInt(p, key, required, 0);
    }

    public static int getInt(Properties p, String key, int def) throws PropertiesException {
        return getInt(p, key, false, def);
    }

    public static int getInt(Properties p, String key, boolean required, int def) throws PropertiesException {
        return get(p, key, required, Integer.toString(def), Integer::parseInt, "integer");
    }

    // ---- long convenience methods ----

    public static long getLong(Properties p, String key) throws PropertiesException {
        return getLong(p, key, false, 0L);
    }

    public static long getLong(Properties p, String key, boolean required) throws PropertiesException {
        return getLong(p, key, required, 0L);
    }

    public static long getLong(Properties p, String key, long def) throws PropertiesException {
        return getLong(p, key, false, def);
    }

    public static long getLong(Properties p, String key, boolean required, long def) throws PropertiesException {
        return get(p, key, required, Long.toString(def), Long::parseLong, "long");
    }

    // ---- double convenience methods ----

    public static double getDouble(Properties p, String key) throws PropertiesException {
        return get(p, key, true, "0", Double::parseDouble, "double");
    }

    // ---- boolean convenience methods ----

    public static boolean getBoolean(Properties p, String key) throws PropertiesException {
        return getBoolean(p, key, false, false);
    }

    public static boolean getBoolean(Properties p, String key, boolean required) throws PropertiesException {
        return getBoolean(p, key, required, false);
    }

    public static boolean getBoolean(Properties p, String key, boolean required, boolean def) throws PropertiesException {
        return get(p, key, required, Boolean.toString(def), Boolean::parseBoolean, "boolean");
    }

    // ---- Properties file loading (with try-with-resources) ----

    public static Properties getProperties(String filename) throws IOException {
        return getProperties(new File(filename));
    }

    public static Properties getProperties(File file) throws IOException {
        var props = new Properties();
        try (var in = new FileInputStream(file)) {
            props.load(in);
        }
        return props;
    }

    public static String getRequiredProperty(Properties props, String key) throws PropertiesException {
        var value = props.getProperty(key);
        if (value == null) throw new MissingPropertyException(props, key);
        return value;
    }

    public static Properties setpointProperties(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Properties file '" + file.getAbsolutePath() + "' does not exist");
        }
        var props = new Properties();
        try (var in = new FileInputStream(file)) {
            props.load(in);
        }
        return props;
    }
}
