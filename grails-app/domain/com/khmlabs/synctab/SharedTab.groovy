package com.khmlabs.synctab

/**
 * A shared tab or link.
 *
 * @author Ruslan Khmelyuk
 */
class SharedTab {

    String id

    Tag tag
    User user

    String title
    String link
    String favicon
    Date date

    static mapping = {
        link field: 'link', index: true
        user field: 'userId', index: true
        title filed: 'title'
        favicon field: 'favicon'
        tag field: 'tag'
        date field: 'date', index: true

        version false

        sort 'date'
        order 'desc'
    }

    static constraints = {
        tag nullable: true
        user nullable: false
        title nullable: true, blank: true
        link nullable: false, blank: false
        favicon nullable: true, blank: true
        date nullable: false
    }

    static transients = ['device']

    String toString() {
        "SharedTab[$id,$link,$date]"
    }

    String getDevice() {
        tag?.name
    }

}
