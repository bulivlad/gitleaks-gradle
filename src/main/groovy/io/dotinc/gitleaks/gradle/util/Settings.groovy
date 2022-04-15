package io.dotinc.gitleaks.gradle.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * @author vladbulimac on 14.04.2022.
 */

public final class Settings {
    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class)
    private static final String ARRAY_SEP = ",";
    private Properties props = null
    private ObjectMapper objectMapper = new ObjectMapper();

    public Settings() {
        this.props = new Properties();
    }

    public Settings(Properties properties) {
        this.props = properties
    }

    public static final class KEYS {
        public static final String SKIP = "gitleaks.skip"
        public static final String FAIL_ON_ERROR = "gitleaks.fail_on_error"
        public static final String CONFIG_FILE = "gitleaks.config_file"
        public static final String SOURCE_PATH = "gitleaks.source_path"
        public static final String FORMAT = "gitleaks.format"
        public static final String OUTPUT_DIRECTORY = "gitleaks.output_directory"
        public static final String RUN_ENVIRONMENT = "gitleaks.run_environment"
    }

    public void setString(final String key, final String value) {
        props.setProperty(key, value)
        LOGGER.debug("Setting: {}='{}'", key, getPrintableValue(key, value))
    }

    /**
     * Sets a property value only if the value is not null.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setStringIfNotNull(final String key, final String value) {
        if (null != value) {
            setString(key, value)
        }
    }

    /**
     * Sets a property value only if the value is not null and not empty.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setStringIfNotEmpty(final String key, final String value) {
        if (null != value && !value.isEmpty()) {
            setString(key, value)
        }
    }

    /**
     * Sets a property value only if the array value is not null and not empty.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setArrayIfNotEmpty(final String key, final String[] value) {
        if (null != value && value.length > 0) {
            try {
                setString(key, objectMapper.writeValueAsString(value))
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException()
            }
        }
    }

    /**
     * Sets a property value only if the array value is not null and not empty.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setArrayIfNotEmpty(final String key, final List<String> value) {
        if (null != value && !value.isEmpty()) {
            try {
                setString(key, objectMapper.writeValueAsString(value))
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException()
            }
        }
    }

    /**
     * Sets a property value.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setBoolean(final String key, boolean value) {
        setString(key, Boolean.toString(value))
    }

    /**
     * Sets a property value only if the value is not null.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setBooleanIfNotNull(final String key, final Boolean value) {
        if (null != value) {
            setBoolean(key, value)
        }
    }

    /**
     * Sets a float property value.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setFloat(final String key, final float value) {
        setString(key, Float.toString(value))
    }

    /**
     * Sets a property value.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setInt(final String key, final int value) {
        props.setProperty(key, String.valueOf(value))
        LOGGER.debug("Setting: {}='{}'", key, value)
    }

    /**
     * Sets a property value only if the value is not null.
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    public void setIntIfNotNull(final String key, final Integer value) {
        if (null != value) {
            setInt(key, value)
        }
    }


    /**
     * Returns a value from the properties file. If the value was specified as a
     * system property or passed in via the -Dprop=value argument - this method
     * will return the value from the system properties before the values in the
     * contained configuration file.
     *
     * @param key the key to lookup within the properties file
     * @param defaultValue the default value for the requested property
     * @return the property from the properties file
     */
    public String getString(final String key, final String defaultValue) {
        return System.getProperty(key, props.getProperty(key, defaultValue))
    }

    /**
     * Returns a value from the properties file. If the value was specified as a
     * system property or passed in via the -Dprop=value argument - this method
     * will return the value from the system properties before the values in the
     * contained configuration file.
     *
     * @param key the key to lookup within the properties file
     * @return the property from the properties file
     */
    public String getString(final String key) {
        return System.getProperty(key, props.getProperty(key))
    }

    /**
     * Returns a list with the given key.
     * <p>
     * If the property is not set then {@code null} will be returned.
     *
     * @param key the key to get from this
     * {@link io.dotinc.gitleaks.gradle.util.Settings}.
     * @return the list or {@code null} if the key wasn't present.
     */
    public String[] getArray(final String key) {
        final String string = getString(key)
        if (string != null) {
            if (string.charAt(0) == ('{' as char) || string.charAt(0) == ('[' as char)) {
                try {
                    return objectMapper.readValue(string, String[].class)
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Unable to read value '" + string + "' as an array")
                }
            } else {
                return string.split(ARRAY_SEP)
            }
        }
        return null
    }

    /**
     * Removes a property from the local properties collection. This is mainly
     * used in test cases.
     *
     * @param key the property key to remove
     */
    public void removeProperty(final String key) {
        props.remove(key)
    }

    /**
     * Returns an int value from the properties file. If the value was specified
     * as a system property or passed in via the -Dprop=value argument - this
     * method will return the value from the system properties before the values
     * in the contained configuration file.
     *
     * @param key the key to lookup within the properties file
     * @return the property from the properties file
     * if there is an error retrieving the setting
     */
    public int getInt(final String key) {
        try {
            return Integer.parseInt(getString(key))
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Could not convert property '" + key + "' to an int.")
        }
    }

    /**
     * Returns an int value from the properties file. If the value was specified
     * as a system property or passed in via the -Dprop=value argument - this
     * method will return the value from the system properties before the values
     * in the contained configuration file.
     *
     * @param key the key to lookup within the properties file
     * @param defaultValue the default value to return
     * @return the property from the properties file or the defaultValue if the
     * property does not exist or cannot be converted to an integer
     */
    public int getInt(final String key, int defaultValue) {
        int value
        try {
            value = Integer.parseInt(getString(key))
        } catch (NumberFormatException ex) {
            if (!getString(key, "").isEmpty()) {
                LOGGER.debug("Could not convert property '{}={}' to an int using {} instead.",
                        key, getPrintableValue(key, getString(key)), defaultValue)
            }
            value = defaultValue
        }
        return value
    }

    /**
     * Returns a long value from the properties file. If the value was specified
     * as a system property or passed in via the -Dprop=value argument - this
     * method will return the value from the system properties before the values
     * in the contained configuration file.
     *
     * @param key the key to lookup within the properties file
     * @return the property from the properties file
     * if there is an error retrieving the setting
     */
    public long getLong(final String key) {
        try {
            return Long.parseLong(getString(key))
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Could not convert property '" + key + "' to a long.")
        }
    }

    /**
     * Returns a boolean value from the properties file. If the value was
     * specified as a system property or passed in via the
     * <code>-Dprop=value</code> argument this method will return the value from
     * the system properties before the values in the contained configuration
     * file.
     *
     * @param key the key to lookup within the properties file
     * @return the property from the properties file
     * if there is an error retrieving the setting
     */
    public boolean getBoolean(final String key) {
        return Boolean.parseBoolean(getString(key))
    }

    /**
     * Returns a boolean value from the properties file. If the value was
     * specified as a system property or passed in via the
     * <code>-Dprop=value</code> argument this method will return the value from
     * the system properties before the values in the contained configuration
     * file.
     *
     * @param key the key to lookup within the properties file
     * @param defaultValue the default value to return if the setting does not
     * exist
     * @return the property from the properties file
     */
    public boolean getBoolean(final String key, boolean defaultValue) {
        return Boolean.parseBoolean(getString(key, Boolean.toString(defaultValue)))
    }

    /**
     * Returns a float value from the properties file. If the value was
     * specified as a system property or passed in via the
     * <code>-Dprop=value</code> argument this method will return the value from
     * the system properties before the values in the contained configuration
     * file.
     *
     * @param key the key to lookup within the properties file
     * @param defaultValue the default value to return if the setting does not
     * exist
     * @return the property from the properties file
     */
    public float getFloat(final String key, float defaultValue) {
        float retValue = defaultValue
        try {
            retValue = Float.parseFloat(getString(key))
        } catch (Throwable ex) {
            LOGGER.trace("ignore", ex)
        }
        return retValue
    }


    /**
     * Obtains the printable/loggable value for a given key/value pair. This
     * will mask some values so as to not leak sensitive information.
     *
     * @param key the property key
     * @param value the property value
     * @return the printable value
     */
    protected String getPrintableValue(String key, String value) {
        String printableValue = null
        if (value != null) {
            printableValue = value
        }
        return printableValue
    }
}
