package com.khmlabs.synctab

class SharedTabService {

    static transactional = false

    boolean addSharedTab(SharedTab tab) {
        tab.save() != null
    }

    List<SharedTab> getSharedTabs(Date since) {
        SharedTab.findAllByDateGreaterThan(since)
    }
}
