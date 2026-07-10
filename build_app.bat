@echo off
set JAVA_HOME=d:\Trial\.jdk_17\jdk-17.0.19+10
echo Using JAVA_HOME=%JAVA_HOME%
call .\gradlew.bat %*
