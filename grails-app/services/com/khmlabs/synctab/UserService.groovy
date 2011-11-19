package com.khmlabs.synctab

class UserService {

    static transactional = false

    TagService tagService

    boolean registerUser(User user, String password) {
        if (user && password) {
            user.created = System.currentTimeMillis()
            user.active = true
            user.password = hashPassword(password, user.email)

            if (user.save() != null) {
                tagService.addDefaultTags(user)
                return true
            }
        }
        return false

    }

    boolean changePassword(User user, String newPassword) {
        if (newPassword && user) {
            user = getUser(user.id)
            if (user) {
                user.password = hashPassword(newPassword, user.email)
                return user.save() != null
            }
        }

        return false
    }

    User getUser(String id) {
        //noinspection GroovyAssignabilityCheck
        User.get(id)
    }

    User getUserByEmail(String email) {
        if (email) {
            return User.findByEmail(email)
        }
        return null
    }

    User getUser(String email, String password) {
        final User user = getUserByEmail(email)
        if (user) {
            def passwordHash = hashPassword(password, email)
            if (passwordHash == user.password) {
                return user
            }
        }

        return null
    }

    /**
     * Gets the list of all users.
     * NOTE: should be used very carefully.
     *
     * @return the list of all users.
     */
    List<User> getUsers() {
        User.findAll()
    }

    private String hashPassword(String password, String salt) {
        return (password + salt).encodeAsSHA1()
    }

    boolean freeEmail(String email) {
        return User.countByEmail(email) == 0
    }
}
