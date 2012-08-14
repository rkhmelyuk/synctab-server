#!/bin/sh

grails clean
rm -rf target out
grails compile
grails war oaproxy.war

scp oaproxy.war ubuntu@server:.
ssh ubuntu@synctabapp.khmelyuk.com "~/deploy-apps.sh"
