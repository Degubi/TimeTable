package degubi.tools;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Settings {
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final JsonObject settingsObject = getMainSettingsObject();
	
	public static boolean enablePopups = getBoolean("enablePopups", true);
	public static LocalTime dayTimeStart = LocalTime.parse(getString("dayTimeStart", "07:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static LocalTime dayTimeEnd = LocalTime.parse(getString("dayTimeEnd", "19:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static Color dayTimeColor = getColor("dayTimeColor", 235, 235, 235);
	public static Color nightTimeColor = getColor("nightTimeColor", 64, 64, 64);
	public static Color currentClassColor = getColor("currentClassColor", 255, 69, 69);
	public static Color upcomingClassColor = getColor("upcomingClassColor", 0, 147, 3);
	public static Color otherClassColor = getColor("otherClassColor", 84, 113, 142);
	public static Color pastClassColor = getColor("pastClassColor", 247, 238, 90);
	public static Color unimportantClassColor = getColor("unimportantClassColor", 192, 192, 192);
	public static int noteTime = getInt("noteTime", 60);
	public static int updateInterval = getInt("updateInterval", 600);
	public static final JsonArray friends = getArray("friends");
	public static final JsonArray notes = getArray("notes");
	/*public static final JsonArray classes = getArray("classes", 
			lines("classData.txt").stream()
				 				  .map(line -> line.split(" "))
				 				  .map(data -> newClassObject(data[0], data[1], data[2], data[3], data[4], data[5], Boolean.parseBoolean(data[6])))
				 				  .toArray(JsonObject[]::new));
	
	private static List<String> lines(String file){
		try {
			return Files.readAllLines(Path.of(file));
		} catch (IOException e) {
			return List.of();
		}
	}
	*/
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
	
	private static JsonArray getArray(String key, Object... defaults){
		if(!settingsObject.has(key)) {
			settingsObject.add(key, gson.toJsonTree(defaults));
			save();
		}
		return settingsObject.getAsJsonArray(key);
	}
	
	public static JsonObject newClassObject(String day, String className, String classType, String startTime, String endTime, String room, boolean unImportant) {
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
		return -1;
	}
	
	public static void save() {
		try {
			Files.writeString(Path.of("settings.json"), gson.toJson(settingsObject), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}