package com.khmlabs.synctab

/**
 * Tags used to filter the tabs.
 * For example, user can share a tab for Home tag only, so only Home browser will open it.
 * Tags is a good way to add multi-browser and multi-device support.
 *
 * @author Ruslan Khmelyuk
 */
class Tag {

    String id

    User user
    String name
    Date created

    static mapping = {
        user field: 'userId', index: true
        name filed: 'name'

        version false

        sort 'created'
        order 'desc'
    }

    static constraints = {
        user nullable: false
        name nullable: false, blank: false
        created nullable: false
    }
}
