# UG Navigate - Setup and Compilation Script
# =========================================

Write-Host "UG Navigate - Setup and Compilation Script" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

Write-Host "`nChecking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java found!" -ForegroundColor Green
    Write-Host $javaVersion[0] -ForegroundColor Cyan
} catch {
    Write-Host "Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 17 or later from: https://adoptium.net/" -ForegroundColor Yellow
    Write-Host "After installation, restart this script" -ForegroundColor Yellow
    Read-Host "Press Enter to continue"
    exit 1
}

Write-Host "`nChecking Maven installation..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1
    Write-Host "Maven found!" -ForegroundColor Green
    Write-Host $mavenVersion[0] -ForegroundColor Cyan
} catch {
    Write-Host "Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Installing Maven..." -ForegroundColor Yellow
    
    Write-Host "Downloading Maven..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.zip" -OutFile "maven.zip"
    
    Write-Host "Extracting Maven..." -ForegroundColor Yellow
    Expand-Archive -Path "maven.zip" -DestinationPath "." -Force
    
    Write-Host "Setting up Maven..." -ForegroundColor Yellow
    $env:MAVEN_HOME = "$PWD\apache-maven-3.9.5"
    $env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"
    
    Write-Host "Maven installed to $env:MAVEN_HOME" -ForegroundColor Green
    Write-Host "Please add $env:MAVEN_HOME\bin to your PATH environment variable" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "Compiling project..." -ForegroundColor Yellow
try {
    mvn clean compile
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nCompilation successful!" -ForegroundColor Green
        Write-Host "`nRunning the application..." -ForegroundColor Yellow
        mvn javafx:run
    } else {
        Write-Host "`nCompilation failed. Please check the errors above." -ForegroundColor Red
        Read-Host "Press Enter to continue"
    }
} catch {
    Write-Host "Error during compilation: $_" -ForegroundColor Red
    Read-Host "Press Enter to continue"
}
