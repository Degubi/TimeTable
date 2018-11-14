package installer;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class InstallerMain {
	
	public static void main(String[] args) throws IOException {
		var libPath = Path.of("lib");
		
		if(!Files.exists(libPath)) {
			Files.createDirectory(libPath);
		}
		
		downloadFile(true, "lib/commons-collections4-4.2.jar", "https://drive.google.com/uc?authuser=0&id=1FU7x9SpDSyxghMhnnwYE8gp_0XfOJHSe&export=download");
		downloadFile(true, "lib/commons-compress-1.18.jar", "https://drive.google.com/uc?authuser=0&id=1zpiKqG_rx5obyJ5Rr7Q_312_FlmT4IOZ&export=download");
		downloadFile(true, "lib/poi-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1WLRIqLqdv8QC_kmyuOwBqeXFq_U4XPy_&export=download");
		downloadFile(true, "lib/poi-ooxml-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1Kynmny0BSnahEY4DlM7FeBGm8kv7-yDY&export=download");
		downloadFile(true, "lib/poi-ooxml-schemas-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1eqlZ68BSZ6QK1GYGt6rDpbUuTYMkmWJj&export=download");
		downloadFile(true, "lib/xmlbeans-3.0.1.jar", "https://drive.google.com/uc?authuser=0&id=1L2fLAFxnj3ycUGcCFtIhZmXj8_UoqnxI&export=download");
		downloadFile(true, "lib/gson-2.8.5.jar", "https://drive.google.com/uc?authuser=0&id=1E9Z5mfv1Cgf7XH4OHaAqUpx8CqAVnQ3a&export=download");
		downloadFile(true, "lib/icon.ico", "https://drive.google.com/uc?authuser=0&id=1zl43T-olB6k-TyYqp33M3uN1mcnqVnMM&export=download");
		downloadFile(false, "TimeTable.jar", "https://drive.google.com/uc?authuser=0&id=1fmTlv695eloSS3CEr2ihQ-a_wMrVdH0V&export=download");
		
		Runtime.getRuntime().exec("java -jar TimeTable.jar");
		System.exit(0);
	}
	
	private static void downloadFile(boolean checkIfExists, String filePath, String url) throws IOException {
		var path = Path.of(filePath);
		
		if(checkIfExists && Files.exists(path)) {
			return;
		}
		
		try(var urlChannel = Channels.newChannel(new URL(url).openStream()); 
			var fileChannel = FileChannel.open(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE)){
			
			fileChannel.transferFrom(urlChannel, 0, Integer.MAX_VALUE);
		}
	}
}