from subprocess import call, DEVNULL
from distutils.dir_util import copy_tree as copydir
from urllib.request import urlretrieve as download
from inspect import cleandoc as format
from shutil import copyfile, rmtree as removedir
from os import mkdir, rename, remove as removefile

print("Generating runtime")
jlinkCommand = (r"jlink --module-path .;..\lib\app "
                       "--output ./TimeTable/ "
                       "--add-modules java.desktop "
                       "--add-modules java.logging "
                       "--add-modules java.sql "
                       "--no-man-pages "
                       "--no-header-files "
        			   "--compress=2")

call(jlinkCommand)

libdir = "./TimeTable/lib/app"
mkdir(libdir)
print("Downloading libraries")

download("https://repo1.maven.org/maven2/org/eclipse/yasson/1.0.5/yasson-1.0.5.jar", f"{libdir}/yasson-1.0.5.jar")
download("https://repo1.maven.org/maven2/jakarta/json/jakarta.json-api/1.1.5/jakarta.json-api-1.1.5.jar", f"{libdir}/jakarta.json-api-1.1.5.jar")
download("https://repo1.maven.org/maven2/org/glassfish/jakarta.json/1.1.5/jakarta.json-1.1.5-module.jar", f"{libdir}/jakarta.json-1.1.5-module.jar")
download("https://repo1.maven.org/maven2/jakarta/json/bind/jakarta.json.bind-api/1.0.1/jakarta.json.bind-api-1.0.1.jar", f"{libdir}/jakarta.json.bind-api-1.0.1.jar")

print("Creating jar file")
call(f"javac -d compile --module-path {libdir}/yasson-1.0.5.jar;{libdir}/jakarta.json-1.1.5-module.jar;{libdir}/jakarta.json.bind-api-1.0.1.jar;{libdir}/jakarta.json-api-1.1.5.jar src/timetable/*.java src/timetable/listeners/*.java src/module-info.java")
copydir("src/assets", "compile/assets")

manifest = """Main-Class: timetable.Main
              Class-Path: lib/app/jakarta.json.bind-api-1.0.1.jar lib/app/jakarta.json-1.1.5-module.jar lib/app/jakarta.json-api-1.1.5.jar lib/app/yasson-1.0.5.jar"""

with open("Manifest.txt", "w") as manifestFile:
    manifestFile.write(format(manifest) + "\n")
    
call("jar cfm TimeTable.jar Manifest.txt -C compile assets -C compile timetable -C compile module-info.class")
rename("TimeTable.jar", "./TimeTable/TimeTable.jar")
removefile("Manifest.txt")
removedir("compile")

copyfile("icon.ico", "./TimeTable/icon.ico")

print("Creating Shortcut Creator")
call("pyinstaller shortcut.py --onefile", stderr = DEVNULL, stdin = DEVNULL)
rename("./dist/shortcut.exe", "./TimeTable/CreateShortcut.exe")
removefile("shortcut.spec")
removedir("build")
removedir("dist")

print("Done")