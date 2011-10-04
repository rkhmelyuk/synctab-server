package com.khmlabs.synctab

import grails.converters.JSON

class ApiController {

    private static final int RECENT_DAYS_NUM = 2

    AuthService authService
    UserService userService
    SharedTabService sharedTabService

    def beforeInterceptor = [
            action: this.&auth,
            except: ['register', 'authorize']]

    boolean auth() {
        println "Executing $actionName"

        def token = params.token
        def user = authService.getUserByToken(token)
        if (!user) {
            response.sendError 401
            return false
        }

        session.user = user

        return true
    }

    def register = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        def email = params.email?.trim()
        def password = params.password?.trim()

        def status = false
        if (email && password && userService.freeEmail(email)) {
            def user = new User()
            user.email = email
            status = userService.registerUser(user, password)
        }

        render([status: status] as JSON)
    }

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

    def getSharedTabsSince = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        def timestamp = params.long('since')
        if (timestamp == null) {
            response.sendError 400
            return
        }

        final Date date
        if (timestamp == 0) {
            date = new Date() - RECENT_DAYS_NUM
        }
        else {
            date = new Date(timestamp)
        }

        def user = session.user
        def tabs = sharedTabService.getSharedTabsSince(user, date)

        render([status: true, tabs: prepareTabs(tabs)] as JSON)
    }

    def getSharedTabsAfter = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        String id = params.id
        if (id == null) {
            response.sendError 400
            return
        }

        final User user = session.user
        final SharedTab sharedTab = sharedTabService.getSharedTab(id)

        final Date date
        if (sharedTab != null) {
            date = sharedTab.date
        }
        else {
            date = new Date() - RECENT_DAYS_NUM
        }

        List<SharedTab> tabs = sharedTabService.getSharedTabsSince(user, date)
        render([status: true, tabs: prepareTabs(tabs)] as JSON)
    }

    private List prepareTabs(List<SharedTab> tabs) {
        if (!tabs) {
            return Collections.emptyList()
        }

        def result = new ArrayList<Map>(tabs.size());
        for (SharedTab each: tabs) {
            def map = [
                    id: each.id,
                    title: each.title ?: "",
                    link: each.link,
                    device: each.device,
                    ts: each.date.time
            ]

            result.add(map)
        }

        return result
    }
}
