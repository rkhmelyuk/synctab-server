package com.khmlabs.synctab

import com.khmlabs.synctab.helper.UrlInfoRetriever
import com.khmlabs.synctab.helper.UrlInfo

class SharedTabService {

    static transactional = false

    MemcachedService memcachedService

    boolean addSharedTab(SharedTab sharedTab) {
        def tab = sharedTab
        fillLinkDetails(tab)

        // search for such tab, if so, just update the date to now
        def found = findSuchSharedTab(tab)
        if (found) {
            found.date = new Date()
            found.title = tab.title
            tab = found
        }

        return saveTab(tab)
    }

    private SharedTab findSuchSharedTab(SharedTab tab) {
        SharedTab.findByUserAndLink(tab.user, tab.link)
    }

    private void fillLinkDetails(SharedTab sharedTab) {
        if (sharedTab.title) return

        try {
            final String cacheKey = 'urlInfo_' + Util.hash(sharedTab.link)
            UrlInfo urlInfo = (UrlInfo) memcachedService.getObject(cacheKey)
            if (!urlInfo) {
                def retriever = new UrlInfoRetriever(sharedTab.link)
                urlInfo = retriever.retrieve()
                memcachedService.putObject(cacheKey, urlInfo)
            }

            sharedTab.link = urlInfo.link
            sharedTab.title = urlInfo.title
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
