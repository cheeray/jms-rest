@echo off
rem -------------------------------------------------------------------------
rem Publish JMS-Util to local Maven Repository
rem -------------------------------------------------------------------------

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd %DIRNAME%..
echo %CD%

call mvn install:install-file -Dfile=target\jms-util-ee.jar -DgroupId=com.cheeray -DartifactId=jms-util-ee -Dversion=1.1 -Dpackaging=jar

call mvn install:install-file -Dfile=target\jms-util-se.jar -DgroupId=com.cheeray -DartifactId=jms-util-se -Dversion=1.1 -Dpackaging=jar

popd

