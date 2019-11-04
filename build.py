import os
import inspect
import urllib.request as net

print("Generating runtime")
jlinkCommand = (r"jlink --module-path .;..\lib\app "
                 "--output ./TimeTable/ "
                 "--add-modules java.desktop "
                 "--add-modules java.logging "
                 "--add-modules java.sql "
                 "--no-man-pages "
                 "--no-header-files")

os.system(jlinkCommand)

print("Downloading libraries")
os.mkdir("./TimeTable/lib/app")

net.urlretrieve("https://repo1.maven.org/maven2/org/eclipse/yasson/1.0.5/yasson-1.0.5.jar", "./TimeTable/lib/app/yasson-1.0.5.jar")
net.urlretrieve("https://repo1.maven.org/maven2/jakarta/json/jakarta.json-api/1.1.5/jakarta.json-api-1.1.5.jar", "./TimeTable/lib/app/jakarta.json-api-1.1.5.jar")
net.urlretrieve("https://repo1.maven.org/maven2/org/glassfish/jakarta.json/1.1.5/jakarta.json-1.1.5-module.jar", "./TimeTable/lib/app/jakarta.json-1.1.5-module.jar")
net.urlretrieve("https://repo1.maven.org/maven2/jakarta/json/bind/jakarta.json.bind-api/1.0.1/jakarta.json.bind-api-1.0.1.jar", "./TimeTable/lib/app/jakarta.json.bind-api-1.0.1.jar")

print("Creating jar file")
manifest = """Main-Class: timetable.Main
              Class-Path: lib/app/jakarta.json.bind-api-1.0.1.jar lib/app/jakarta.json-1.1.5-module.jar lib/app/jakarta.json-api-1.1.5.jar lib/app/yasson-1.0.5.jar"""

with open("Manifest.txt", "w") as manifestFile:
    manifestFile.write(inspect.cleandoc(manifest) + "\n")
    
os.system("jar cfm TimeTable.jar Manifest.txt -C bin assets -C bin timetable -C bin module-info.class")
os.rename("TimeTable.jar", "./TimeTable/TimeTable.jar")
os.remove("Manifest.txt")

print("Creating exe file")
os.system(r"c:\windows\Microsoft.NET\Framework\v3.5\csc.exe /t:exe /win32icon:icon.ico /out:TimeTable.exe Runner.cs")
os.rename("TimeTable.exe", "./TimeTable/TimeTable.exe")

print("Done")