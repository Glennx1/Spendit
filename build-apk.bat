@echo off
setlocal
echo ========================================================
echo SpendSplit - Native Android Personal Finance Tracker
echo ========================================================

if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Java\jdk-21.0.10" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
    )
)

if "%ANDROID_HOME%"=="" (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
    )
)

echo JAVA_HOME: %JAVA_HOME%
echo ANDROID_HOME: %ANDROID_HOME%
echo.

set BUILD_TYPE=debug
if /i "%1"=="release" set BUILD_TYPE=release

echo Building SpendSplit (%BUILD_TYPE% APK)...
call gradlew.bat assemble%BUILD_TYPE:~0,1%%BUILD_TYPE:~1%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo BUILD SUCCESSFUL!
    if "%BUILD_TYPE%"=="debug" (
        echo APK Path: app\build\outputs\apk\debug\app-debug.apk
    ) else (
        echo APK Path: app\build\outputs\apk\release\app-release-unsigned.apk
    )
    echo ========================================================
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%.
)

endlocal
