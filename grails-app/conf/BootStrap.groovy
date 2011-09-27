import com.khmlabs.synctab.UserService
import com.khmlabs.synctab.User

class BootStrap {

    UserService userService

    def init = { servletContext ->
        User user = userService.getUserByEmail("ruslan@khmelyuk.com")
        if (user == null) {
            user = new User()
            user.email = "ruslan@khmelyuk.com"
            userService.registerUser(user, "qwerty")
        }
    }

    def destroy = {
    }
}
