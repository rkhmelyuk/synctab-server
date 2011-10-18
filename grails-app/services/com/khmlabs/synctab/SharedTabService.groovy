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
            sharedTab.favicon = urlInfo.favicon
        }
        catch (Exception e) {
            log.error("Error to retrieve and save details about $sharedTab.link", e)
        }
    }

    List<SharedTab> getSharedTabsAfter(User user, Date since) {
        SharedTab.findAllByUserAndDateGreaterThan(user, since)
    }

    List<SharedTab> getSharedTabsBefore(User user, Date since, int max) {
        SharedTab.findAllByUserAndDateLessThan(user, since, [max: max])
    }

    List<SharedTab> getLastSharedTabs(User user, int max) {
        // FIXME - Next line is not working
        // TODO - need to get count, and the offset,
        // SharedTab.findAllByUser(user, [max: max, sort: 'created', order: 'desc'])

        def users = SharedTab.findAllByUser(user)
        int size = users.size()
        if (size > max) {
            return new ArrayList<SharedTab>(users.subList(size - max, size))
        }
        return users
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
