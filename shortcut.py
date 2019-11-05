from subprocess import call
from inspect import cleandoc as format
from os import getcwd, environ, remove as removefile

workdir = getcwd()
linkPath = environ["HOMEPATH"] + r"\Desktop\TimeTable.lnk"

command = fr'''Set oWS = WScript.CreateObject("WScript.Shell")
               Set oLink = oWS.CreateShortcut("{linkPath}")
                   oLink.TargetPath = "{workdir}\bin\javaw.exe"
                   oLink.Arguments = "-jar ""{workdir}\TimeTable.jar"""
                   oLink.WorkingDirectory = "{workdir}"
                   oLink.IconLocation = "{workdir}\icon.ico"
                   oLink.Save'''

with open("iconScript.vbs", "w") as vbsScript:
    vbsScript.write(format(command))

call("wscript.exe iconScript.vbs");
removefile("iconScript.vbs")