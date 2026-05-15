@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo ========================================
echo    BLOOD BUILD SYSTEM - ALL-IN-ONE
echo ========================================
echo.

:: ============================================
:: 1. Build Java Injectable Jar
:: ============================================
echo [1/5] Building InjectableJar...
cd libMujina\InjectableJar\InjectableJar
call gradle-8.10\bin\gradle.bat build --no-daemon
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java build failed
    cd ..\..\..
    pause
    exit /b %ERRORLEVEL%
)
cd ..\..\..
echo [OK] Java build successful
echo.

:: ============================================
:: 2. Sync Jar to C++ Header
:: ============================================
echo [2/5] Syncing Jar to C++ header...
powershell -ExecutionPolicy Bypass -File libMujina\update_jar.ps1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Sync failed
    pause
    exit /b %ERRORLEVEL%
)
echo [OK] Sync successful
echo.

:: ============================================
:: 3. Build C++ DLL
:: ============================================
echo [3/5] Compiling libMujina.dll...
if not exist build (
    mkdir build
    cmake -B build
)
cmake --build build --config Release
if %ERRORLEVEL% neq 0 (
    echo [ERROR] DLL compilation failed
    pause
    exit /b %ERRORLEVEL%
)
echo [OK] DLL compilation successful
echo.

:: ============================================
:: 4. Embed DLL as resource
:: ============================================
echo [4/5] Embedding DLL into loader...

if not exist "build\Release\libMujina.dll" (
    echo [ERROR] libMujina.dll not found at build\Release\libMujina.dll
    pause
    exit /b 1
)

if not exist loader mkdir loader

echo Converting DLL to Base64...
powershell -Command "$bytes = [System.IO.File]::ReadAllBytes('build\Release\libMujina.dll'); $base64 = [Convert]::ToBase64String($bytes); Set-Content -Path 'loader\EmbeddedDll.txt' -Value $base64 -NoNewline"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to embed DLL
    pause
    exit /b %ERRORLEVEL%
)

for %%A in (loader\EmbeddedDll.txt) do set EMBED_SIZE=%%~zA
echo [OK] DLL embedded as Base64 resource (!EMBED_SIZE! bytes)
echo.

:: ============================================
:: 5. Build standalone loader (FAST METHOD)
:: ============================================
echo [5/5] Building standalone BloodLoader.exe...

where dotnet >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] .NET SDK not found!
    pause
    exit /b 1
)

cd loader

:: Auto-detect the .cs file
set CS_FILE=
if exist BloodLoader.cs (
    set CS_FILE=BloodLoader.cs
    echo [INFO] Found BloodLoader.cs
) else if exist HeroLoader.cs (
    set CS_FILE=HeroLoader.cs
    echo [INFO] Found HeroLoader.cs
) else (
    echo [ERROR] No loader .cs file found
    cd ..
    pause
    exit /b 1
)

echo [INFO] Using direct compilation method (faster, no NuGet hang)
echo.

:: Create output directory
if not exist ..\build\Standalone mkdir ..\build\Standalone

:: Method 1: Try ILCompiler (AOT) if available - FAST
echo [STEP 1] Attempting AOT compilation...
dotnet publish -c Release -r win-x64 /p:PublishAot=true /p:StripSymbols=true -o ..\build\Standalone >nul 2>&1

if exist ..\build\Standalone\BloodLoader.exe (
    echo [OK] AOT compilation successful (native code, no dependencies^)
    goto :build_success
)

:: Method 2: Single-file publish with timeout
echo [STEP 2] Attempting single-file publish (60 second timeout^)...
start /B "" dotnet publish -c Release -r win-x64 --self-contained -p:PublishSingleFile=true -o ..\build\Standalone

:: Wait up to 60 seconds
set TIMEOUT=60
:wait_loop
timeout /t 1 /nobreak >nul
set /a TIMEOUT-=1
if exist ..\build\Standalone\BloodLoader.exe (
    taskkill /F /IM dotnet.exe >nul 2>&1
    echo [OK] Single-file publish successful
    goto :build_success
)
if !TIMEOUT! leq 0 (
    taskkill /F /IM dotnet.exe >nul 2>&1
    echo [WARN] Publish timed out, trying fallback method...
    goto :fallback_method
)
goto :wait_loop

:fallback_method
:: Method 3: PowerShell compilation (guaranteed to work)
echo [STEP 3] Using PowerShell compiler (fallback method^)...

powershell -Command "$code = Get-Content '%CS_FILE%' -Raw; $base64 = Get-Content 'EmbeddedDll.txt' -Raw; Add-Type -TypeDefinition $code -OutputAssembly '..\build\Standalone\BloodLoader.exe' -ReferencedAssemblies @('System.dll','System.Core.dll','System.Management.dll','System.IO.Compression.dll','System.IO.Compression.FileSystem.dll') -CompilerParameters @{GenerateExecutable=$true; CompilerOptions='/platform:x64 /optimize+'} 2>&1"

if exist ..\build\Standalone\BloodLoader.exe (
    echo [OK] PowerShell compilation successful
    goto :build_success
) else (
    echo [ERROR] All compilation methods failed
    cd ..
    pause
    exit /b 1
)

:build_success
cd ..
echo.
echo [OK] Build successful
echo.

:: ============================================
:: Build Summary
:: ============================================
echo ========================================
echo    BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.

if exist "build\Standalone\BloodLoader.exe" (
    for %%A in (build\Standalone\BloodLoader.exe) do set EXE_SIZE=%%~zA
    set /a EXE_MB=!EXE_SIZE! / 1048576
    echo Output: build\Standalone\BloodLoader.exe
    echo Size: !EXE_MB! MB (!EXE_SIZE! bytes)
    echo.
    echo ▄▄▄▄    ██▓     ▒█████   ▒█████  ▓█████▄ 
    echo ▓█████▄ ▓██▒    ▒██▒  ██▒▒██▒  ██▒▒██▀ ██▌
    echo ▒██▒ ▄██▒██░    ▒██░  ██▒▒██░  ██▒░██   █▌
    echo ▒██░█▀  ▒██░    ▒██   ██░▒██   ██░░▓█▄   ▌
    echo ░▓█  ▀█▓░██████▒░ ████▓▒░░ ████▓▒░░▒████▓ 
    echo.
    echo Ready to use!
    echo.
) else (
    echo [ERROR] Build failed - executable not found
)

echo ========================================
pause