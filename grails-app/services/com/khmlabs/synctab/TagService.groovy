package com.khmlabs.synctab

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
        return Tag.findAllByUser(user)
    }

    /**
     * Add the default tags for specified user.
     *
     * @param user the user to add tags for.
     */
    void addDefaultTags(User user) {
        addTag(new Tag(user: user, name: 'Chrome'))
        addTag(new Tag(user: user, name: 'Android'))
        addTag(new Tag(user: user, name: 'Home'))
        addTag(new Tag(user: user, name: 'Work'))
    }

    private boolean saveTag(Tag tag) {
        return tag.save(flush: true) != null
    }
}
