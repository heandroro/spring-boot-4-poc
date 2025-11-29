@echo off
set DIRNAME=%~dp0
set CLASSPATH=%DIRNAME%gradle\wrapper\gradle-wrapper.jar
if not defined JAVA_HOME (
  set JAVA_CMD=java
) else (
  set JAVA_CMD=%JAVA_HOME%\bin\java
)
%JAVA_CMD% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
