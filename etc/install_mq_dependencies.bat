@echo off
rem -------------------------------------------------------------------------
rem Add MQ dependencies to Maven Repository
rem -------------------------------------------------------------------------

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd %DIRNAME%
echo %CD%

call mvn install:install-file -Dfile=com.ibm.mq.commonservices.jar -DgroupId=com.ibm.mq -DartifactId=commonservices -Dversion=1.0 -Dpackaging=jar

call mvn install:install-file -Dfile=com.ibm.mq.headers.jar -DgroupId=com.ibm.mq -DartifactId=headers -Dversion=7.5.0.1 -Dpackaging=jar

call mvn install:install-file -Dfile=com.ibm.mq.jar -DgroupId=com.ibm.mq -DartifactId=mq -Dversion=7.5.0.1 -Dpackaging=jar

call mvn install:install-file -Dfile=com.ibm.mq.jmqi.jar -DgroupId=com.ibm.mq -DartifactId=jmqi -Dversion=7.5.0.1 -Dpackaging=jar

rem call mvn install:install-file -Dfile=com.ibm.mq.jmqi.local.jar -DgroupId=com.ibm.mq.jmqi -DartifactId=local -Dversion=7.5.0.0 -Dpackaging=jar
rem call mvn install:install-file -Dfile=com.ibm.mq.jmqi.remote.jar -DgroupId=com.ibm.mq.jmqi -DartifactId=remote -Dversion=7.5.0.0 -Dpackaging=jar
rem call mvn install:install-file -Dfile=com.ibm.mq.jmqi.system.jar -DgroupId=com.ibm.mq.jmqi -DartifactId=system -Dversion=7.5.0.0 -Dpackaging=jar
rem call mvn install:install-file -Dfile=com.ibm.mq.jms.admin.jar -DgroupId=com.ibm.mq.jms -DartifactId=admin -Dversion=7.5.0.0 -Dpackaging=jar

call mvn install:install-file -Dfile=com.ibm.mq.pcf.jar -DgroupId=com.ibm.mq -DartifactId=pcf -Dversion=7.5.0.1 -Dpackaging=jar

popd
