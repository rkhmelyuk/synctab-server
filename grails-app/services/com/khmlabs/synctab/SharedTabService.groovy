package com.khmlabs.synctab

import com.khmlabs.synctab.helper.PageInfoRetriever
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

    /**
     * Find user shared tab by specified link. Used to find already shared tab
     * with specified address.
     *
     * @param tab the shared tab to find similar to.
     * @return the found shared tab, if not found then null.
     */
    private SharedTab findSuchSharedTab(SharedTab tab) {
        SharedTab.findByUserAndLink(tab.user, tab.link)
    }

    /**
     * Fill links details, like title, favicon and real url.
     * @param sharedTab the shared tab to fill link details for.
     */
    private void fillLinkDetails(SharedTab sharedTab) {
        try {
            // Build a cache key, that starts with a prefix as a way to have namespace of values.
            // Hash of link is used, as cache key is limited to 256 chars but links can be way longer.
            final String cacheKey = 'urlInfo_' + Util.hash(sharedTab.link)
            UrlInfo urlInfo = null; // TODO - uncomment me (UrlInfo) memcachedService.getObject(cacheKey)
            if (!urlInfo) {
                // if not found in cache, then retrieve information from web
                def retriever = new PageInfoRetriever(sharedTab.link)
                urlInfo = retriever.retrieve()

                // and save into the cache
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

    /**
     * Gets the list of shared tabs since specified date for specified user.
     * @param user the user to get shared tabs for.
     * @param since the date to get shared tabs after.
     * @return the list of found shared tabs, if nothing is found than list is empty.
     */
    List<SharedTab> getSharedTabsAfter(User user, Date since) {
        SharedTab.findAllByUserAndDateGreaterThan(user, since)
    }

    /**
     * Get n shared tabs before specified date for specified user.
     *
     * @param user the user to get shared tabs for.
     * @param before the date to get shared tabs before.
     * @param max the max number of shared tabs to return.
     * @return the list of found shared tabs, if nothing is found then list is empty.
     */
    List<SharedTab> getSharedTabsBefore(User user, Date before, int max) {
        SharedTab.findAllByUserAndDateLessThan(user, before, [max: max])
    }

    /**
     * Get n recent shared tabs for user.
     *
     * @param user the user to get shared tabs for.
     * @param max the number of recent tabs to return.
     * @return the list of found recent tabs, if nothing found then list is empty.
     */
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

    /**
     * Get shared tab by id.
     *
     * @param id the id to get shared tab by.
     * @return the found SharedTab or null.
     */
    SharedTab getSharedTab(String id) {
        //noinspection GroovyAssignabilityCheck
        return SharedTab.get(id)
    }

    /**
     * Remove tab.
     *
     * @param tab the tab to remove;
     */
    void remove(SharedTab tab) {
        if (tab) {
            tab.delete(flush: true)
        }
    }

    /**
     * Reshare a tab: set tab datetime to now.
     *
     * @param tab the tab to reshare.
     * @return whether tab was reshared.
     */
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
