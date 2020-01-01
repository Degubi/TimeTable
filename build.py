from subprocess import call, DEVNULL
from distutils.dir_util import copy_tree as copydir
from urllib.request import urlretrieve as download
from inspect import cleandoc as format
from shutil import copyfile, rmtree as removedir
from os import mkdir, rename, path, environ, remove as removefile

print("Generating runtime")
jlinkCommand = (r"jlink --module-path .;..\lib\app "
                       "--output ./TimeTable/ "
                       "--add-modules jdk.charsets "
                       "--add-modules jdk.crypto.ec "
                       "--add-modules java.desktop "
                       "--add-modules java.logging "
                       "--add-modules java.net.http "
                       "--add-modules java.sql "
                       "--no-man-pages "
                       "--no-header-files "
                       "--compress=2")
call(jlinkCommand)

print("Downloading libraries")
mkdir(r"./TimeTable/lib/app")

libURLs = ["https://repo1.maven.org/maven2/org/eclipse/yasson/1.0.5/yasson-1.0.5.jar",
           "https://repo1.maven.org/maven2/jakarta/json/jakarta.json-api/1.1.5/jakarta.json-api-1.1.5.jar",
           "https://repo1.maven.org/maven2/org/glassfish/jakarta.json/1.1.5/jakarta.json-1.1.5-module.jar",
           "https://repo1.maven.org/maven2/jakarta/json/bind/jakarta.json.bind-api/1.0.1/jakarta.json.bind-api-1.0.1.jar",
           "https://repo1.maven.org/maven2/org/apache/poi/poi/4.1.1/poi-4.1.1.jar",
           "https://repo1.maven.org/maven2/commons-codec/commons-codec/1.13/commons-codec-1.13.jar",
           "https://repo1.maven.org/maven2/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar",
           "https://repo1.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar",
           "https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/4.1.1/poi-ooxml-4.1.1.jar",
           "https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml-schemas/4.1.1/poi-ooxml-schemas-4.1.1.jar",
           "https://repo1.maven.org/maven2/org/apache/xmlbeans/xmlbeans/3.1.0/xmlbeans-3.1.0.jar",
           "https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.19/commons-compress-1.19.jar",
           "https://repo1.maven.org/maven2/com/github/virtuald/curvesapi/1.06/curvesapi-1.06.jar"]

substringer = lambda x : x[x.rfind('/') + 1:]
libnames = list(map(substringer, libURLs))

for i in range(len(libURLs)):
    download(libURLs[i], "./TimeTable/lib/app/" + libnames[i])

print("Creating jar file")
call(f"javac -d compile --module-path ./TimeTable/lib/app/" + ";./TimeTable/lib/app/".join(libnames) + " src/main/java/timetable/*.java src/main/java/timetable/listeners/*.java src/main/java/module-info.java")
copydir("src/main/resources/assets", "compile/assets")

manifest = """Main-Class: timetable.Main
              Class-Path: lib/app/""" + " lib/app/".join(libnames)

with open("Manifest.txt", "w") as manifestFile:
    manifestFile.write(format(manifest) + "\n")

call("jar cfm TimeTable.jar Manifest.txt -C compile assets -C compile timetable -C compile module-info.class")
rename("TimeTable.jar", "./TimeTable/TimeTable.jar")
removefile("Manifest.txt")
removedir("compile")

copyfile("icon.ico", "./TimeTable/icon.ico")

builderLocation = fr"{environ['PROGRAMFILES(X86)']}\Microsoft Visual Studio\2019\Community\MSBuild\Current\Bin\MSBuild.exe"
if path.exists(builderLocation):
    print("Creating Shortcut Creator")
    call(fr'"{builderLocation}" Shortcut.csproj /p:Configuration=Release /verbosity:quiet', stderr = DEVNULL, stdin = DEVNULL)
    
    rename("./bin/Release/Shortcut.exe", "./TimeTable/ShortcurCreator.exe")
    removedir("bin")
    removedir("obj")
else:
    print("Unable to find csc.exe, skipping shortcut creation")

print("Done")