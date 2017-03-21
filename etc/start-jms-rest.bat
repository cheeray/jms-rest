@echo off
rem -------------------------------------------------------------------------
rem Start JMS-Rest bridge
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

call java -jar -Dlog4j.configuration=file:conf\log4j.properties target\jms-rest-bridge.jar conf\jms-rest.yaml

popd

