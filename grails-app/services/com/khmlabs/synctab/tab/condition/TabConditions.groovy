package com.khmlabs.synctab.tab.condition

import com.khmlabs.synctab.Tag
import com.khmlabs.synctab.User
import grails.gorm.CriteriaBuilder

/**
 * The conditions used to get the list of tabs.
 */
abstract class TabConditions {

    final User user
    final Tag tag

    TabConditions(User user, Tag tag) {
        this.tag = tag
        this.user = user
    }

    void fillCriteria(CriteriaBuilder builder) {
        //builder {
            if (user) {
                builder.eq 'user', user
            }
            if (tag) {
                builder.eq 'tag', tag
            }
        //}
    }
}
