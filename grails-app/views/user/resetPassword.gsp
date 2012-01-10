<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>SyncTab: Reset your password</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1">
        <meta name="Keywords" content="android, browser, chrome, firefox, synctab, tab, links, bookmarks, synchronize, share, download, send links" />
        <meta name="Description" content="SyncTab is an Android application that sends the links to your desktop or laptop browser directly." />
        <meta name="Author" content="Ruslan Khmelyuk" />

        <link rel="shortcut icon" href="<g:resource dir="images" file="favicon.png"/>" type="image/png" />


        <link type="text/css" rel="stylesheet" href="<g:resource dir="css" file="styles.css"/>"/>
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        <script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/jquery.validate/1.9/jquery.validate.min.js"></script>

        <script type="text/javascript">
            <g:set var="gaTrackingCode" value="${grailsApplication.config.synctab.googleTrackingCode}"/>
            <g:if test="${gaTrackingCode}">
                var _gaq = _gaq || [];
                _gaq.push(["_setAccount", "${gaTrackingCode}"]);
                _gaq.push(["_trackPageview"]);
                (function() {
                  var ga = document.createElement("script"); ga.type = "text/javascript"; ga.async = true;
                  ga.src = ("https:" == document.location.protocol ? "https://ssl" : "http://www") + ".google-analytics.com/ga.js";
                  var s = document.getElementsByTagName("script")[0]; s.parentNode.insertBefore(ga, s);
                })();
            </g:if>
        </script>

        <script type="text/javascript">
            $(document).ready(function() {
                $("#resetPasswordForm").validate({
                    rules: {
                        newPassword: 'required',
                        confirmPassword: {
                            equalTo: '#newPassword'
                        }
                    }
                });
            })
        </script>

    </head>
    <body>
        <div id="content">
            <div id="formContent">
                <g:if test="${status == 'error'}">
                    <h1><span>SyncTab:</span> Sorry!</h1>
                    <div class="error">${msg}</div>
                </g:if>
                <g:elseif test="${status == 'info'}">
                    <h1><span>SyncTab:</span> Congratulations!</h1>
                    <div class="success">${msg}</div>
                </g:elseif>
                <g:elseif test="${status == 'form'}">
                    <h1><span>SyncTab:</span> Reset Password</h1>
                    <g:if test="${msg}"><div class="error">${msg}</div></g:if>
                    <g:form name="resetPasswordForm" action="resetPassword" method="POST" id="${params.id}">
                        <p>
                            <label for="newPassword">New Password</label>
                            <g:passwordField name="newPassword" maxlength="50" required="true" autofocus="true"/>
                        </p>

                        <p>
                            <label for="confirmPassword">Confirm New Password</label>
                            <g:passwordField name="confirmPassword" maxlength="50" required="true"/>
                        </p>

                        <p><input type="submit" value="Change Password" class="mainButton"/></p>
                        <div class="clear"></div>
                    </g:form>
                </g:elseif>
            </div>

            <div id="footer">
                <div id="left">Copyright &copy; 2012 <a href="http://www.khmelyuk.com">Ruslan Khmelyuk</a></div>
                <div class="clear"></div>
            </div>
        </div>
    </body>
</html>