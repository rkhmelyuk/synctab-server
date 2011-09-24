package com.khmlabs.synctab

class AuthToken {

    String id
    String userId

    long timestamp
    long endTimestamp

    static mapping = {
        userId column: 'user'
        timestamp column: 'ts'
        endTimestamp column: 'endts'

        version false
        cache true
    }

    static constraints = {
        userId nullable: false, blank: false
    }
}
