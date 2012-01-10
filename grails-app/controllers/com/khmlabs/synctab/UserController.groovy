package com.khmlabs.synctab

class UserController {

    UserService userService

    def resetPassword = {
        def resetKey = params.id
        def user = userService.getUserByPasswordResetKey(resetKey)
        if (user == null) {
            return [status: 'error', msg: 'Reset link is incorrect or password is changed already.']
        }

        if (request.method == 'POST') {
            def newPassword = params.newPassword
            def confirmPassword = params.confirmPassword

            if (!newPassword) {
                return [status: 'form', msg: 'New Password is required']
            }
            else if (newPassword != confirmPassword) {
                return [status: 'form', msg: 'Password Confirmation is not the same as New Password']
            }

            if (userService.changePassword(user, newPassword)){
                return [status: 'info', msg: 'Your Password was changed successfully!']
            }

            return [status: 'error', msg: 'Sorry, but we failed to change your password.']
        }

        return [status: 'form']
    }
}
