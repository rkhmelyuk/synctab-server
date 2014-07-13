package com.khmlabs.synctab.tab.condition

import com.khmlabs.synctab.Tag
import com.khmlabs.synctab.User
import grails.gorm.CriteriaBuilder

/**
 * Conditions to get tabs page.
 */
class TabsPageConditions extends TabConditions {

    final long first
    final long limit

    TabsPageConditions(User user, Tag tag, long first, long limit) {
        super(user, tag)
        this.first = first
        this.limit = limit
    }

    @Override
    void fillCriteria(CriteriaBuilder builder) {
        super.fillCriteria(builder)
        builder.order("date", "desc")
    }

}
