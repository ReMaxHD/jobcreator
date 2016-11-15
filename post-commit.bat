@echo off	

REM Repository path
set REPOS=%1

REM Revision
set REV=%2

REM set jenkins jobcreator parameters
set CONFIG_FILE=D:\Swisslog\Jenkins_Jobcreator\polarion-integration-config.xml
set ENV_PROPERTIES=D:\Swisslog\Jenkins_Jobcreator\environment.properties
set JOBCREATOR_FOLDER=D:\Swisslog\Jenkins_Jobcreator

REM start jenkins jobcreator (asynchronously)
D:\Swisslog\PsTools\PsExec.exe /accepteula -d "%JAVA_HOME%/java.exe" -cp %JOBCREATOR_FOLDER%\jenkins-jobcreator.jar com.swisslog.jenkins.jobcreator.polarion.PostCommitHook %REPOS% %REV% %CONFIG_FILE% %ENV_PROPERTIES%

REM always exit with error code 0 (allow commit)
exit 0