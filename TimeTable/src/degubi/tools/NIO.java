package degubi.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class NIO {
	private NIO() {}
	
	public static Process createLink(Path tempScriptFile, String filePath, String toSavePath) {
		var command = ("Set oWS = WScript.CreateObject(\"WScript.Shell\")\n" + 
						  "Set oLink = oWS.CreateShortcut(\"" + toSavePath + "\")\n" + 
						  	  "oLink.TargetPath = \"" + filePath + "\"\n" + 
						  	  "oLink.Arguments = \"-window\"\n" + 
						  	  "oLink.WorkingDirectory = \"" + filePath.substring(0, filePath.lastIndexOf("\\")) + "\"\n" +
							  "oLink.Save\n").getBytes();
		try {
			Files.write(tempScriptFile, command);
			return Runtime.getRuntime().exec("wscript.exe iconScript.vbs");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	public static void deleteIfExists(Path path) {
		if(Files.exists(path))
			try {
				Files.delete(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public static void createNewFile(Path filePath) {
		try {
			Files.createFile(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeAllLines(String filePath, List<String> lines) {
		try {
			Files.write(Paths.get(filePath), lines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Path getFullPath(String strPath) {
		try {
			return Paths.get(strPath).toRealPath();
		} catch (IOException e) {
			return null;
		}
	}
}