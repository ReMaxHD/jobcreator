@echo off

set /p env="Enter environment (test / productive): "

if exist postcommithook (
	rmdir /S /Q postcommithook
)

call mvn clean install -Ppolarion

mkdir postcommithook

mkdir postcommithook\input
mkdir postcommithook\templates
copy install\templates\ postcommithook\templates\
copy target\jenkins-jobcreator-1.0-SNAPSHOT-postcommithook.jar postcommithook\jenkins-jobcreator.jar
copy jobcreator-config-polarion.xml postcommithook\jobcreator-config-polarion.xml
copy polarion-integration-config.xml postcommithook\polarion-integration-config.xml
copy env\environment-%env%.properties postcommithook\environment.properties

REM if this is not executed the unit tests will fail with a method not found exception
call mvn clean install eclipse:eclipse