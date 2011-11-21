package com.khmlabs.synctab.tab.condition

import com.khmlabs.synctab.Tag
import com.khmlabs.synctab.User
import grails.gorm.CriteriaBuilder

/**
 * Conditions to get tabs after some date.
 */
class AfterTabConditions extends TabConditions {

    final Date since

    AfterTabConditions(User user, Tag tag, Date since) {
        super(user, tag)
        this.since = since
    }

    @Override
    void fillCriteria(CriteriaBuilder builder) {
        super.fillCriteria(builder)

        if (since) {
            builder.gt 'date', since
        }
    }

}
