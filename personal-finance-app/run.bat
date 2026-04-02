@echo off
echo Starting Personal Finance Application...
echo.
echo Make sure you have Java 17+ and Maven installed and in your PATH
echo.

REM Check if mvn is available
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven (mvn) is not found in PATH
    echo Please install Maven and add it to your PATH
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not found in PATH
    echo Please install Java 17+ and add it to your PATH
    pause
    exit /b 1
)

echo Cleaning and compiling...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot application...
echo The application will be available at: http://localhost:8080
echo H2 Console will be available at: http://localhost:8080/h2-console
echo.
echo Demo credentials:
echo Username: demo
echo Password: password123
echo.

call mvn spring-boot:run

pause
