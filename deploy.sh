#!/bin/sh

grails clean
rm -rf target out
grails compile
grails war ROOT.war

scp ROOT.war website:.
ssh website "~/deploy-synctab.sh"
