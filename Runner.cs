using System.Diagnostics;

namespace Runner {
    public class Runner {
        public static void Main(string[] args) {
            Process.Start("bin\\javaw", "-jar TimeTable.jar");
        }
    }
}