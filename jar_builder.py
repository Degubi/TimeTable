import os
import inspect

print("Creating jar files")

manifest = """Main-Class: contentgenerator.Main
              Class-Path: lib/app/jakarta.json.bind-api-1.0.1.jar lib/app/jakarta.json-1.1.5-module.jar lib/app/jakarta.json-api-1.1.5.jar lib/app/yasson-1.0.5.jar"""

with open("Manifest.txt", "w") as manifestFile:
	manifestFile.write(inspect.cleandoc(manifest) + "\n")
    
os.system("jar cfm TimeTable.jar Manifest.txt -C bin assets -C bin timetable -C bin module-info.class")
os.remove("Manifest.txt")
    
print("Done")