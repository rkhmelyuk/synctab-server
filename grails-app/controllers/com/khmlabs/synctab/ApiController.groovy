package com.khmlabs.synctab

import grails.converters.JSON

class ApiController {

    AuthService authService
    UserService userService
    SharedTabService sharedTabService

    def beforeInterceptor = [
            action: this.&auth,
            except: ['register', 'authorize']]

    boolean auth() {
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
        if (email && password) {
            def user = new User()
            user.email = params.email?.trim()
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

    def shareTab = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        def tab = new SharedTab()
        tab.title = params.title?.trim()
        tab.link = params.link?.trim()
        tab.device = params.device?.trim()
        tab.date = new Date()

        def status = sharedTabService.addSharedTab(tab)
        render([status: status] as JSON)
    }

    def getSharedTabs = {
        if (request.method != 'GET') {
            response.sendError 405
            return
        }

        def timestamp = params.long('since')
        if (timestamp == null) {
            response.sendError 400
            return
        }

        def date = new Date(timestamp)
        def tabs = sharedTabService.getSharedTabs(date)

        render(prepareTabs(tabs) as JSON)
    }

    private List prepareTabs(List<SharedTab> tabs) {
        if (!tabs) {
            return Collections.emptyList()
        }

        def result = new ArrayList<Map>(tabs.size());
        for (SharedTab each: tabs) {
            def map = [
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
