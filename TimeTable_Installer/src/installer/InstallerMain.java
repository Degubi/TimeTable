package installer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;

public final class InstallerMain {
	public static void main(String[] args) throws IOException, InterruptedException {
		var dir = Path.of("lib");
		var client = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).build();

		if(!Files.exists(dir)) {
			Files.createDirectory(dir);
		}

		downloadFile(true, "lib/commons-collections4-4.2.jar", "https://drive.google.com/uc?authuser=0&id=1FU7x9SpDSyxghMhnnwYE8gp_0XfOJHSe&export=download", client);
		downloadFile(true, "lib/commons-compress-1.18.jar", "https://drive.google.com/uc?authuser=0&id=1zpiKqG_rx5obyJ5Rr7Q_312_FlmT4IOZ&export=download", client);
		downloadFile(true, "lib/poi-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1WLRIqLqdv8QC_kmyuOwBqeXFq_U4XPy_&export=download", client);
		downloadFile(true, "lib/poi-ooxml-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1Kynmny0BSnahEY4DlM7FeBGm8kv7-yDY&export=download", client);
		downloadFile(true, "lib/poi-ooxml-schemas-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1eqlZ68BSZ6QK1GYGt6rDpbUuTYMkmWJj&export=download", client);
		downloadFile(true, "lib/xmlbeans-3.0.1.jar", "https://drive.google.com/uc?authuser=0&id=1L2fLAFxnj3ycUGcCFtIhZmXj8_UoqnxI&export=download", client);
		downloadFile(true, "lib/gson-2.8.5.jar", "https://drive.google.com/uc?authuser=0&id=1E9Z5mfv1Cgf7XH4OHaAqUpx8CqAVnQ3a&export=download", client);
		downloadFile(true, "lib/icon.ico", "https://drive.google.com/uc?authuser=0&id=1zl43T-olB6k-TyYqp33M3uN1mcnqVnMM&export=download", client);
		downloadFile(false, "TimeTable.jar", "https://drive.google.com/uc?authuser=0&id=1fmTlv695eloSS3CEr2ihQ-a_wMrVdH0V&export=download", client);
		
		Runtime.getRuntime().exec("java -jar TimeTable.jar");
	}
	
	public static void downloadFile(boolean checkForExistence, String filePath, String URL, HttpClient client) throws IOException, InterruptedException {
		var file = Path.of(filePath);
		
		if(!checkForExistence || !Files.exists(file)) {
			client.send(HttpRequest.newBuilder(URI.create(URL)).build(), BodyHandlers.ofFile(file));
		}
	}
}