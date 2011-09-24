package com.khmlabs.synctab

class User {

    String id

    String email
    String password

    long created
    boolean active

    static mapping = {
        email field: 'email', index: true
        password filed: 'password'
        created field: 'created'
        active field: 'active'

        version false
        cache true
    }

    static constraints = {
        email nullable: false, blank: false, maxSize: 500
        password nullable: false, blank: false, maxSize: 500
    }
}
