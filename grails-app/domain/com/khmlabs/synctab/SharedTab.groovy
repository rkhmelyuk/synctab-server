package com.khmlabs.synctab

/**
 * A shared tab or link.
 *
 * @author Ruslan Khmelyuk
 */
class SharedTab {

    String id

    User user
    String title
    String link
    String device
    String favicon
    Date date

    static mapping = {
        link field: 'link', index: true
        user field: 'userId', index: true
        title filed: 'title'
        device field: 'device'
        favicon filed: 'favicon'
        date field: 'date', index: true

        version false

        sort 'date'
        order 'desc'
    }

    static constraints = {
        user nullable: false
        title nullable: true, blank: true
        link nullable: false, blank: false
        device nullable: false, blank: false
        favicon nullable: true, blank: true
        date nullable: false
    }

    String toString() {
        "SharedTab[$id,$link,$date]"
    }

}
