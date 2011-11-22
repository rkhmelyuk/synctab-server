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
    String device
    String favicon
    Date date

    static mapping = {
        link field: 'link', index: true
        user field: 'userId', index: true
        title field: 'title'
        device field: 'device'
        favicon field: 'favicon'
        tag field: 'tag', index: true
        date field: 'date', index: true

        version false

        sort 'date'
        order 'desc'
    }

    static constraints = {
        tag nullable: true
        user nullable: false
        device nullable: false, blank: false
        title nullable: true, blank: true
        link nullable: false, blank: false
        favicon nullable: true, blank: true
        date nullable: false
    }

    String toString() {
        "SharedTab[$id,$link,$date]"
    }

}
