import com.khmlabs.synctab.User
import com.khmlabs.synctab.UserService
import com.khmlabs.synctab.upgrade.VersionUpgrade
import grails.util.GrailsUtil
import org.codehaus.groovy.grails.commons.GrailsApplication

class BootStrap {

    UserService userService
    GrailsApplication grailsApplication

    def init = { servletContext ->
        User user = userService.getUserByEmail('ruslan@khmelyuk.com')
        if (user == null) {
            user = new User()
            user.email = 'ruslan@khmelyuk.com'
            userService.registerUser(user, 'gop2ca')
        }

        switch (GrailsUtil.environment) {
            case 'production':
                // upgrade to the newer version if any
                String version = grailsApplication.metadata['app.version']
                new VersionUpgrade(grailsApplication.mainContext).upgradeToVersion(version)
                break
            case 'staging': case 'development':
                new VersionUpgrade(grailsApplication.mainContext).upgrade()
                break
            case 'test':
                break
        }
    }

    def destroy = {
    }
}
