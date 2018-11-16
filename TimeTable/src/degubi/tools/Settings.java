package degubi.tools;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Settings extends FileFilter{
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final JsonObject settingsObject = getMainSettingsObject();
	
	public static boolean enablePopups = getBoolean("enablePopups", true);
	public static LocalTime dayTimeStart = LocalTime.parse(getString("dayTimeStart", "07:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static LocalTime dayTimeEnd = LocalTime.parse(getString("dayTimeEnd", "19:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static Color dayTimeColor = getColor("dayTimeColor", 235, 235, 235);
	public static Color nightTimeColor = getColor("nightTimeColor", 64, 64, 64);
	public static Color currentClassColor = getColor("currentClassColor", 255, 69, 69);
	public static Color upcomingClassColor = getColor("upcomingClassColor", 0, 147, 3);
	public static Color otherDayClassColor = getColor("otherDayClassColor", 84, 113, 142);
	public static Color pastClassColor = getColor("pastClassColor", 247, 238, 90);
	public static Color unimportantClassColor = getColor("unimportantClassColor", 192, 192, 192);
	public static int noteTime = getInt("noteTime", 60);
	public static int updateInterval = getInt("updateInterval", 600);
	public static final JsonArray friends = getArray("friends", () -> new JsonObject[0]);
	public static final JsonArray notes = getArray("notes", () -> new JsonObject[0]);
	public static final JsonArray classes = getArray("classes", () -> getClassData());
	
	private Settings() {}
	
	private static JsonObject getMainSettingsObject() {
		var filePath = Path.of("settings.json");
		var parser = new JsonParser();
		
		try {
			return parser.parse(Files.readString(filePath)).getAsJsonObject();
		} catch (IOException e) {
			try {
				Files.writeString(filePath, "{}", StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e1) {}
			
			return parser.parse("{}").getAsJsonObject();
		}
	}
	
	private static boolean getBoolean(String key, boolean defaultValue) {
		if(!settingsObject.has(key)) {
			settingsObject.addProperty(key, defaultValue);
			save();
		}
		return settingsObject.get(key).getAsBoolean();
	}
	
	private static int getInt(String key, int defaultValue) {
		if(!settingsObject.has(key)) {
			settingsObject.addProperty(key, defaultValue);
			save();
		}
		return settingsObject.get(key).getAsInt();
	}
	
	private static Color getColor(String key, int r, int g, int b) {
		var color = getString(key, r + " " + g + " " + b).split(" ", 3);
		return new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
	}
	
	private static String getString(String key, String defaultValue) {
		if(!settingsObject.has(key)) {
			settingsObject.addProperty(key, defaultValue);
			save();
		}
		return settingsObject.get(key).getAsString();
	}
	
	private static JsonArray getArray(String key, Supplier<JsonObject[]> defaults){
		if(!settingsObject.has(key)) {
			settingsObject.add(key, gson.toJsonTree(defaults.get()));
			save();
		}
		return settingsObject.getAsJsonArray(key);
	}
	
	public static JsonObject newClassObject(String day, String className, String classType, String startTime, String endTime, String room, boolean unImportant) {
		var obj = new JsonObject();
		obj.addProperty("day", day);
		obj.addProperty("className", className);
		obj.addProperty("classType", classType);
		obj.addProperty("startTime", startTime);
		obj.addProperty("endTime", endTime);
		obj.addProperty("room", room);
		obj.addProperty("unImportant", unImportant);
		return obj;
	}
	
	public static JsonObject newClassObject(JTable table, boolean unImportant) {
		return newClassObject(table.getValueAt(1, 1).toString(), 
							  table.getValueAt(0, 1).toString(), 
							  table.getValueAt(2, 1).toString(), 
							  table.getValueAt(3, 1).toString(), 
							  table.getValueAt(4, 1).toString(), 
							  table.getValueAt(5, 1).toString(), 
							  unImportant);
	}
	
	public static JsonObject newFriendObject(String name, String URL) {
		var obj = new JsonObject();
		obj.addProperty("name", name);
		obj.addProperty("url", URL);
		return obj;
	}
	
	public static Color parseFromString(String color) {
		var split = color.split(" ", 3);
		return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
	}
	
	public static JsonObject newNoteObject(String message, Color color) {
		var obj = new JsonObject();
		obj.addProperty("color", color.getRed() + " " + color.getGreen() + " " + color.getBlue());
		obj.addProperty("message", message);
		return obj;
	}
	
	public static void updateColor(String key, Color val) {
		settingsObject.addProperty(key, val.getRed() + " " + val.getGreen() + " " + val.getBlue());
	}
	
	public static void updateBoolean(String key, boolean val) {
		settingsObject.addProperty(key, val);
	}
	
	public static void updateInt(String key, int val) {
		settingsObject.addProperty(key, val);
	}
	
	public static void updateString(String key, String val) {
		settingsObject.addProperty(key, val);
	}
	
	public static Stream<JsonObject> stream(JsonArray array){
		return StreamSupport.stream(array.spliterator(), false).map(JsonElement::getAsJsonObject);
	}
	
	public static<T> int indexOf(T find, T[] array) {
		for(int k = 0; k < array.length; ++k) {
			if(array[k].equals(find)) {
				return k;
			}
		}
		System.out.println(Arrays.stream(array).map(Object::toString).collect(Collectors.joining(", ")));
		throw new IllegalArgumentException("WTF Bro: " + find);
	}
	
	public static void save() {
		try {
			Files.writeString(Path.of("settings.json"), gson.toJson(settingsObject), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void createLink(String filePath, String toSavePath, String cmdArgs) {
		var scriptPath = Path.of("iconScript.vbs");
		var command = ("Set oWS = WScript.CreateObject(\"WScript.Shell\")\n" + 
					   "Set oLink = oWS.CreateShortcut(\"" + toSavePath + "\")\n" + 
						  	  "oLink.TargetPath = \"" + filePath + "\"\n" + 
						  	  "oLink.Arguments = \"" + cmdArgs + "\"\n" +
						  	  "oLink.IconLocation = \"" + getFullPath("./lib/icon.ico") + "\"\n" +
						  	  "oLink.WorkingDirectory = \"" + filePath.substring(0, filePath.lastIndexOf("\\")) + "\"\n" +
							  "oLink.Save\n");
		try {
			Files.writeString(scriptPath, command);
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
	
	public static Path getFullPath(String strPath) {
		try {
			return Path.of(strPath).toRealPath();
		} catch (IOException e) {
			return null;
		}
	}
	
	
	
	
	@Override
	public boolean accept(File file) {
		if(file.isDirectory()) {
			return true;
		}
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
	
	private static JsonObject[] showExcelFileBrowser() {
		if(JOptionPane.showConfirmDialog(null, "Van Excel Fájlod Cica?", "Excel Keresõ", JOptionPane.YES_NO_OPTION) == 0) {
			JFileChooser chooser = new JFileChooser("./");
			chooser.setFileFilter(new Settings());
			
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				try(var book = WorkbookFactory.create(chooser.getSelectedFile().getAbsoluteFile())){
					return StreamSupport.stream(book.getSheetAt(0).spliterator(), false)
								 		.filter(row -> row.getRowNum() > 0)
								 		.map(Settings::mapToClassObject)
								 		.toArray(JsonObject[]::new);
				} catch (IOException e) {}
			}
		}
		return new JsonObject[0];
	}
	
	private static JsonObject mapToClassObject(Row row) {
		var classType = row.getCell(3).getStringCellValue();
		var classInfo = row.getCell(5).getStringCellValue();
		var className = row.getCell(1).getStringCellValue()
				 			  			 .replace(" BSc", "")
				 			  			 .replace(" gyakorlat", "")
				 			  			 .replace(" gyak", "")
				 			  			 .replace(".", "");
		
		if(!classInfo.isEmpty()) {
			int divisorIndex = classInfo.indexOf('-');
			char dayChar = classInfo.charAt(0);
			
			var startTime = classInfo.substring(divisorIndex - 5, divisorIndex);
			var endTime = classInfo.substring(divisorIndex + 1, divisorIndex + 6);
			var room = classInfo.replace('-', ' ').split(" ", 9)[7];
			var day = dayChar == 'H' ? "Hétfõ" : dayChar == 'K' ? "Kedd" : dayChar == 'S' ? "Szerda" : dayChar == 'C' ? "Csütörtök" : "Péntek";
			
			return Settings.newClassObject(day, className, classType, startTime, endTime, room, false);
		}
		return Settings.newClassObject("Péntek", className, classType, "08:00", "10:00", "Ismeretlen", false);
	}
	
	
	
	//TODO Ez alatt törlendõ dolgok
	private static JsonObject newClassObjectInternal(String day, String className, String classType, String startTime, String endTime, String room, boolean unImportant) {
		var obj = new JsonObject();
		obj.addProperty("day", day);
		obj.addProperty("className", className.replace('_', ' '));
		obj.addProperty("classType", classType);
		obj.addProperty("startTime", startTime);
		obj.addProperty("endTime", endTime);
		obj.addProperty("room", room);
		obj.addProperty("unImportant", unImportant);
		return obj;
	}
	
	private static JsonObject[] getClassData() {
		if(Files.exists(Path.of("classData.txt"))) {
			return lines("classData.txt").stream()
					 .map(line -> line.split(" "))
					 .map(data -> newClassObjectInternal(data[0], data[1], data[2], data[3], data[4], data[5], Boolean.parseBoolean(data[6])))
					 .toArray(JsonObject[]::new);
		}
		return showExcelFileBrowser();
	}
	
	private static List<String> lines(String file){
		try {
			return Files.readAllLines(Path.of(file));
		} catch (IOException e) {
			return List.of();
		}
	}
}