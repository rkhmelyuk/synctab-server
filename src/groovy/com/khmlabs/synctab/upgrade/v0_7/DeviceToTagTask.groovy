package com.khmlabs.synctab.upgrade.v0_7

import com.khmlabs.synctab.upgrade.UpgradeTask
import org.springframework.context.ApplicationContext
import com.khmlabs.synctab.*

/**
 * Convert device information to tag information,
 * and remove device field for shared tabs.
 */
class DeviceToTagTask implements UpgradeTask {

    boolean upgrade(ApplicationContext appContext) {
        TagService tagService = (TagService) appContext.getBean("tagService")
        UserService userService = (UserService) appContext.getBean("userService")
        SharedTabService sharedTabService = (SharedTabService) appContext.getBean("sharedTabService")

        List<User> users = userService.getUsers()
        for (User user : users) {
            Tag androidTag = getUserAndroidTags(tagService, user)
            List<SharedTab> tabs = sharedTabService.getSharedTabs(user)
            for (SharedTab tab : tabs) {
                tab.tag = androidTag
                if (!sharedTabService.saveTab(tab)) {
                    return false
                }
            }
        }

        return true
    }

    private Tag getUserAndroidTags(TagService tagService, User user) {
        List<Tag> tags = tagService.getTags(user)
        tags.find { it.name == 'Android'}
    }

}
