package com.khmlabs.synctab

class SharedTab {

    String id

    String title
    String link
    String device
    Date date

    static mapping = {
        link field: 'link'
        title filed: 'title'
        device field: 'device'
        date field: 'date', index: true

        version false
    }

    static constraints = {
        title nullable: true, blank: true
        link nullable: false, blank: false
        device nullable: false, blank: false
        date nullable: false
    }

}
