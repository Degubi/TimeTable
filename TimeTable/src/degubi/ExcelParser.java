package degubi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public final class ExcelParser extends FileFilter{
	
	public static void showExcelFileBrowser(Path dataFilePath) throws IOException {
		if(JOptionPane.showConfirmDialog(null, "Van Excel Fájlod Cica?", "Excel Keresõ", JOptionPane.YES_NO_OPTION) == 0) {
			JFileChooser chooser = new JFileChooser("./");
			chooser.setFileFilter(new ExcelParser());
			
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				try(Workbook book = WorkbookFactory.create(chooser.getSelectedFile().getAbsoluteFile())){
					List<String> dataLines = StreamSupport.stream(book.getSheetAt(0).spliterator(), false)
														  .filter(row -> row.getRowNum() > 0)
														  .map(ExcelParser::mapToDataLine)
														  .collect(Collectors.toList());
					
					Files.write(dataFilePath, dataLines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				}
			}else{
				Files.createFile(dataFilePath);
			}
		}else {
			Files.createFile(dataFilePath);
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
}