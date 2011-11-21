package com.khmlabs.synctab.tab.condition

import com.khmlabs.synctab.Tag
import com.khmlabs.synctab.User
import grails.gorm.CriteriaBuilder

/**
 * Conditions to get tabs before some date.
 */
class BeforeTabConditions extends TabConditions {

    final Date before
    final int max

    BeforeTabConditions(User user, Tag tag, Date before, int max) {
        super(user, tag)
        this.max = max
        this.before = before
    }

    @Override
    void fillCriteria(CriteriaBuilder builder) {
        super.fillCriteria(builder)

        if (before) {
            builder.lt 'date', before
        }

        builder.maxResults(max)
    }
}
