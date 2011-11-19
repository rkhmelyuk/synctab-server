package com.khmlabs.synctab

import com.prutsoft.core.utils.ConversionUtils
import org.codehaus.groovy.grails.plugins.codecs.SHA1Codec

class Util {

    static final String EMAIL_REGEX = "[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";

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

    /**
     * Check if the input string is an email.
     *
     * @param email the input string, that claims to be an email.
     * @return true if input string is email, otherwise false.
     */
    static boolean isEmail(String email) {
        return email ==~ EMAIL_REGEX
    }

    /**
     * Check if link is relative and if it's so, converts it to absolute.
     *
     * @param pageURL the page url.
     * @param link the link to check and convert to absolute.
     * @return the absolute link.
     */
    static String relativeLinkToAbsolute(URL pageURL, String link) {
        link = link.trim()
        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            // the relative is relative

            if (!link.startsWith("/")) {
                link = "/" + link
            }
            if (!link.startsWith("//")) {
                String path = pageURL.getProtocol() + "://" + pageURL.getHost()

                // append port
                int port = pageURL.getPort()
                if (port > 0 && port != 80 && port != 443) {
                    path += ":" + Integer.toString(port)
                }

                link = path + link
            }
            else {
                // to handle links like //www.google.com/favicon.png
                link = "http:" + link
            }
        }

        return link
    }

    /**
     * Extract the charset name form the content type string.
     * Content type string is received from Content-Type header.
     *
     * @param contentType the content type string; must be no null, no checking
     * @return the found charset name or null if not found.
     */
    static String extractCharsetName(String contentType) {

        String charsetName = null
        final String[] mediaTypes = contentType.split(":")
        if (mediaTypes) {
            final String[] params = mediaTypes[0].split(";")
            for (String each in params) {
                each = each.trim()
                if (each.startsWith("charset=")) {
                    charsetName = each.substring(8)
                    break
                }
            }
        }

        return charsetName
    }

}
