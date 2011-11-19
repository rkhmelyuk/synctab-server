package com.khmlabs.synctab

import grails.test.*

class TagTests extends GrailsUnitTestCase {

    void testFields() {

        def date = new Date()

        Tag tag = new Tag()
        tag.name = "Name"
        tag.user = new User(id: "abcdef")
        tag.created = date

        assertEquals "Name", tag.name
        assertEquals "abcdef", tag.user.id
        assertEquals date, tag.created
    }
}
