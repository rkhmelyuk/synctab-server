package com.khmlabs.synctab

import com.khmlabs.synctab.tag.DefaultTags

/**
 * The service to work with Tags.
 *
 * @author Ruslan Khmelyuk
 */
class TagService {

    static transactional = false

    /**
     * Add new user tag.
     * @param tag the tag to add.
     * @return true if was added successfully.
     */
    boolean addTag(Tag tag) {
        if (tag) {
            tag.created = new Date()
            return saveTag(tag)
        }

        return false
    }

    /**
     * Rename the tag.
     *
     * @param tag the tag to rename.
     * @param newName the new tag name; renamed.
     * @return true if renamed, false if failed to rename.
     */
    boolean renameTag(Tag tag, String newName) {
        if (tag && newName) {
            tag.name = newName
            return saveTag(tag)
        }

        return false
    }

    /**
     * Gets the list of user tags.
     * @param user the user to get tags for.
     * @return the list of user tags.
     */
    List<Tag> getTags(User user) {
        if (user) {
            return Tag.findAllByUser(user)
        }

        return Collections.emptyList()
    }

    /**
     * Gets the tag by its id.
     * @param id the id to find tag by.
     * @return the found tag or null.
     */
    Tag getTagById(String id) {
        //noinspection GroovyAssignabilityCheck
        Tag.get(id)
    }

    /**
     * Add the default tags for specified user.
     *
     * @param user the user to add tags for.
     */
    void addDefaultTags(User user) {
        addTag(new Tag(user: user, name: DefaultTags.CHROME))
        addTag(new Tag(user: user, name: DefaultTags.ANDROID))
        addTag(new Tag(user: user, name: DefaultTags.HOME))
        addTag(new Tag(user: user, name: DefaultTags.WORK))
    }

    private boolean saveTag(Tag tag) {
        return tag.save(flush: true) != null
    }
}
