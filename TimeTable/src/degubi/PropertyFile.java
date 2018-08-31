package degubi;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class PropertyFile {
	private final Map<String, String> storage;
	private final Path path;
	
	public PropertyFile(String filePath) {
		path = Paths.get(filePath);
		
		Map<String, String> data;
		try(var lines = Files.lines(path)){
			data = lines.map(line -> line.split(":", 2)).collect(Collectors.toMap(line -> line[0], line -> line[1], (k, v) -> k, LinkedHashMap::new));
		} catch (IOException e) {
			data = new LinkedHashMap<>(10);
			try {
				Files.createFile(path);
			} catch (IOException e1) {}
		}
		storage = data;
	}
	
	public String get(String key, String defaultValue) {
		if(storage.containsKey(key)) {
			return storage.get(key);
		}
		storage.put(key, defaultValue);
		save();
		return defaultValue;
	}
	
	public Color getColor(String key, int r, int g, int b) {
		String[] val = get(key, r + " " + g + " " + b).split(" ");
		return new Color(Integer.parseInt(val[0]), Integer.parseInt(val[1]), Integer.parseInt(val[2]));
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
	}
	
	public void setColor(String key, Color newColor) {
		set(key, newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
	}
	
	public void setBoolean(String key, boolean newValue) {
		set(key, String.valueOf(newValue));
	}
	
	public void set(String key, String value) {
		storage.replace(key, value);
		save();
	}
	
	public void save() {
		try {
			Files.write(path, storage.entrySet().stream()
					   				 .map(entry -> entry.getKey() + ':' + entry.getValue())
					   				 .collect(Collectors.toList()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e1) {}
	}
}