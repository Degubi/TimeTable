package degubi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public final class ExcelParser extends FileFilter{
	private ExcelParser() {}
	
	public static void showExcelFileBrowser() {
		JFileChooser chooser = new JFileChooser("./");
		chooser.setFileFilter(new ExcelParser());
		
		if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			try(Workbook book = WorkbookFactory.create(chooser.getSelectedFile().getAbsoluteFile())){
				List<String> dataTestLines = new ArrayList<>();
				
				for(Row row : book.getSheetAt(0)) {
					if(row.getRowNum() > 0) {
						String className = row.getCell(1).getStringCellValue()
														 .replace(" BSc", "")
														 .replace(" gyakorlat", "")
														 .replace(" gyak", "")
														 .replace(' ', '_')
														 .replace(".", "");
						
						String classType = row.getCell(3).getStringCellValue();
						String classInfo = row.getCell(5).getStringCellValue();
						
						if(!classInfo.isEmpty()) {
							int divisorIndex = classInfo.indexOf('-');
							String startTime = classInfo.substring(divisorIndex - 5, divisorIndex);
							String endTime = classInfo.substring(divisorIndex + 1, divisorIndex + 6);
							String room = classInfo.replace('-', ' ').split(" ", 9)[7];
							
							dataTestLines.add(getDayFromChar(classInfo.charAt(0)) + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + false);
						}else{
							dataTestLines.add("Péntek" + ' ' + className + ' ' + classType + " 08:00 10:00 Ismeretlen " + false);
						}
					}
				}
				
				Files.write(Paths.get("classData.txt"), dataTestLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (EncryptedDocumentException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String getDayFromChar(char day) {
		switch(day) {
			case 'H': return "Hétfõ";
			case 'K': return "Kedd";
			case 'S': return "Szerda";
			case 'C': return "Csütörtök";
			default: return "Péntek";
		}
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
}