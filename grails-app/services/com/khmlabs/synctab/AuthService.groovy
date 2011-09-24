package com.khmlabs.synctab

import com.prutsoft.core.KeyGenerator

class AuthService {

    static transactional = false

    static long MAX_TOKEN_TIME = 1000 * 60 * 60 * 24 * 14 // 2 weeks
    static int TOKEN_KEY_LENGTH = 40

    UserService userService

    AuthToken auth(User user) {
        final AuthToken token = new AuthToken()

        token.id = KeyGenerator.generateSimpleKey(TOKEN_KEY_LENGTH)
        token.timestamp = System.currentTimeMillis()
        token.endTimestamp = token.timestamp + MAX_TOKEN_TIME
        token.userId = user.id

        return token.save()
    }

    void discard(AuthToken token) {
        if (token != null) {
            token.delete()
        }
    }

    AuthToken getAuthToken(String key) {
        //noinspection GroovyAssignabilityCheck
        return AuthToken.get(key)
    }

    User getUserByToken(String key) {
        if (key) {
            AuthToken token = getAuthToken(key)
            if (token != null) {
                if (token.endTimestamp > System.currentTimeMillis()) {
                    return userService.getUser(token.userId)
                }

                discard(token)
            }
        }

        return null
    }
}
