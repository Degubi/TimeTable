from subprocess import call
from shutil import copyfile, copytree
from os import rename

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

copytree("./target/lib", "./TimeTable/lib/app")
copyfile("icon.ico", "./TimeTable/icon.ico")
copyfile("createShortcut.vbs", "./TimeTable/createShortcut.vbs")

print("Creating jar file")

call("jar cfm TimeTable.jar MANIFEST.MF -C target/classes module-info.class -C target/classes timetable -C target/classes assets")
rename("TimeTable.jar", "./TimeTable/TimeTable.jar")

print("Done")