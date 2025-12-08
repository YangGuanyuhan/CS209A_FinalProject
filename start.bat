@echo off
REM CS209A Final Project - Quick Start Script
REM This script will build and run the Spring Boot application

echo ========================================
echo CS209A Final Project - Stack Overflow Java Analysis
echo ========================================
echo.

echo [1/3] Cleaning previous build...
call mvnw.cmd clean
if errorlevel 1 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Building project...
call mvnw.cmd install -DskipTests
if errorlevel 1 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Starting application...
echo.
echo Application will start at: http://localhost:8080
echo.
echo Press Ctrl+C to stop the application
echo.

call mvnw.cmd spring-boot:run
