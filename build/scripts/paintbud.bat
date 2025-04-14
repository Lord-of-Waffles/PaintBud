@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  paintbud startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and PAINTBUD_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Dio.ktor.development=false"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\paintbud-0.0.1.jar;%APP_HOME%\lib\ktor-server-netty-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-server-config-yaml-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-server-thymeleaf-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-server-content-negotiation-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-server-core-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-serialization-jackson-jvm-3.1.2.jar;%APP_HOME%\lib\jackson-databind-2.18.2.jar;%APP_HOME%\lib\firebase-admin-9.1.0.jar;%APP_HOME%\lib\google-cloud-storage-2.13.0.jar;%APP_HOME%\lib\jackson-core-2.18.2.jar;%APP_HOME%\lib\jackson-annotations-2.18.2.jar;%APP_HOME%\lib\jackson-module-kotlin-2.18.2.jar;%APP_HOME%\lib\kotlin-reflect-2.1.20.jar;%APP_HOME%\lib\ktor-http-cio-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-serialization-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-websockets-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-http-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-events-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-network-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-utils-jvm-3.1.2.jar;%APP_HOME%\lib\ktor-io-jvm-3.1.2.jar;%APP_HOME%\lib\kotlinx-coroutines-core-jvm-1.10.1.jar;%APP_HOME%\lib\yamlkt-jvm-0.13.0.jar;%APP_HOME%\lib\kotlinx-serialization-core-jvm-1.8.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.8.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.8.0.jar;%APP_HOME%\lib\kotlinx-io-core-jvm-0.6.0.jar;%APP_HOME%\lib\kotlinx-io-bytestring-jvm-0.6.0.jar;%APP_HOME%\lib\kotlin-stdlib-2.1.20.jar;%APP_HOME%\lib\logback-classic-1.4.14.jar;%APP_HOME%\lib\annotations-23.0.0.jar;%APP_HOME%\lib\thymeleaf-3.1.3.RELEASE.jar;%APP_HOME%\lib\slf4j-api-2.0.16.jar;%APP_HOME%\lib\config-1.4.3.jar;%APP_HOME%\lib\jansi-2.4.1.jar;%APP_HOME%\lib\logback-core-1.4.14.jar;%APP_HOME%\lib\google-api-client-gson-2.0.0.jar;%APP_HOME%\lib\google-api-client-2.0.0.jar;%APP_HOME%\lib\google-cloud-firestore-3.6.0.jar;%APP_HOME%\lib\google-auth-library-oauth2-http-1.11.0.jar;%APP_HOME%\lib\google-oauth-client-1.34.1.jar;%APP_HOME%\lib\google-http-client-gson-1.42.2.jar;%APP_HOME%\lib\google-http-client-apache-v2-1.42.2.jar;%APP_HOME%\lib\google-http-client-1.42.2.jar;%APP_HOME%\lib\proto-google-cloud-firestore-bundle-v1-3.6.0.jar;%APP_HOME%\lib\api-common-2.2.1.jar;%APP_HOME%\lib\opencensus-contrib-http-util-0.31.1.jar;%APP_HOME%\lib\guava-31.1-jre.jar;%APP_HOME%\lib\netty-codec-http2-4.1.118.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.118.Final.jar;%APP_HOME%\lib\netty-handler-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-native-kqueue-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.1.118.Final.jar;%APP_HOME%\lib\netty-codec-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-classes-kqueue-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-classes-epoll-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.118.Final.jar;%APP_HOME%\lib\netty-transport-4.1.118.Final.jar;%APP_HOME%\lib\alpn-api-1.1.3.v20160715.jar;%APP_HOME%\lib\httpclient-4.5.13.jar;%APP_HOME%\lib\httpcore-4.4.15.jar;%APP_HOME%\lib\proto-google-cloud-firestore-v1-3.6.0.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\j2objc-annotations-1.3.jar;%APP_HOME%\lib\opencensus-api-0.31.1.jar;%APP_HOME%\lib\javax.annotation-api-1.3.2.jar;%APP_HOME%\lib\auto-value-annotations-1.9.jar;%APP_HOME%\lib\google-auth-library-credentials-1.11.0.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\checker-qual-3.25.0.jar;%APP_HOME%\lib\google-http-client-jackson2-1.42.2.jar;%APP_HOME%\lib\google-api-services-storage-v1-rev20220705-2.0.0.jar;%APP_HOME%\lib\gson-2.9.1.jar;%APP_HOME%\lib\google-cloud-core-2.8.20.jar;%APP_HOME%\lib\proto-google-common-protos-2.9.6.jar;%APP_HOME%\lib\google-cloud-core-http-2.8.20.jar;%APP_HOME%\lib\google-http-client-appengine-1.42.2.jar;%APP_HOME%\lib\gax-httpjson-0.104.2.jar;%APP_HOME%\lib\gax-2.19.2.jar;%APP_HOME%\lib\grpc-context-1.49.2.jar;%APP_HOME%\lib\proto-google-iam-v1-1.6.2.jar;%APP_HOME%\lib\protobuf-java-3.21.7.jar;%APP_HOME%\lib\protobuf-java-util-3.21.7.jar;%APP_HOME%\lib\threetenbp-1.6.2.jar;%APP_HOME%\lib\google-cloud-core-grpc-2.8.20.jar;%APP_HOME%\lib\grpc-core-1.49.2.jar;%APP_HOME%\lib\annotations-4.1.1.4.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.22.jar;%APP_HOME%\lib\perfmark-api-0.25.0.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.15.jar;%APP_HOME%\lib\opencensus-contrib-grpc-util-0.31.1.jar;%APP_HOME%\lib\grpc-protobuf-1.49.2.jar;%APP_HOME%\lib\grpc-protobuf-lite-1.49.2.jar;%APP_HOME%\lib\grpc-api-1.49.2.jar;%APP_HOME%\lib\error_prone_annotations-2.15.0.jar;%APP_HOME%\lib\gax-grpc-2.19.2.jar;%APP_HOME%\lib\grpc-alts-1.49.2.jar;%APP_HOME%\lib\grpc-grpclb-1.49.2.jar;%APP_HOME%\lib\conscrypt-openjdk-uber-2.5.2.jar;%APP_HOME%\lib\grpc-auth-1.49.2.jar;%APP_HOME%\lib\grpc-netty-shaded-1.49.2.jar;%APP_HOME%\lib\grpc-googleapis-1.49.2.jar;%APP_HOME%\lib\grpc-xds-1.49.2.jar;%APP_HOME%\lib\opencensus-proto-0.2.0.jar;%APP_HOME%\lib\grpc-services-1.49.2.jar;%APP_HOME%\lib\re2j-1.6.jar;%APP_HOME%\lib\grpc-stub-1.49.2.jar;%APP_HOME%\lib\netty-buffer-4.1.118.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.118.Final.jar;%APP_HOME%\lib\netty-common-4.1.118.Final.jar;%APP_HOME%\lib\ognl-3.3.4.jar;%APP_HOME%\lib\attoparser-2.0.7.RELEASE.jar;%APP_HOME%\lib\unbescape-1.1.6.RELEASE.jar;%APP_HOME%\lib\javassist-3.29.0-GA.jar


@rem Execute paintbud
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %PAINTBUD_OPTS%  -classpath "%CLASSPATH%" io.ktor.server.netty.EngineMain %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable PAINTBUD_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%PAINTBUD_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
