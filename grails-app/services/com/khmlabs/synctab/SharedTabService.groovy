package com.khmlabs.synctab

import com.khmlabs.synctab.helper.UrlInfoRetriever

class SharedTabService {

    static transactional = false

    boolean addSharedTab(SharedTab tab) {
        // TODO - don't add duplicate links, just refresh date of existing one
        fillLinkDetails(tab)
        return saveTab(tab)
    }

    private void fillLinkDetails(SharedTab sharedTab) {
        if (sharedTab.title) return

        try {
            def retriever = new UrlInfoRetriever(sharedTab.link)
            retriever.retrieve()

            sharedTab.title = retriever.title
            sharedTab.link = retriever.realUrl
        }
        catch (Exception e) {
            log.error("Error to retrieve and save details about $sharedTab.link", e)
        }
    }

    List<SharedTab> getSharedTabsSince(User user, Date since) {
        SharedTab.findAllByUserAndDateGreaterThan(user, since)
    }

    SharedTab getSharedTab(String id) {
        //noinspection GroovyAssignabilityCheck
        return SharedTab.get(id)
    }

    void remove(SharedTab tab) {
        if (tab) {
            tab.delete(flush: true)
        }
    }

    boolean reshare(SharedTab tab) {
        if (tab) {
            tab.date = new Date()
            return saveTab(tab)
        }
        return false
    }

    private boolean saveTab(SharedTab tab) {
        return tab.save(flush: true) != null
    }
}
