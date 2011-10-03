#!/bin/sh

#git pull origin

grails clean
rm -rf target out
grails compile
grails war ROOT.war

scp -i ~/.ssh/mailsight.pem ROOT.war ubuntu@synctabapp.khmelyuk.com:.
ssh -i ~/.ssh/mailsight.pem ubuntu@synctabapp.khmelyuk.com "~/deploy-synctab.sh"
