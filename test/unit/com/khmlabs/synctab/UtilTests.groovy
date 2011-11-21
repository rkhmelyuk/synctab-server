package com.khmlabs.synctab

/**
 * The tests for {@link Util}'s methods.
 */
class UtilTests extends GroovyTestCase {

    public void testIsEmail() {
        assertTrue(Util.isEmail("test@synctabapp.com"))
        assertTrue(Util.isEmail("test.or.not.to.test@synctabapp.com"))
        assertTrue(Util.isEmail("test.or.not.to+test@synctabapp.com"))
        assertTrue(Util.isEmail("master@synctabapp.com.ua"))
        assertTrue(Util.isEmail("test@synctabapp.com.labmba"))
        assertFalse(Util.isEmail("master@synctabapp"))
        assertFalse(Util.isEmail("@synctabapp.com"))
        assertFalse(Util.isEmail("synctabapp.com"))
    }

    public void testExtractCharsetName() {
        assertEquals("UTF-8", Util.extractCharsetName("text/plain; charset=UTF-8"))
        assertEquals("ascii", Util.extractCharsetName("image/png; charset=ascii; media=image"))
        assertEquals("ascii", Util.extractCharsetName("image/plain; charset=ascii : text/html; charset=utf-16"))
        assertEquals('windows-1251', Util.extractCharsetName("charset=windows-1251"))

        assertNull(Util.extractCharsetName("image/plain"))
        assertNull(Util.extractCharsetName(""))
    }

    public void testRelativeLinkToAbsolute() {
        assertAbsoluteLink("favicon.ico", "http://example.com/favicon.ico");
        assertAbsoluteLink("/favicon.ico", "http://example.com/favicon.ico");
        assertAbsoluteLink("//example.com/favicon.ico", "http://example.com/favicon.ico");
        assertAbsoluteLink("../favicon.ico", "http://example.com/../favicon.ico");
    }

    void assertAbsoluteLink(String link, String result) {
        URL url = new URL("http://example.com")
        assertEquals result, Util.relativeLinkToAbsolute(url, link)
    }
}
