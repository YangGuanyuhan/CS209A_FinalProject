# CS209A Final Project - API Test Script
# Run this after the application starts to test all REST endpoints
# 注意：运行此脚本前需要先运行数据收集器，确保 stackoverflow_data.json 文件存在

$baseUrl = "http://localhost:8080"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing REST API Endpoints" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Initialize data from JSON file
Write-Host "[Test 1] POST /api/init - Load data from stackoverflow_data.json" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/init" -Method POST
    Write-Host "✓ Success: " -ForegroundColor Green -NoNewline
    Write-Host "$($response.message)"
    Write-Host "  Data count: $($response.collected)" -ForegroundColor Gray
}
catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
    Write-Host "  请确保 stackoverflow_data.json 文件存在" -ForegroundColor Yellow
}
Write-Host ""

# Wait for data to be ready
Start-Sleep -Seconds 2

# Test 2: Get statistics
Write-Host "[Test 2] GET /api/stats - Get statistics" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/stats"
    Write-Host "✓ Success" -ForegroundColor Green
    Write-Host "  Total Questions: $($response.totalQuestions)" -ForegroundColor Gray
    Write-Host "  Total Answers: $($response.totalAnswers)" -ForegroundColor Gray
    Write-Host "  Average Score: $([math]::Round($response.avgScore, 2))" -ForegroundColor Gray
}
catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: Get topic trends
Write-Host "[Test 3] GET /api/trends?years=3 - Topic trends" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/trends?years=3"
    Write-Host "✓ Success" -ForegroundColor Green
    $topicCount = $response.topicTrends.PSObject.Properties.Count
    Write-Host "  Topics analyzed: $topicCount" -ForegroundColor Gray
}
catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: Get co-occurrence
Write-Host "[Test 4] GET /api/cooccurrence?topN=10 - Topic co-occurrence" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/cooccurrence?topN=10"
    Write-Host "✓ Success" -ForegroundColor Green
    Write-Host "  Top pairs found: $($response.topPairs.Count)" -ForegroundColor Gray
    if ($response.topPairs.Count -gt 0) {
        Write-Host "  Example: $($response.topPairs[0].topics) - $($response.topPairs[0].count) times" -ForegroundColor Gray
    }
}
catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 5: Get pitfalls
Write-Host "[Test 5] GET /api/pitfalls?topN=8 - Multithreading pitfalls" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/pitfalls?topN=8"
    Write-Host "✓ Success" -ForegroundColor Green
    Write-Host "  Pitfalls found: $($response.topPitfalls.Count)" -ForegroundColor Gray
    Write-Host "  Total MT questions: $($response.totalMultithreadingQuestions)" -ForegroundColor Gray
}
catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 6: Get solvability
Write-Host "[Test 6] GET /api/solvability - Solvability analysis" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/solvability"
    Write-Host "✓ Success" -ForegroundColor Green
    Write-Host "  Solvable questions: $($response.solvableCount)" -ForegroundColor Gray
    Write-Host "  Hard-to-solve questions: $($response.hardToSolveCount)" -ForegroundColor Gray
    Write-Host "  Factors analyzed: $($response.factors.PSObject.Properties.Count)" -ForegroundColor Gray
}
catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 7: Get questions list
Write-Host "[Test 7] GET /api/questions?limit=5 - Get questions" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/questions?limit=5"
    Write-Host "✓ Success" -ForegroundColor Green
    Write-Host "  Questions returned: $($response.Count)" -ForegroundColor Gray
}
catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "All tests completed!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "You can now open http://localhost:8080 in your browser" -ForegroundColor Green
