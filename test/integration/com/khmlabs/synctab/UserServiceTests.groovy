package com.khmlabs.synctab

import com.khmlabs.synctab.tag.DefaultTags

/**
 * Tests for {@link UserService}
 */
class UserServiceTests extends GroovyTestCase {

    TagService tagService
    UserService userService

    void tearDown() {
        super.tearDown()

        removeUsers()
    }

    private void removeUsers() {
        List<User> users = userService.getUsers()
        for (User each : users) {
            each.delete()
        }
    }

    void testRegisterUser() {
        User user = new User()
        user.email = "test-register-user@synctabapp.com"

        assertTrue userService.registerUser(user, "password")

        assertNotNull user.id
        assertNotNull user.created
        assertNotNull user.password
        assertTrue user.active

        List<Tag> tags = tagService.getTags(user)

        assertNotNull tags
        assertEquals DefaultTags.COUNT, tags.size()
    }

    void testGetUserByEmail() {
        User user1 = new User(email: "test-get-user-by-email-1@synctabapp.com")
        User user2 = new User(email: "test-get-user-by-email-2@synctabapp.com")

        assertTrue userService.registerUser(user1, "password")
        assertTrue userService.registerUser(user2, "password")

        assertNotNull userService.getUserByEmail(user1.email).id
        assertNotNull userService.getUserByEmail(user2.email).id
        assertNull userService.getUserByEmail("not-existing-user-email@example.com")
    }

    void testGetUser() {
        User user = new User(email: "test-get-user@synctabapp.com")

        assertTrue userService.registerUser(user, "password")

        assertNotNull userService.getUser(user.email, "password")
        assertNull userService.getUser(user.email, "password1")
        assertNull userService.getUser("test_get_user@synctabapp.com", "password")
    }

    void testChangePassword() {
        User user = new User(email: "test-change-password@synctabapp.com")

        assertTrue userService.registerUser(user, "password")

        User found = userService.getUser(user.email, "password")
        assertNotNull found

        userService.changePassword(found, "parole")
        assertNull userService.getUser(user.email, "password")
        assertNotNull userService.getUser(user.email, "parole")

    }

    void testFreeEmail() {
        assertTrue userService.freeEmail("test-free-email@synctabapp.com")

        // register a new user with specified password
        User user = new User(email: "test-free-email@synctabapp.com")
        assertTrue userService.registerUser(user, "password")

        // check that registered password is not free now
        assertFalse userService.freeEmail("test-free-email@synctabapp.com")

    }
}
