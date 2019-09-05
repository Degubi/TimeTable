@echo off
set updateJar=false
set updateLib=false

if "%1" == "jar" (
	set updateJar=true
)

if "%1" == "lib" (
	set updateLib=true
	set updateJar=true
)

if not exist .\lib\app (
	set updateLib=true
)

if "%1" == "runtime" (
	echo Updating TimeTable Runtime
	timeout 5 >nul

	set updateJar=true
	set updateLib=true

	rmdir /S /Q bin
	rmdir /S /Q conf
	rmdir /S /Q legal
	rmdir /S /Q lib
	del release
	del tray.ico
	del TimeTable.jar

	echo Downloading Runtime
	powershell -Command "(New-Object Net.WebClient).DownloadFile('https://drive.google.com/uc?id=1STxdFTz6vj3TwpI7qvq9ejTGMueav5F4&export=download', 'first.zip')"
	powershell -Command "(New-Object Net.WebClient).DownloadFile('https://drive.google.com/uc?id=1Oho0rVHm-7NWn0kxxT2Ktd9hy8_Pp5jE&export=download', 'second.zip')"
	
	echo Extracting Runtime
	"C:\Program Files\7-Zip\7z.exe" x first.zip
	"C:\Program Files\7-Zip\7z.exe" x second.zip

	del first.zip
	del second.zip
)

if %updateLib% == true (
	echo Updating Libraries
	timeout 5 >nul
	
	del "lib\app\*.*?"
	
	powershell -Command "(New-Object Net.WebClient).DownloadFile('https://drive.google.com/uc?id=1IOucjzMV2X3T7dLj9epzMgFoNciiy6mG&export=download', 'appLib.zip')"
	"C:\Program Files\7-Zip\7z.exe" x appLib.zip "-olib\app"
	del appLib.zip
)

if %updateJar% == true (
	echo Updating TimeTable.jar
	timeout 5 >nul
	
	powershell -Command "(New-Object Net.WebClient).DownloadFile('https://drive.google.com/uc?id=1T1AcoX4HjPwyArnmyOeMoFFzBU2GcwsT&export=download', 'TimeTable.jar')"

	echo Starting TimeTable.jar
)

start bin\javaw -jar TimeTable.jar
exit