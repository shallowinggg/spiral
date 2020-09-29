package io.github.shallowinggg.spiral.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author ding shimin
 */
public final class SystemPropertyUtils {

    private static final Log log = LogFactory.getLog(SystemPropertyUtils.class);

    private SystemPropertyUtils() {
    }

    /**
     * Returns {@code true} if and only if the system property with the specified {@code key}
     * exists.
     */
    public static boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to {@code null} if the property access fails.
     *
     * @return the property value or {@code null}
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     *
     * @return the property value.
     * {@code def} if there's no such property or if an access to the
     * specified property is not allowed.
     */
    public static String get(final String key, String def) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty.");
        }

        String value = null;
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
            }
        } catch (SecurityException e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Unable to retrieve a system property '%s'; default values will be used."
                        , key), e);
            }
        }

        if (value == null) {
            return def;
        }

        return value;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     *
     * @return the property value.
     * {@code def} if there's no such property or if an access to the
     * specified property is not allowed.
     */
    public static boolean getBoolean(String key, boolean def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            return def;
        }

        if ("true".equals(value) || "yes".equals(value) || "on".equals(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equals(value) || "no".equals(value) || "off".equals(value) || "0".equals(value)) {
            return false;
        }

        if (log.isWarnEnabled()) {
            log.warn(String.format("Unable to parse the boolean system property '%s':%s - using the default value: %s",
                    key, value, def));
        }

        return def;
    }
}
