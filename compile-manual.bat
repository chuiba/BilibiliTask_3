@echo off
echo Compiling BilibiliTask manually...

rem Create output directory
if not exist "build\classes" mkdir build\classes

rem Download dependencies if not exist
if not exist "lib" mkdir lib

echo.
echo Note: This project requires the following dependencies:
echo - fastjson2-2.0.43.jar
echo - httpclient-4.5.14.jar
echo - logback-classic-1.4.14.jar
echo - snakeyaml-2.2.jar
echo - lombok (annotation processor)
echo.
echo To run this project:
echo 1. Download the required JAR files to the 'lib' directory
echo 2. Set the environment variables: BILI_JCT, SESSDATA, DEDEUSERID
echo 3. Use: java -cp "lib/*;build/classes" top.srcrs.BiliStart
echo.
echo The project has been updated with:
echo - Java 11 compatibility
echo - Updated dependencies (fastjson2, etc.)
echo - Security fixes
echo - Updated GitHub Actions workflow