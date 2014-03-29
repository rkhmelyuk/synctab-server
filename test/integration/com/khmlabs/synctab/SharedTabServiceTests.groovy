package com.khmlabs.synctab

import com.khmlabs.synctab.tab.condition.AfterTabConditions
import com.khmlabs.synctab.tab.condition.BeforeTabConditions
import com.khmlabs.synctab.tag.DefaultTags
import com.khmlabs.synctab.tab.condition.RecentTabConditions
import com.khmlabs.synctab.tab.condition.TabsPageConditions

/**
 * Tests for {@link SharedTabService}
 */
class SharedTabServiceTests extends GroovyTestCase {

    TagService tagService
    UserService userService
    SharedTabService sharedTabService

    User user

    void setUp() {
        super.setUp()

        user = new User()
        user.email = "shared-tab-service-tests@synctabapp.com"
        userService.registerUser(user, "password")
    }

    void tearDown() {
        super.tearDown()

        List<SharedTab> tabs = SharedTab.findAll()
        for (SharedTab each : tabs) {
            each.delete()
        }
    }

    void testAddSharedTab() {
        SharedTab tab = new SharedTab()
        tab.link = "http://synctab.khmelyuk.com"
        tab.user = user
        tab.device = "Android"
        tab.date = new Date()

        tab = sharedTabService.addSharedTab(tab)

        assertNotNull tab
        assertEquals "http://synctab.khmelyuk.com", tab.link
        assertEquals user.id, tab.user.id

        assertNull tab.tag
        assertNotNull tab.date
    }

    void testAddSharedTabWithTag() {
        Tag androidTag = getTag(DefaultTags.ANDROID)

        SharedTab tab = new SharedTab()
        tab.link = "http://www.khmelyuk.com"
        tab.tag = androidTag
        tab.user = user
        tab.device = "Android"
        tab.date = new Date()

        tab = sharedTabService.addSharedTab(tab)

        assertNotNull tab
        assertNotNull tab.tag
        assertEquals androidTag.id, tab.tag.id
    }

    void testGetSharedTabsAfter() {
        createSharedTab("http://blog.khmelyuk.com", new Date() - 3)
        createSharedTab("http://synctab.khmelyuk.com", new Date())
        createSharedTab("http://www.khmelyuk.com", new Date())

        AfterTabConditions conditions = new AfterTabConditions(user, null, new Date() - 1)
        List<SharedTab> sharedTabs = sharedTabService.getSharedTabsAfter(conditions)

        assertNotNull sharedTabs
        assertEquals 2, sharedTabs.size()
        assertTrue sharedTabs.any { it.link == "http://synctab.khmelyuk.com" }
        assertTrue sharedTabs.any { it.link == "http://www.khmelyuk.com" }
    }

    /**
     * Test getting the list of shared tabs only for specified tag.
     */
    void testGetSharedTabsAfter_ByTag() {
        createSharedTab("http://blog.khmelyuk.com", new Date() - 3, DefaultTags.ANDROID)
        createSharedTab("http://synctab.khmelyuk.com", new Date(), DefaultTags.ANDROID)
        createSharedTab("http://www.khmelyuk.com", new Date(), DefaultTags.CHROME)

        Tag tag = getTag(DefaultTags.ANDROID)
        AfterTabConditions conditions = new AfterTabConditions(user, tag, new Date() - 1)
        List<SharedTab> sharedTabs = sharedTabService.getSharedTabsAfter(conditions)

        assertNotNull sharedTabs
        assertEquals 1, sharedTabs.size()
        assertTrue sharedTabs.any { it.link == "http://synctab.khmelyuk.com" }
    }

    void testGetSharedTabsBefore() {
        createSharedTab("http://blog.khmelyuk.com", new Date() - 3)
        createSharedTab("http://synctab.khmelyuk.com", new Date() - 2)
        createSharedTab("http://www.khmelyuk.com", new Date())

        BeforeTabConditions conditions = new BeforeTabConditions(user, null, new Date() - 1, 10)
        List<SharedTab> sharedTabs = sharedTabService.getSharedTabsBefore(conditions)

        assertNotNull sharedTabs
        assertEquals 2, sharedTabs.size()
        assertTrue sharedTabs.any { it.link == "http://synctab.khmelyuk.com" }
        assertTrue sharedTabs.any { it.link == "http://blog.khmelyuk.com" }
    }

    /**
     * Test getting the list of shared tabs only for specified tag.
     */
    void testGetSharedTabsBefore_ByTag() {
        createSharedTab("http://blog.khmelyuk.com", new Date() - 3, DefaultTags.ANDROID)
        createSharedTab("http://synctab.khmelyuk.com", new Date() - 2, DefaultTags.CHROME)
        createSharedTab("http://www.khmelyuk.com", new Date(), DefaultTags.ANDROID)

        Tag tag = getTag(DefaultTags.ANDROID)
        BeforeTabConditions conditions = new BeforeTabConditions(user, tag, new Date() - 1, 10)
        List<SharedTab> sharedTabs = sharedTabService.getSharedTabsBefore(conditions)

        assertNotNull sharedTabs
        assertEquals 1, sharedTabs.size()
        assertTrue sharedTabs.any { it.link == "http://blog.khmelyuk.com" }
    }

    /**
     * Check that max limit works correctly.
     */
    void testGetSharedTabsBefore_Limited() {
        createSharedTab("http://blog.khmelyuk.com", new Date() - 3)
        createSharedTab("http://synctab.khmelyuk.com", new Date() - 2)
        createSharedTab("http://www.khmelyuk.com", new Date())

        BeforeTabConditions conditions = new BeforeTabConditions(user, null, new Date() - 1, 1)
        List<SharedTab> sharedTabs = sharedTabService.getSharedTabsBefore(conditions)

        assertNotNull sharedTabs
        assertEquals 1, sharedTabs.size()
        assertTrue sharedTabs.any { it.link == "http://synctab.khmelyuk.com" }
    }

    /**
     * Check that pag condition works.
     */
    void testGetSharedTabsByPage() {
        createSharedTab("http://blog.khmelyuk.com", new Date() - 3)
        createSharedTab("http://synctab.khmelyuk.com", new Date() - 2)
        createSharedTab("http://www.khmelyuk.com", new Date())

        TabsPageConditions conditions = new TabsPageConditions(user, null, 0, 2)
        List<SharedTab> sharedTabs = sharedTabService.getSharedTabs(conditions)

        assertNotNull sharedTabs
        assertEquals 2, sharedTabs.size()
        assertNotNull sharedTabs.any { it.link == "http://synctab.khmelyuk.com" }
        assertNotNull sharedTabs.any { it.link == "http://www.khmelyuk.com" }
    }

    void testGetRecentTabs() {
        createSharedTab("http://blog.khmelyuk.com", new Date() - 3)
        createSharedTab("http://synctab.khmelyuk.com", new Date() - 2)
        createSharedTab("http://www.khmelyuk.com", new Date())

        RecentTabConditions conditions = new RecentTabConditions(user, null, 2)
        List<SharedTab> sharedTabs = sharedTabService.getRecentSharedTabs(conditions)

        assertNotNull sharedTabs
        assertEquals 2, sharedTabs.size()
        assertTrue sharedTabs.any { it.link == "http://synctab.khmelyuk.com" }
        assertTrue sharedTabs.any { it.link == "http://www.khmelyuk.com" }
    }

    void testGetRecentTabs_ByTag() {
        createSharedTab("http://blog.khmelyuk.com", new Date() - 3, DefaultTags.ANDROID)
        createSharedTab("http://synctab.khmelyuk.com", new Date() - 2, DefaultTags.ANDROID)
        createSharedTab("http://www.khmelyuk.com", new Date(), DefaultTags.CHROME)

        Tag tag = getTag(DefaultTags.ANDROID)
        RecentTabConditions conditions = new RecentTabConditions(user, tag, 2)
        List<SharedTab> sharedTabs = sharedTabService.getRecentSharedTabs(conditions)

        assertNotNull sharedTabs
        assertEquals 2, sharedTabs.size()
        assertTrue sharedTabs.any { it.link == "http://blog.khmelyuk.com" }
        assertTrue sharedTabs.any { it.link == "http://synctab.khmelyuk.com" }
    }

    private Tag getTag(String name) {
        List<Tag> tags = tagService.getTags(user)
        tags.find { it.name == name }
    }

    private void createSharedTab(String link, Date date, String tagName = DefaultTags.ANDROID) {
        SharedTab tab = new SharedTab()
        tab.link = link
        tab.tag = getTag(tagName)
        tab.device = "Chrome"
        tab.user = user
        tab.date = date

        sharedTabService.addSharedTab(tab)
    }
}
