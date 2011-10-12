package com.khmlabs.synctab

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
    }

    static constraints = {
        user nullable: false
        title nullable: true, blank: true
        link nullable: false, blank: false
        device nullable: false, blank: false
        favicon nullable: true, blank: true
        date nullable: false
    }

}
