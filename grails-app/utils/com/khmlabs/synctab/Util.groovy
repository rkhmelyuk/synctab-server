package com.khmlabs.synctab

import org.codehaus.groovy.grails.plugins.codecs.SHA1Codec
import com.prutsoft.core.utils.ConversionUtils

class Util {

    @SuppressWarnings("GroovyAssignabilityCheck")
    static String hash(String value) {
        SHA1Codec.encode(value.bytes)
    }

    /**
     * Try to read the integer value. It accepts integer and string values.
     * The default value is return if incoming value is null or not number.
     *
     * @param value the incoming value to convert to integer.
     * @param defaultValue the default value.
     * @return the result of conversion.
     */
    static Integer getInteger(def value, Integer defaultValue) {
        if (value instanceof Integer) {
            return value
        }
        else if (value instanceof String) {
            return ConversionUtils.getInteger((String) value, defaultValue)
        }
        return defaultValue
    }

    /**
     * Try to read the long value. It accepts long and string values.
     * The default value is return if incoming value is null or not number.
     *
     * @param value the incoming value to convert to long.
     * @param defaultValue the default value.
     * @return the result of conversion.
     */
    static Long getLong(def value, Long defaultValue) {
        if (value instanceof Long) {
            return value
        }
        else if (value instanceof String) {
            return ConversionUtils.getLong((String) value, defaultValue)
        }
        return defaultValue
    }

}
