package com.khmlabs.synctab.tab.condition

import com.khmlabs.synctab.Tag
import com.khmlabs.synctab.User

/**
 * The conditions used to get the list of recently added tabs.
 */
class RecentTabConditions extends TabConditions {

    final int max

    RecentTabConditions(User user, Tag tag, int max) {
        super(user, tag)
        this.max = max
    }

}
