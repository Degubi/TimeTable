@echo off

echo Generating app runtime...
jlink --module-path .;.\lib\app ^
--output ./runtime/ ^
--add-modules degubi.timetable ^
--add-modules jdk.crypto.ec ^
--no-header-files ^
--no-man-pages ^
--compress=2 ^
--strip-debug

set runtimeFolder=%~dp0runtime

copy tray.ico %runtimeFolder%

"C:\Program Files\7-Zip\7z.exe" a first.zip .\runtime\lib
"C:\Program Files\7-Zip\7z.exe" a second.zip .\runtime\bin .\runtime\conf .\runtime\legal .\runtime\release .\runtime\run.bat .\runtime\tray.ico
"C:\Program Files\7-Zip\7z.exe" a appLib.zip .\lib\app\*

rmdir /S /Q %runtimeFolder%

echo Build done...
pause