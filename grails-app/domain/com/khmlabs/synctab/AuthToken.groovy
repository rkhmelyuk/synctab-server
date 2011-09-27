package com.khmlabs.synctab

class AuthToken {

    String id
    String userId
    String token

    long timestamp
    long endTimestamp

    static mapping = {
        userId column: 'user', index: true
        token column: 'token', index: true
        timestamp column: 'ts'
        endTimestamp column: 'endts'

        version false
        cache true
    }

    static constraints = {
        token nullable: false, blank: false
        userId nullable: false, blank: false
    }
}
