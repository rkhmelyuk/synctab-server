package com.khmlabs.synctab

class SharedTabService {

    static transactional = false

    boolean addSharedTab(SharedTab tab) {
        tab.save() != null
    }

    List<SharedTab> getSharedTabsSince(User user, Date since) {
        SharedTab.findAllByUserAndDateGreaterThan(user, since)
    }

    SharedTab getSharedTab(String id) {
        //noinspection GroovyAssignabilityCheck
        return SharedTab.get(id)
    }
}
