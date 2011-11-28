package com.khmlabs.synctab

import com.khmlabs.synctab.tag.DefaultTags

/**
 * Tests for {@link TagService}
 */
class TagServiceTests extends GroovyTestCase {

    TagService tagService
    UserService userService

    User user

    void setUp() {
        super.setUp()

        user = new User()
        user.email = "tag-service-tests@synctabapp.com"
        userService.registerUser(user, "password")
    }

    void testAddTag() {
        Tag tag = new Tag()
        tag.user = user
        tag.name = "Test"

        assertTrue tagService.addTag(tag)

        Tag found = tagService.getTagById(tag.id)

        assertNotNull found
        assertEquals user.id, found.user.id
        assertEquals "Test", found.name
        assertEquals tag.created, found.created
    }

    void testRenameTag() {
        Tag tag = new Tag()
        tag.user = user
        tag.name = "Test"

        assertTrue tagService.addTag(tag)

        Tag found = tagService.getTagById(tag.id)

        assertNotNull found
        assertEquals "Test", found.name

        tagService.renameTag(found, "Johny")

        Tag found2 = tagService.getTagById(tag.id)

        assertNotNull found2
        assertEquals "Johny", found2.name
    }

    void testRemoveTag() {
        Tag tag = new Tag()
        tag.user = user
        tag.name = "Test"

        assertTrue tagService.addTag(tag)

        Tag found = tagService.getTagById(tag.id)

        assertNotNull found
        assertEquals "Test", found.name

        assertTrue tagService.removeTag(found)

        Tag found2 = tagService.getTagById(tag.id)

        assertNull found2
    }

    void testFindUserTags() {
        User user = new User()
        user.email = "test-find-user-tags@synctabapp.com"
        user.created = System.currentTimeMillis()
        user.active = true
        user.password = "password"

        assertNotNull user.save()

        Tag tag1 = new Tag(user: user, name: "tag1")
        Tag tag2 = new Tag(user: user, name: "tag2")
        Tag tag3 = new Tag(user: user, name: "tag3")

        assertTrue tagService.addTag(tag1)
        assertTrue tagService.addTag(tag2)
        assertTrue tagService.addTag(tag3)

        List<Tag> tags = tagService.getTags(user)

        assertNotNull tags
        assertEquals 3, tags.size()
        assertTrue tags.any { it.name == "tag1" }
        assertTrue tags.any { it.name == "tag2" }
        assertTrue tags.any { it.name == "tag3" }
    }

    void testAddDefaultTags() {
        User user = new User()
        user.email = "test-add-defult-tags@synctabapp.com"
        user.created = System.currentTimeMillis()
        user.active = true
        user.password = "password"

        assertNotNull user.save()

        tagService.addDefaultTags(user)
        List<Tag> tags = tagService.getTags(user)

        assertNotNull tags
        assertEquals DefaultTags.COUNT, tags.size()
        assertTrue tags.any { it.name == DefaultTags.CHROME }
        assertTrue tags.any { it.name == DefaultTags.ANDROID }
        assertTrue tags.any { it.name == DefaultTags.HOME }
        assertTrue tags.any { it.name == DefaultTags.WORK }
    }

}
