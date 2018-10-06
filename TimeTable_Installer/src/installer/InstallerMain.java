package installer;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

public final class InstallerMain {
	public static final JTextArea outputWindow = new JTextArea();
	public static final JProgressBar poggersBar = new JProgressBar(0, 7);
	public static final JLabel poggersLabel = new JLabel();
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm:ss");
	
	public static void main(String[] args) throws IOException {
		if(args.length == 0 || !args[0].equals("-window")) {
			EventQueue.invokeLater(() -> {
				JDialog frame = new JDialog((JFrame)null, "TimeTable Installer");
				
				outputWindow.setFont(new Font("Arial", Font.PLAIN, 12));
				outputWindow.setEditable(false);
				outputWindow.setBounds(10, 10, 560, 400);
				outputWindow.setBorder(new LineBorder(Color.BLACK, 2));
				
				poggersBar.setBounds(10, 450, 560, 30);
				
				poggersLabel.setForeground(Color.BLACK);
				poggersLabel.setBounds(10, 500, 300, 30);
				
				frame.setLayout(null);
				frame.add(outputWindow);
				frame.add(poggersBar);
				frame.add(poggersLabel);
				frame.setBounds(0, 0, 600, 600);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			});
		}
		
		var libPath = Paths.get("lib");
		
		if(!Files.exists(libPath)) {
			printToConsole("Lib folder doesn't exist, creating it");
			Files.createDirectory(libPath);
		}
		printToConsole("Checking libraries");
		
		downloadFile(true, "lib/commons-collections4-4.2.jar", "https://drive.google.com/uc?authuser=0&id=1FU7x9SpDSyxghMhnnwYE8gp_0XfOJHSe&export=download");
		downloadFile(true, "lib/commons-compress-1.18.jar", "https://drive.google.com/uc?authuser=0&id=1zpiKqG_rx5obyJ5Rr7Q_312_FlmT4IOZ&export=download");
		downloadFile(true, "lib/poi-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1WLRIqLqdv8QC_kmyuOwBqeXFq_U4XPy_&export=download");
		downloadFile(true, "lib/poi-ooxml-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1Kynmny0BSnahEY4DlM7FeBGm8kv7-yDY&export=download");
		downloadFile(true, "lib/poi-ooxml-schemas-4.0.0.jar", "https://drive.google.com/uc?authuser=0&id=1eqlZ68BSZ6QK1GYGt6rDpbUuTYMkmWJj&export=download");
		downloadFile(true, "lib/xmlbeans-3.0.1.jar", "https://drive.google.com/uc?authuser=0&id=1L2fLAFxnj3ycUGcCFtIhZmXj8_UoqnxI&export=download");
		downloadFile(false, "TimeTable.jar", "https://drive.google.com/uc?authuser=0&id=1fmTlv695eloSS3CEr2ihQ-a_wMrVdH0V&export=download");
		
		printToConsole("Starting TimeTable.jar");
		
		Runtime.getRuntime().exec("java -jar TimeTable.jar" + (args.length == 1 ? " " + args[0] : ""));
		System.exit(0);
	}
	
	private static void printToConsole(String text) {
		outputWindow.append("  " + LocalDateTime.now().format(formatter) + ": " + text + '\n');
	}
	
	private static void downloadFile(boolean checkIfExists, String filePath, String URL) throws IOException {
		var path = Paths.get(filePath);
		poggersBar.setValue(poggersBar.getValue() + 1);
		
		if(checkIfExists && Files.exists(path)) {
			printToConsole("File already exists: " + filePath);
			return;
		}
		
		printToConsole("Started to download file: " + filePath);
		poggersLabel.setText("Downloading file: " + filePath);
		
		try(var urlChannel = Channels.newChannel(new URL(URL).openStream()); 
			var fileChannel = FileChannel.open(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE)){
			
			printToConsole("Writing file: " + filePath);
			fileChannel.transferFrom(urlChannel, 0, Integer.MAX_VALUE);
		}
	}
}