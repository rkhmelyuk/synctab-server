package com.khmlabs.synctab

import com.google.common.collect.Lists
import com.khmlabs.synctab.helper.PageInfoRetriever
import com.khmlabs.synctab.helper.UrlInfo
import com.khmlabs.synctab.tab.condition.AfterTabConditions
import com.khmlabs.synctab.tab.condition.BeforeTabConditions
import com.khmlabs.synctab.tab.condition.RecentTabConditions
import com.khmlabs.synctab.tab.condition.TabsPageConditions
import com.khmlabs.synctab.tab.condition.TabConditions

/**
 * Manage work with SharedTabs.
 *
 * @author Ruslan Khmelyuk
 */
class SharedTabService {

    static transactional = false

    MemcachedService memcachedService

    SharedTab addSharedTab(SharedTab sharedTab) {
        def tab = sharedTab
        fillLinkDetails(tab)

        // search for such tab, if so, just update the date to now
        def found = findSuchSharedTab(tab)
        if (found) {
            found.date = new Date()
            found.title = tab.title
            found.link = tab.link
            found.device = tab.device
            found.favicon = tab.favicon
            found.tag = tab.tag
            tab = found
        }

        return (saveTab(tab) ? tab : null)
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
            UrlInfo urlInfo = (UrlInfo) memcachedService.getObject(cacheKey)
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
     * Gets the list of shared tabs.
     *
     * @param conditions the search conditions.
     * @return the list of found shared tabs, if nothing is found than list is empty.
     */
    List<SharedTab> getSharedTabs(TabsPageConditions conditions) {
        def builder = SharedTab.createCriteria()
        return builder.list(max: (int) conditions.limit, offset: (int) conditions.first) {
            conditions.fillCriteria(builder)
        }
    }

    /**
     * Gets the list of shared tabs since specified date for specified user.
     *
     * @param conditions the search conditions.
     * @return the list of found shared tabs, if nothing is found than list is empty.
     */
    List<SharedTab> getSharedTabsAfter(AfterTabConditions conditions) {
        findByConditions(conditions)
    }

    /**
     * Get n shared tabs before specified date for specified user.
     *
     * @param conditions the search conditions.
     * @return the list of found shared tabs, if nothing is found then list is empty.
     */
    List<SharedTab> getSharedTabsBefore(BeforeTabConditions conditions) {
        findByConditions(conditions)
    }

    /**
     * Get n recent shared tabs for user.
     *
     * @param conditions the search conditions.
     * @return the list of found recent tabs, if nothing found then list is empty.
     */
    List<SharedTab> getRecentSharedTabs(RecentTabConditions conditions) {
        List<SharedTab> tabs = findByConditions(conditions)

        int size = tabs.size()
        int max = conditions.max
        if (size > max) {
            tabs = tabs.subList(size - max, size)
            return Lists.newArrayList(tabs)
        }
        return tabs
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

    /**
     * Update the existing tab.
     * @param tab the tab to update.
     * @return true if tab was updated, otherwise false.
     */
    boolean updateTab(SharedTab tab) {
        if (!tab.id) {
            return false
        }

        return saveTab(tab)
    }

    /**
     * Gets the list of all shared tabs for specified user.
     * @param user the user to get shared tabs for.
     * @return the list of user shared tabs.
     */
    List<SharedTab> getSharedTabs(User user) {
        SharedTab.findAllByUser(user)
    }

    private boolean saveTab(SharedTab tab) {
        return tab.save(flush: true) != null
    }

    private List<SharedTab> findByConditions(TabConditions conditions) {
        def builder = SharedTab.createCriteria()
        return builder.listDistinct {
            conditions.fillCriteria(builder)
        }
    }
}
