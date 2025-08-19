@echo off
echo UG Navigate - Setup and Compilation Script
echo ==========================================

echo.
echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Java is not installed or not in PATH
    echo Please install Java 17 or later from: https://adoptium.net/
    echo After installation, restart this script
    pause
    exit /b 1
)

echo Java found!
echo.

echo Checking Maven installation...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Maven is not installed or not in PATH
    echo Installing Maven...
    
    echo Downloading Maven...
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.zip' -OutFile 'maven.zip'"
    
    echo Extracting Maven...
    powershell -Command "Expand-Archive -Path 'maven.zip' -DestinationPath '.' -Force"
    
    echo Setting up Maven...
    set MAVEN_HOME=%CD%\apache-maven-3.9.5
    set PATH=%MAVEN_HOME%\bin;%PATH%
    
    echo Maven installed to %MAVEN_HOME%
    echo Please add %MAVEN_HOME%\bin to your PATH environment variable
    echo.
)

echo Compiling project...
mvn clean compile

if %errorlevel% equ 0 (
    echo.
    echo Compilation successful!
    echo.
    echo Running the application...
    mvn javafx:run
) else (
    echo.
    echo Compilation failed. Please check the errors above.
    pause
)
