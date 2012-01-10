<html>
  <head>
	  <title>Internal Error</title>
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <link type="text/css" rel="stylesheet" href="<g:resource dir="css" file="styles.css"/>"/>

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
  </head>

  <body>
    <div id="content">
        <div id="formContent">
            <h1>Internal Error</h1>
            Please submit issue using form at <a href="http://synctab.khmelyuk.com/submit-issue">http://synctab.khmelyuk.com/submit-issue</a>.
        </div>
    </div>
  </body>
</html>