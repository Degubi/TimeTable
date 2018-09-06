package degubi;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class PropertyFile {
	private static final Map<String, String> storage = initPropertyFile();
	private PropertyFile() {}
	
	private static Map<String, String> initPropertyFile() {
		try(var lines = Files.lines(Paths.get("settings.prop"))){
			return lines.map(line -> line.split(":", 2)).collect(Collectors.toMap(line -> line[0], line -> line[1], (k, v) -> k, LinkedHashMap::new));
		} catch (IOException e) {
			try {
				Files.createFile(Paths.get("settings.prop"));
			} catch (IOException e1) {}
			
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
		String[] val = getString(key, r + " " + g + " " + b).split(" ");
		return new Color(Integer.parseInt(val[0]), Integer.parseInt(val[1]), Integer.parseInt(val[2]));
	}
	
	private static boolean getBoolean(String key, boolean defaultValue) {
		return Boolean.parseBoolean(getString(key, String.valueOf(defaultValue)));
	}
	
	public static void setColor(String key, Color newColor) {
		setString(key, newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
	}
	
	public static void setBoolean(String key, boolean newValue) {
		setString(key, newValue ? "true" : "false");
	}
	
	public static void setString(String key, String value) {
		storage.replace(key, value);
		save();
	}
	
	private static void save() {
		try {
			Files.write(Paths.get("settings.prop"), storage.entrySet().stream()
					   				 .map(entry -> entry.getKey() + ':' + entry.getValue())
					   				 .collect(Collectors.toList()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e1) {}
	}
	
	public static LocalTime dayTimeStart = LocalTime.parse(getString("dayTimeStart", "07:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static LocalTime dayTimeEnd = LocalTime.parse(getString("dayTimeEnd", "19:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static Color dayTimeColor = getColor("dayTimeColor", 240, 240, 240);
	public static Color nightTimeColor = getColor("nightTimeColor", 64, 64, 64);
	public static boolean enablePopups = getBoolean("enablePopups", true);
	public static Color currentClassColor = getColor("currentClassColor", 255, 69, 69);
	public static Color upcomingClassColor = getColor("upcomingClassColor", 0, 147, 3);
	public static Color otherClassColor = getColor("otherClassColor", 84, 113, 142);
	public static Color pastClassColor = getColor("pastClassColor", 247, 238, 90);
	public static Color unimportantClassColor = getColor("unimportantClassColor", 192, 192, 192);
}