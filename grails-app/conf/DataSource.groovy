environments {
    development {
        grails {
            mongo {
                host = "127.0.0.1"
                port = 27017
                databaseName = "synctab"
            }
        }
    }
    test {
        mongo {
            host = "127.0.0.1"
            port = 27017
            databaseName = "synctab_test"
        }
    }
    production { }
}
