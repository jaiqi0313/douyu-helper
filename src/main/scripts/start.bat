@echo off

echo %JAVA_HOME%

if "%JAVA_HOME%" == "" (
   echo Can not find JAVA_HOME, Warnning! try to use java
   set JAVA="java"
) else (
   set JAVA="%JAVA_HOME%\bin\java"
)

REM meta home directory
set app_home="%~dp0.."

set CLASSPATH="%CLASSPATH%;%app_home%\lib\*"

echo on


%JAVA% -cp %CLASSPATH% io.github.hengyunabc.douyuhelper.HelperMain

