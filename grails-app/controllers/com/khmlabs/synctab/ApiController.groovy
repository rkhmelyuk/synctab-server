package com.khmlabs.synctab

import grails.converters.JSON

class ApiController {

    SharedTabService sharedTabService

    def shareTab = {
        if (request.method != 'POST') {
            response.sendError 405
            return
        }

        def tab = new SharedTab()
        tab.title = params.title
        tab.link = params.link
        tab.device = params.device
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
        for (SharedTab each : tabs) {
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
