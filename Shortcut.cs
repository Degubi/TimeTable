using System;
using System.IO;
using IWshRuntimeLibrary;

namespace Shortcut {
    public class Shortcut {
        public static void Main(string[] args) {
            var workdir = Directory.GetCurrentDirectory();
            var desktop = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            var shortcut = (IWshShortcut)new WshShell().CreateShortcut(desktop + @"\TimeTable.lnk");
            shortcut.TargetPath = workdir + @"\bin\javaw.exe";
            shortcut.IconLocation = workdir + @"\icon.ico";
            shortcut.Arguments = $"-jar \"{workdir}\\TimeTable.jar\"";
            shortcut.WorkingDirectory = workdir;
            shortcut.Save();
        }
    }
}