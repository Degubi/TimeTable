package installer;

import static java.nio.file.StandardOpenOption.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.*;
import java.net.http.HttpResponse.*;
import java.nio.file.*;

public final class InstallerMain {
	public static void main(String[] args) throws IOException, InterruptedException {
		HttpClient.newBuilder().followRedirects(Redirect.NORMAL).build()
		  		  .send(HttpRequest.newBuilder(URI.create("https://drive.google.com/uc?authuser=0&id=1fmTlv695eloSS3CEr2ihQ-a_wMrVdH0V&export=download")).build(), 
		  				BodyHandlers.ofFileDownload(Path.of("./"), CREATE, WRITE, TRUNCATE_EXISTING));

		Runtime.getRuntime().exec("java -jar TimeTable.jar");
	}
}