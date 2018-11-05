package degubi.tools;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import degubi.TimeTableMain;

public final class NIO extends FileFilter{
	private NIO() {}
	
	public static void showExcelFileBrowser(Path dataFilePath) {
		if(JOptionPane.showConfirmDialog(null, "Van Excel Fájlod Cica?", "Excel Keresõ", JOptionPane.YES_NO_OPTION) == 0) {
			JFileChooser chooser = new JFileChooser("./");
			chooser.setFileFilter(new NIO());
			
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				try(var book = WorkbookFactory.create(chooser.getSelectedFile().getAbsoluteFile())){
					var dataLines = StreamSupport.stream(book.getSheetAt(0).spliterator(), false)
												 .filter(row -> row.getRowNum() > 0)
												 .map(NIO::mapToDataLine)
												 .collect(Collectors.toList());
					
					Files.write(dataFilePath, dataLines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {}
			}else{
				createNewFile(dataFilePath);
			}
		}else {
			createNewFile(dataFilePath);
		}
	}
	
	private static String mapToDataLine(Row row) {
		String classType = row.getCell(3).getStringCellValue(), classInfo = row.getCell(5).getStringCellValue();
		String className = row.getCell(1).getStringCellValue()
				 			  			 .replace(" BSc", "")
				 			  			 .replace(" gyakorlat", "")
				 			  			 .replace(" gyak", "")
				 			  			 .replace(' ', '_')
				 			  			 .replace(".", "");
		
		if(!classInfo.isEmpty()) {
			int divisorIndex = classInfo.indexOf('-');
			char dayChar = classInfo.charAt(0);
			
			String startTime = classInfo.substring(divisorIndex - 5, divisorIndex);
			String endTime = classInfo.substring(divisorIndex + 1, divisorIndex + 6);
			String room = classInfo.replace('-', ' ').split(" ", 9)[7];
			String day = dayChar == 'H' ? "Hétfõ " : dayChar == 'K' ? "Kedd " : dayChar == 'S' ? "Szerda " : dayChar == 'C' ? "Csütörtök " : "Péntek ";
			
			return day + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + false;
		}
		return "Péntek " + className + ' ' + classType + " 08:00 10:00 Ismeretlen " + false;
	}
	
	
	@Override
	public boolean accept(File file) {
		if(file.isFile()) {
			String fileName = file.getName();
			return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()).equals("xlsx");
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "Only Excel Documents";
	}
	
	public static void createLink(String filePath, String toSavePath, String cmdArgs) {
		var scriptPath = Paths.get("iconScript.vbs");
		var command = ("Set oWS = WScript.CreateObject(\"WScript.Shell\")\n" + 
						  "Set oLink = oWS.CreateShortcut(\"" + toSavePath + "\")\n" + 
						  	  "oLink.TargetPath = \"" + filePath + "\"\n" + 
						  	  "oLink.Arguments = \"" + cmdArgs + "\"\n" + 
						  	  "oLink.IconLocation = \"" + getFullPath("./lib/icon.ico") + "\"\n" +
						  	  "oLink.WorkingDirectory = \"" + filePath.substring(0, filePath.lastIndexOf("\\")) + "\"\n" +
							  "oLink.Save\n").getBytes();
		try {
			Files.write(scriptPath, command);
			var proc = Runtime.getRuntime().exec("wscript.exe iconScript.vbs");
			
			while(proc.isAlive()) Thread.onSpinWait();
			Files.delete(scriptPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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
	
	public static void checkFileOr(Path filePath, Consumer<Path> path) {
		if(!Files.exists(filePath)) {
			path.accept(filePath);
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
	
	public static void createScreenshot(@SuppressWarnings("unused") ActionEvent event) {
		var window = TimeTableMain.mainPanel.getTopLevelAncestor().getLocationOnScreen();
		try {
			ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 50, window.y + 80, 870, 600)), "PNG", new File(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) +".png"));
		} catch (HeadlessException | AWTException | IOException e1) {}
	}
	
	public static ImageIcon getIcon(String path, int scale) {
		var image = Toolkit.getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/" + path));
		if(scale == 0 || image.getWidth(null) == scale) {
			return new ImageIcon(image);
		}
		return new ImageIcon(image.getScaledInstance(scale, scale, Image.SCALE_SMOOTH));
	}
}