package com.khmlabs.synctab

/**
 * Represents the single setting with id (name) and value.
 */
class Setting {

    String id
    String name
    String value

    static mapping = {
        name field: 'name', index: true
        value field: 'value'
    }

    static constraints = {
        name nullable: false, blank: false
        value nullable: true
    }
}
