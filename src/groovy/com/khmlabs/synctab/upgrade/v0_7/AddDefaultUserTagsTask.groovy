package com.khmlabs.synctab.upgrade.v0_7

import com.khmlabs.synctab.TagService
import com.khmlabs.synctab.User
import com.khmlabs.synctab.UserService
import com.khmlabs.synctab.upgrade.UpgradeTask
import org.springframework.context.ApplicationContext

/**
 * This task is responsible to add a default tags for each registered user.
 */
class AddDefaultUserTagsTask implements UpgradeTask {

    boolean upgrade(ApplicationContext appContext) {
        TagService tagService = (TagService) appContext.getBean("tagService")
        UserService userService = (UserService) appContext.getBean("userService")

        // add default tags for each user
        List<User> users = userService.getUsers()
        for (User each : users) {
            if (!tagService.addDefaultTags(each)) {
                return false
            }
        }

        return true
    }

}
