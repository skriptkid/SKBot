@ECHO OFF

SETLOCAL
SET name=SKBOT
TITLE %name% Remover
ECHO.
ECHO Please close all instances of %name% before continuing.
ECHO Note: this removes ALL account information, screenshots and scripts.
CHOICE /m "Remove all %name% files from your system?"
IF ERRORLEVEL 2 GOTO :end
IF ERRORLEVEL 1 GOTO remove
:end
ECHO.
PAUSE
GOTO :eof

:remove
ECHO.

ECHO Removing accounts files...
IF EXIST "%APPDATA%\%name%_Accounts.ini" DEL "%APPDATA%\%name%_Accounts.ini"
IF EXIST "%APPDATA%\%name% Accounts.ini" DEL "%APPDATA%\%name% Accounts.ini"

ECHO Removing program directory...
IF EXIST "%SKBOT_HOME%" RMDIR /S /Q "%SKBOT_HOME%"
FOR /F "tokens=3" %%G IN ('REG QUERY "HKCU\Software\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders" /v "Personal"') DO (SET docs=%%G)
IF EXIST "%docs%\%name%" RMDIR /S /Q "%docs%\%name%"

ECHO Completed.
GOTO :end