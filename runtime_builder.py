import os
import shutil
from zipfile import ZipFile, ZIP_DEFLATED

def zipDirs(zipName, *directories):
    userZip = ZipFile(zipName, "w", ZIP_DEFLATED)

    for dirs in directories:
        for root, _, fileNames in os.walk("runtime/" + dirs):
            for file in fileNames:
                filePath = root + "/" + file
                userZip.write(filePath, filePath[8:])
    
    return userZip

print("Generating runtime")

jlinkCommand = (r"jlink --module-path .;..\lib\app "
                 "--output ./runtime/ "
                 "--add-modules java.desktop "
				 "--add-modules java.logging "
				 "--add-modules java.sql "
				 "--add-modules java.net.http "
				 "--add-modules jdk.crypto.ec "
                 "--no-man-pages "
                 "--no-header-files")

os.system(jlinkCommand)
print("Zipping runtime")

runtimeZip = zipDirs("runtime1.zip", "lib", "bin", "conf", "legal")
runtimeZip.close()

print("Deleting runtime dir")
shutil.rmtree("./runtime")
print("Done")