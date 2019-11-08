from plyer import notification
from os import environ
from urllib.request import urlretrieve as download
from win32com.client import Dispatch
from zipfile import ZipFile

notification.notify(title = "Here is the title",
                    message = "Here is the message",
                    app_name = "Here is the application name",
                    timeout = 5)
                    #app_icon = 'path/to/the/icon.png')

download("GOOGLE DRIVE ZIP HERE", "./TimeTable/lib/app/yasson-1.0.5.jar")

with ZipFile("Download.zip", 'r') as zip_ref:
    zip_ref.extractall(r"C:\Program Files\TimeTable")

shell = Dispatch("WScript.Shell")
shortcut = shell.CreateShortCut(environ["HOMEPATH"] + r"\Desktop\Test.lnk")
shortcut.Targetpath = r"C:\Program Files\TimeTable\bin\javaw.exe"
shortcut.IconLocation = r"C:\Program Files\TimeTable\icon.ico"
shortcut.Arguments = r"-jar C:\Program Files\TimeTable\TimeTable.jar"
shortcut.WorkingDirectory = r"C:\Program Files\TimeTable"
shortcut.save()