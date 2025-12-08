# CS209A Final Project - Quick Start Script for PowerShell
# This script will build and run the Spring Boot application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CS209A Final Project - Stack Overflow Java Analysis" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/3] Cleaning previous build..." -ForegroundColor Yellow
& .\mvnw.cmd clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Clean failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "[2/3] Building project..." -ForegroundColor Yellow
& .\mvnw.cmd install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "[3/3] Starting application..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Application will start at: http://localhost:8080" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C to stop the application" -ForegroundColor Cyan
Write-Host ""

& .\mvnw.cmd spring-boot:run
