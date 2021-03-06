package com.khmlabs.synctab

import com.khmlabs.synctab.tab.condition.AfterTabConditions
import com.khmlabs.synctab.tab.condition.BeforeTabConditions
import com.khmlabs.synctab.tab.condition.RecentTabConditions
import com.khmlabs.synctab.tab.condition.TabsPageConditions
import grails.converters.JSON

class ApiController {

    private static final int LAST_TABS_NUM = 20
    private static final int RECENT_DAYS_NUM = 1

    TagService tagService
    AuthService authService
    UserService userService
    SharedTabService sharedTabService

    def beforeInterceptor = [
            action: this.&auth,
            except: ['register', 'authorize', 'resetPassword']]

    /**
     * The interceptor for API, that requires an authToken.
     * This interceptor finds a user by authToken and share it via
     * <code>session.user</code> variable.
     * <br/>
     * If authToken is invalid or absent, this interceptor returns 401 code.
     * In this case clients must invalidate their current session, logout and show a login screen.
     *
     * @return whether to continue request processing.
     */
    boolean auth() {
        if (log.traceEnabled) {
            log.trace("API: $actionName $params")
        }

        def token = params.token
        def user = authService.getUserByToken(token)
        if (!user) {
            response.sendError 401
            return false
        }

        session.user = user

        return true
    }

    /**
     * Register new user with specified email and password.
     * Both email and password are required, email should be unique and must have a correct format.
     *
     * Action returns a json object with two values:
     *  - status: "true" if registered, otherwise "false".
     *  - message: the error message, should be used when status is "false", to show an error message to the user.
     */
    def register = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        final String email = params.email?.trim()
        final String password = params.password?.trim()

        String msg = ''
        boolean status = false
        if (!email) {
            msg = g.message(code: 'user.email.blank')
        }
        else if (!Util.isEmail(email)) {
            msg = g.message(code: 'user.email.email.invalid')
        }
        else if (!userService.isEmailFree(email)) {
            msg = g.message(code: 'user.email.duplicate')
        }
        else if (!password) {
            msg = g.message(code: 'user.password.blank')
        }
        else {
            def user = new User()
            user.email = email
            status = userService.registerUser(user, password)
        }

        render([status: status, message: msg] as JSON)
    }

    /**
     * Authorize user email and password. Clients call this method on login.
     * When user email and password are valid, a new authToken is created for user
     * and returned to the client as token.
     *
     * @see com.khmlabs.synctab.AuthToken
     *
     * Returns:
     *  - status: "true" is authorized, otherwise "false"
     *  - token if status is "true".
     */
    def authorize = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        final String email = params.email?.trim()
        final String password = params.password?.trim()

        boolean status = false
        String tokenKey = null

        final User user = userService.getUser(email, password)
        if (user != null && user.active) {
            AuthToken token = authService.auth(user)
            if (token) {
                status = true
                tokenKey = token.token
            }
        }
        render([status: status, token: tokenKey] as JSON)
    }

    /**
     * Discards authToken, that was used by client.
     * This operation should be called by client application, when user choose to logout
     * or client session was invalidated.
     *
     * Returns:
     *  - status: "true" if logout successfully, otherwise "false".
     */
    def logout = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        final String tokenKey = params.token

        boolean status = false
        if (tokenKey) {
            AuthToken token = authService.getAuthToken(tokenKey)
            status = authService.discard(token)
        }
        render([status: status] as JSON)
    }

    /**
     * Gets the user email address and send a reset password link to it.
     *
     * Returns:
     *  - status: "true" if password reset email was sent successfully, otherwise "false".
     */
    def resetPassword = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        final String email = params.email

        boolean status = false
        if (email) {
            status = userService.sendPasswordResetEmail(email)
        }
        render([status: status] as JSON)
    }

    // --------------------------------------------------------------
    // Tabs
    // --------------------------------------------------------------

    /**
     * Share a tab.
     *
     * Returns:
     *  - status: "true" if was shared, otherwise "false".
     */
    def shareTab = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        def tab = new SharedTab()
        tab.user = session.user
        tab.title = params.title?.trim()
        tab.link = params.link?.trim()
        tab.device = params.device?.trim()
        tab.date = new Date()
        tab.tag = getTagFromRequest()

        def status = sharedTabService.addSharedTab(tab) != null
        render([status: status] as JSON)
    }

    /**
     * Remove the tab by id. User can remove only own tabs.
     *
     * Returns:
     *  - status: "true" if was removed, otherwise "false".
     */
    def removeTab = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        String id = params.id
        if (id == null) {
            response.sendError 400
            return
        }

        boolean status = true
        SharedTab tab = sharedTabService.getSharedTab(id)
        if (tab != null) {
            if (tab.user.id != session.user.id) {
                response.sendError 404
                return
            }

            try {
                sharedTabService.remove(tab)
            }
            catch (Exception e) {
                status = false
            }
        }

        render([status: status] as JSON)
    }

    def reshareTab = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        String id = params.id
        if (id == null) {
            response.sendError 400
            return
        }

        boolean status = false
        SharedTab tab = sharedTabService.getSharedTab(id)
        if (tab != null) {
            if (tab.user.id != session.user.id) {
                response.sendError 404
                return
            }

            try {
                status = sharedTabService.reshare(tab)
            }
            catch (Exception e) {
                status = false
            }
        }

        render([status: status] as JSON)
    }

    /**
     * Returns the list of last available tabs.
     * Client can pass a "tabId" parameter with an id of the tab to get tags for.
     *
     * Returns:
     *  - status: "true" if was removed, otherwise "false".
     *  - tabs: an array of found tabs.
     */
    def getLastTabs = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        User user = session.user
        Tag tag = getTagFromRequest()
        RecentTabConditions conditions = new RecentTabConditions(user, tag, LAST_TABS_NUM)
        List<SharedTab> tabs = sharedTabService.getRecentSharedTabs(conditions)

        render([status: true, tabs: prepareTabs(tabs)] as JSON)
    }

    /**
     * Returns the page with tabs.
     *
     * Input:
     *  - first: index of first element on page, 0 based
     *  - limit: number of tabs to return
     *
     * Returns:
     *  - status: "true" if was removed, otherwise "false".
     *  - tabs: an array of found tabs.
     */
    def getTabs = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        final Long first = params.long('first')
        final Long limit = params.long('limit')
        if (first == null || limit == null) {
            response.sendError 400
            return
        }

        User user = session.user
        Tag tag = getTagFromRequest()
        TabsPageConditions conditions = new TabsPageConditions(user, tag, first, limit)
        List<SharedTab> tabs = sharedTabService.getSharedTabs(conditions)
        int count = sharedTabService.getSharedTabsCount(conditions)

        render([status: true, tabs: prepareTabs(tabs), count: count] as JSON)
    }

    /**
     * Returns the list of tabs after specified date or after specified tab.
     * Client can pass a "tabId" parameter with an id of the tab to get tags for.
     *
     * Returns:
     *  - status: "true" if was removed, otherwise "false".
     *  - tabs: an array of found tabs.
     */
    def getTabsAfter = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        final String id = params.id
        final Long timestamp = params.long('ts')
        if (id == null && timestamp == null) {
            response.sendError 400
            return
        }

        User user = session.user
        Tag tag = getTagFromRequest()
        Date date = getDate(id, timestamp)
        AfterTabConditions conditions = new AfterTabConditions(user, tag, date)
        List<SharedTab> tabs = sharedTabService.getSharedTabsAfter(conditions)

        render([status: true, tabs: prepareTabs(tabs)] as JSON)
    }

    /**
     * Returns the list of tabs before specified tab.
     * Client can pass a "tabId" parameter with an id of the tab to get tags for.
     *
     * Used to get the next page of shared tabs.
     *
     * Returns:
     *  - status: "true" if was removed, otherwise "false".
     *  - tabs: an array of found tabs.
     */
    def getTabsBefore = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        final String id = params.id
        if (id == null) {
            response.sendError 400
            return
        }

        int max = params.int('max') ?: 10

        User user = session.user
        final SharedTab sharedTab = sharedTabService.getSharedTab(id)

        final List<SharedTab> tabs
        if (sharedTab != null) {
            Date date = sharedTab.date
            Tag tag = getTagFromRequest()
            BeforeTabConditions conditions = new BeforeTabConditions(user, tag, date, max)
            tabs = sharedTabService.getSharedTabsBefore(conditions)
        }
        else {
            tabs = Collections.emptyList()
        }

        render([status: true, tabs: prepareTabs(tabs)] as JSON)
    }

    def getSharedTabs = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        final Long timestamp = params.long('ts')

        Date date = null
        if (timestamp != null) {
            date = new Date(timestamp)
        }
        if (date == null) {
            date = new Date() - RECENT_DAYS_NUM
        }

        User user = session.user
        Tag tag = getTagFromRequest()
        AfterTabConditions conditions = new AfterTabConditions(user, tag, date)
        List<SharedTab> tabs = sharedTabService.getSharedTabsAfter(conditions)

        render([status: true, tabs: prepareTabs(tabs)] as JSON)
    }

    private Date getDate(String id, Long timestamp) {
        final Date date = null
        if (id != null) {
            final SharedTab sharedTab = sharedTabService.getSharedTab(id)
            if (sharedTab != null) {
                date = sharedTab.date
            }
        }
        else if (timestamp) {
            date = new Date(timestamp)
        }

        if (date == null) {
            date = new Date() - RECENT_DAYS_NUM
        }
        return date
    }

    private List<Map> prepareTabs(List<SharedTab> tabs) {
        if (!tabs) {
            return Collections.emptyList()
        }

        def result = new ArrayList<Map>(tabs.size());
        for (SharedTab each: tabs) {
            def map = [
                    id: each.id,
                    title: each.title,
                    link: each.link,
                    device: each.device,
                    tag: each.tag?.id,
                    ts: each.date.time,
                    favicon: each.favicon
            ]

            result.add(map)
        }

        return result
    }

    /**
     * Gets the user tag from request.
     *
     * @return the found tag or null if not found.
     */
    private Tag getTagFromRequest() {
        final String tagId = params.tagId

        if (tagId) {
            final User user = session.user
            return tagService.getUserTagById(user, tagId)
        }

        return null
    }


    // -----------------------------------------------------------------------
    // Tags
    // -----------------------------------------------------------------------

    /**
     * Gets the list of user tags.
     */
    def getTags = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        final User user = session.user
        List<Tag> tags = tagService.getTags(user)

        render([status: true, tags: prepareTags(tags)] as JSON)
    }

    /**
     * Add new user tag.
     * Accepts only 'name' parameter.
     *
     * Returns:
     *  - status: "true" if was added, otherwise "false".
     *  - id: the id of added tag, if it was added.
     */
    def addTag = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        Tag tag = new Tag()
        tag.user = session.user
        tag.name = params.name?.trim()

        boolean status = tagService.addTag(tag)
        render([status: status, id: tag.id] as JSON)
    }

    /**
     * Rename user tag.
     *
     * Accepts two parameters:
     *  - id: the id of the tag to rename,
     *  - name: the new tag name.
     *
     * Returns:
     *  - status: "true" if was renamed, otherwise "false".
     */
    def renameTag = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        Tag tag = getTagById()
        boolean status = false
        String name = params.name?.trim()

        if (tag && name) {
            status = tagService.renameTag(tag, name)
        }

        render([status: status] as JSON)
    }

    /**
     * Remove tag. No matter if it was used before or not.
     *
     * Returns:
     *  - status: "true" if was removed, otherwise "false".
     */
    def removeTag = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        Tag tag = getTagById()
        boolean status = false

        if (tag) {
            status = tagService.removeTag(tag)
        }

        render([status: status] as JSON)
    }

    private Tag getTagById() {
        final String tagId = params.id

        if (tagId) {
            final User user = session.user
            return tagService.getUserTagById(user, tagId)
        }

        return null
    }

    private List<Map> prepareTags(List<Tag> tags) {
        if (!tags) {
            return Collections.emptyList()
        }

        def result = new ArrayList<Map>(tags.size());
        for (Tag each: tags) {
            def map = [
                    id: each.id,
                    name: each.name,
                    ts: each.created.time
            ]

            result.add(map)
        }

        return result
    }
}
