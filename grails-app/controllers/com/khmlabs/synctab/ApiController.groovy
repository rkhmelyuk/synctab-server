package com.khmlabs.synctab

import grails.converters.JSON

class ApiController {

    private static final int LAST_TABS_NUM = 20
    private static final int RECENT_DAYS_NUM = 2

    TagService tagService
    AuthService authService
    UserService userService
    SharedTabService sharedTabService

    def beforeInterceptor = [
            action: this.&auth,
            except: ['register', 'authorize']]

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
        log.info "API: $actionName $params"
        println "API: $actionName $params"

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

        String msg = ""
        boolean status = false
        if (!email) {
            msg = g.message(code: 'user.email.blank')
        }
        else if (!Util.isEmail(email)) {
            msg = g.message(code: 'user.email.email.invalid')
        }
        else if (!userService.freeEmail(email)) {
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

        def status = sharedTabService.addSharedTab(tab)
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

        boolean status
        SharedTab tab = sharedTabService.getSharedTab(id)
        if (tab != null) {
            if (tab.user.id != session.user.id) {
                response.sendError 404
                return
            }

            try {
                sharedTabService.remove(tab)
                status = true
            }
            catch (Exception e) {
                status = false
            }
        }
        else {
            status = true
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

    def getLastTabs = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        def user = session.user
        def tabs = sharedTabService.getLastSharedTabs(user, LAST_TABS_NUM)

        render([status: true, tabs: prepareTabs(tabs)] as JSON)
    }

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
        Date date = getDate(id, timestamp)
        List<SharedTab> tabs = sharedTabService.getSharedTabsAfter(user, date)

        render([status: true, tabs: prepareTabs(tabs)] as JSON)
    }

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

        int max = params.int("max") ?: 10

        User user = session.user
        final SharedTab sharedTab = sharedTabService.getSharedTab(id)

        final List<SharedTab> tabs
        if (sharedTab != null) {
            Date date = sharedTab.date
            tabs = sharedTabService.getSharedTabsBefore(user, date, max)
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

        final User user = session.user
        List<SharedTab> tabs = sharedTabService.getSharedTabsAfter(user, date)

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
                    ts: each.date.time,
                    favicon: each.favicon
            ]

            result.add(map)
        }

        return result
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

        render([status: true, tabs: prepareTags(tags)] as JSON)
    }

    /**
     * Add new user tag.
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
        render([status: status] as JSON)
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
