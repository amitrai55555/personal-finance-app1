@echo off
echo Starting Personal Finance App - Frontend and Backend
echo ==================================================

echo.
echo Starting Backend Server...
start "Backend Server" cmd /k "mvn spring-boot:run"

echo.
echo Waiting 10 seconds for backend to start...
timeout /t 10 /nobreak > nul

echo.
echo Starting Frontend Server...
cd /d "D:\webdev\CascadeProjects\windsurf-project"
start "Frontend Server" cmd /k "python -m http.server 3000"

echo.
echo Both servers are starting...
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000
echo.
echo Press any key to open the application in your browser...
pause > nul

start http://localhost:3000
