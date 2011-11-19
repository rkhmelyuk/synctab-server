package com.khmlabs.synctab

import grails.test.*

class SettingTests extends GrailsUnitTestCase {

    void testFields() {

        Setting setting = new Setting()
        setting.id = "abcdef"
        setting.name = "lastVersion"
        setting.value = "v0.7"

        assertEquals "abcdef", setting.id
        assertEquals "lastVersion", setting.name
        assertEquals "v0.7", setting.value
    }
}
