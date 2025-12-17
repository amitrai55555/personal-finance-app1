# Personal Finance App - Start Both Frontend and Backend
Write-Host "Starting Personal Finance App - Frontend and Backend" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green

# Start Backend
Write-Host "`nStarting Backend Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'D:\JAVA\personal-finance-app'; mvn spring-boot:run" -WindowStyle Normal

# Wait for backend to start
Write-Host "`nWaiting 15 seconds for backend to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Start Frontend
Write-Host "`nStarting Frontend Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'D:\webdev\CascadeProjects\windsurf-project'; python -m http.server 3000" -WindowStyle Normal

# Wait a moment then open browser
Write-Host "`nWaiting 5 seconds then opening application..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host "`nServers Started Successfully!" -ForegroundColor Green
Write-Host "Backend: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:3000" -ForegroundColor Cyan
Write-Host "`nOpening application in browser..." -ForegroundColor Yellow

# Open the application
Start-Process "http://localhost:3000"

Write-Host "`nPress any key to exit this script (servers will continue running)..." -ForegroundColor Magenta
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
