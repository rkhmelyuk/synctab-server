package com.khmlabs.synctab

/**
 * AuthToken used to identify a user client application.
 * It is created in authenticate action and removed on logout action.
 * AuthToken expires in 2 weeks, so client will require a user to authorize every 2 weeks.
 *
 * The same user can have many authTokens.
 */
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
