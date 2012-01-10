package com.khmlabs.synctab

import com.prutsoft.core.KeyGenerator
import grails.plugin.mail.MailService
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * Manages work with users.
 */
class UserService {

    static transactional = false

    TagService tagService
    AuthService authService
    MailService mailService
    SharedTabService sharedTabService
    GrailsApplication grailsApplication

    boolean registerUser(User user, String password) {
        if (user && password) {
            user.created = System.currentTimeMillis()
            user.active = true
            user.password = hashPassword(password, user.email)

            if (user.save(flush: true) != null) {
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
                user.passwordResetKey = ''
                user.password = hashPassword(newPassword, user.email)
                return user.save(flush: true) != null
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

    /**
     * Check if such email is not used yet.
     * @param email the email to check.
     * @return true if email isn't in the system yet.
     */
    boolean freeEmail(String email) {
        return User.countByEmail(email) == 0
    }

    /**
     * Sends a password reset email. If there is no such user with specified email, then nothing is send but false is returned.
     * @param email the email to send a password reset email to.
     * @return true if email was send, otherwise false.
     */
    boolean sendPasswordResetEmail(String email) {
        def user = getUserByEmail(email)
        if (user == null) {
            // if user by specified email is not found, then returns false
            return false
        }

        // generates a reset key
        user.passwordResetKey = generatePasswordResetKey()
        if (user.save(flush: true) == null) {
            return false
        }

        // and now sends an email

        def toAddress = user.email
        def fromAddress = grailsApplication.config.synctab.mail.resetPassword.from.toString()
        def subjectStr = grailsApplication.config.synctab.mail.resetPassword.subject.toString()

        mailService.sendMail {
            from fromAddress
            to toAddress
            subject subjectStr
            body view: '/mail/resetPassword', model: [user: user]
        }

        return true
    }

    User getUserByPasswordResetKey(String key) {
        User.findByPasswordResetKey(key)
    }

    private String generatePasswordResetKey() {
        KeyGenerator.generateSimpleKey(50)
    }

    private String hashPassword(String password, String salt) {
        return (password + salt).encodeAsSHA1()
    }
}
