from subprocess import call
from distutils.dir_util import copy_tree as copydir
from urllib.request import urlretrieve as download
from inspect import cleandoc as format
from shutil import rmtree as removedir
from os import mkdir, rename, remove as removefile, devnull

print("Generating runtime")
jlinkCommand = (r"jlink --module-path .;..\lib\app "
                 "--output ./TimeTable/ "
                 "--add-modules java.desktop "
                 "--add-modules java.logging "
                 "--add-modules java.sql "
                 "--no-man-pages "
                 "--no-header-files")

call(jlinkCommand)

print("Downloading libraries")
mkdir("./TimeTable/lib/app")

download("https://repo1.maven.org/maven2/org/eclipse/yasson/1.0.5/yasson-1.0.5.jar", "./TimeTable/lib/app/yasson-1.0.5.jar")
download("https://repo1.maven.org/maven2/jakarta/json/jakarta.json-api/1.1.5/jakarta.json-api-1.1.5.jar", "./TimeTable/lib/app/jakarta.json-api-1.1.5.jar")
download("https://repo1.maven.org/maven2/org/glassfish/jakarta.json/1.1.5/jakarta.json-1.1.5-module.jar", "./TimeTable/lib/app/jakarta.json-1.1.5-module.jar")
download("https://repo1.maven.org/maven2/jakarta/json/bind/jakarta.json.bind-api/1.0.1/jakarta.json.bind-api-1.0.1.jar", "./TimeTable/lib/app/jakarta.json.bind-api-1.0.1.jar")

print("Creating jar file")
call("javac -d compile --module-path ./TimeTable/lib/app/yasson-1.0.5.jar;./TimeTable/lib/app/jakarta.json-1.1.5-module.jar;./TimeTable/lib/app/jakarta.json.bind-api-1.0.1.jar;./TimeTable/lib/app/jakarta.json-api-1.1.5.jar src/timetable/*.java src/module-info.java")
copydir("src/assets", "compile/assets")

manifest = """Main-Class: timetable.Main
              Class-Path: lib/app/jakarta.json.bind-api-1.0.1.jar lib/app/jakarta.json-1.1.5-module.jar lib/app/jakarta.json-api-1.1.5.jar lib/app/yasson-1.0.5.jar"""

with open("Manifest.txt", "w") as manifestFile:
    manifestFile.write(format(manifest) + "\n")
    
call("jar cfm TimeTable.jar Manifest.txt -C compile assets -C compile timetable -C compile module-info.class")
rename("TimeTable.jar", "./TimeTable/TimeTable.jar")
removefile("Manifest.txt")
removedir("compile")

print("Creating exe file")
call(r"c:\windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe /t:exe /win32icon:icon.ico /out:TimeTable.exe Runner.cs", stdout = open(devnull, 'w'))
rename("TimeTable.exe", "./TimeTable/TimeTable.exe")

print("Done")