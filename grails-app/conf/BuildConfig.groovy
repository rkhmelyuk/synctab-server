grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {

    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        excludes 'guava'
    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()

        mavenRepo "http://files.couchbase.com/maven2/"
    }

    dependencies {

        runtime 'net.sourceforge.htmlcleaner:htmlcleaner:2.2'
        runtime 'com.google.code.gson:gson:2.2.2'
        runtime 'spy:spymemcached:2.8.2'
    }
}
