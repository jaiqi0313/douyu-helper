@echo off

setlocal

if "%JAVA_HOME%" == "" (
   echo Plase set JAVA_HOME at first
   pause
   exit)

set JAVA="%JAVA_HOME%\bin\java"

REM meta home directory
set app_home="%~dp0.."

set CLASSPATH="%CLASSPATH%;%app_home%\lib\*"

echo on

%JAVA% -cp %CLASSPATH% io.github.hengyunabc.douyuhelper.HelperMain

endlocal


