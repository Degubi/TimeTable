package installer;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

public final class InstallerMain {
	private static final String LibZipURL = "https://drive.google.com/uc?authuser=0&id=1KBGmNJ4UhwakUa-Fv2d2uklHPc8twpdZ&export=download";
	private static final String TimeTableJarURL = "https://drive.google.com/uc?authuser=0&id=1fmTlv695eloSS3CEr2ihQ-a_wMrVdH0V&export=download";
	
	public static void main(String[] args) throws IOException {
		if(args.length == 0 || !args[0].equals("-window")) EventQueue.invokeLater(() -> JOptionPane.showOptionDialog(null, "Telepítés folyamatban...", "TimeTable Installer", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null));
		
		var libPath = Paths.get("lib");
		
		if(!Files.exists(libPath)) {
			Files.createDirectory(libPath);
			
			try(var urlInput = new URL(LibZipURL).openStream();
				var input = new ZipInputStream(urlInput)){
				
				for(var entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
					Files.copy(input, Paths.get("./lib/" + entry.getName()));
				}
			}
		}
		
		try(var urlChannel = Channels.newChannel(new URL(TimeTableJarURL).openStream()); 
			var fileChannel = FileChannel.open(Paths.get("TimeTable.jar"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE)){
					
			fileChannel.transferFrom(urlChannel, 0, Integer.MAX_VALUE);
		}
		
		Runtime.getRuntime().exec("java -jar TimeTable.jar" + (args.length == 1 ? " " + args[0] : ""));
		System.exit(0);
	}
}