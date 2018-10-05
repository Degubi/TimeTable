package degubi.tools;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class PropertyFile {
	private static final Map<String, String> storage = initPropertyFile();
	private PropertyFile() {}
	
	private static Map<String, String> initPropertyFile() {
		var settingsFile = Paths.get("settings.prop");
		
		try(var lines = Files.lines(settingsFile)){
			return lines.map(line -> line.split(":", 2))
						.collect(Collectors.toMap(line -> line[0], line -> line[1], (k, v) -> k, LinkedHashMap::new));
		} catch (IOException e) {
			NIO.createNewFile(settingsFile);
			
			return new LinkedHashMap<>(11);
		}
	}
	
	private static String getString(String key, String defaultValue) {
		if(storage.containsKey(key)) {
			return storage.get(key);
		}
		
		storage.put(key, defaultValue);
		save();
		return defaultValue;
	}
	
	private static Color getColor(String key, int r, int g, int b) {
		String[] val = getString(key, r + " " + g + " " + b).split(" ", 3);
		return new Color(Integer.parseInt(val[0]), Integer.parseInt(val[1]), Integer.parseInt(val[2]));
	}
	
	private static int getInt(String key, int defaultValue) {
		return Integer.parseInt(getString(key, Integer.toString(defaultValue)));
	}
	
	private static boolean getBoolean(String key, boolean defaultValue) {
		return Boolean.parseBoolean(getString(key, String.valueOf(defaultValue)));
	}
	
	public static Map<String, String> getMap(String key){
		String[] fullData = getString(key, "").split(";");
		
		return fullData[0].isEmpty() ? new LinkedHashMap<>() 
									 : Arrays.stream(fullData)
									 		 .map(str -> str.split(" ", 2))
									 		 .collect(Collectors.toMap(str -> str[0], str -> str[1]));
	}
	
	public static void setMap(String key, Map<String, String> map) {
		setString(key, map.entrySet().stream().map(entry -> entry.getKey() + ' ' + entry.getValue()).collect(Collectors.joining(";")));
	}
	
	public static void setColor(String key, Color newColor) {
		setString(key, newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
	}
	
	public static void setInt(String key, int value) {
		setString(key, Integer.toString(value));
	}
	
	public static void setBoolean(String key, boolean newValue) {
		setString(key, newValue ? "true" : "false");
	}
	
	public static void setString(String key, String value) {
		storage.replace(key, value);
		save();
	}
	
	private static void save() {
		var dataLines = storage.entrySet().stream()
		   		   			   .map(entry -> entry.getKey() + ':' + entry.getValue())
		   		   			   .collect(Collectors.toList());
		
		NIO.writeAllLines("settings.prop", dataLines);
	}
	
	public static LocalTime dayTimeStart = LocalTime.parse(getString("dayTimeStart", "07:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static LocalTime dayTimeEnd = LocalTime.parse(getString("dayTimeEnd", "19:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static Color dayTimeColor = getColor("dayTimeColor", 235, 235, 235);
	public static Color nightTimeColor = getColor("nightTimeColor", 64, 64, 64);
	public static boolean enablePopups = getBoolean("enablePopups", true);
	public static Color currentClassColor = getColor("currentClassColor", 255, 69, 69);
	public static Color upcomingClassColor = getColor("upcomingClassColor", 0, 147, 3);
	public static Color otherClassColor = getColor("otherClassColor", 84, 113, 142);
	public static Color pastClassColor = getColor("pastClassColor", 247, 238, 90);
	public static Color unimportantClassColor = getColor("unimportantClassColor", 192, 192, 192);
	public static int noteTime = getInt("noteTime", 60);
	public static int updateInterval = getInt("updateInterval", 600);
	public static final Map<String, String> friendsMap = getMap("friends");
}