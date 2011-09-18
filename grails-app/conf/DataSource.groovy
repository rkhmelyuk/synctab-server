environments {
    development {
        grails {
            mongo {
                host = "localhost"
                port = 27017
                databaseName = "synctab"
            }
        }
    }
    test {
        mongo {
            host = "localhost"
            port = 27017
            databaseName = "synctab_test"
        }
    }
    production { }
}
