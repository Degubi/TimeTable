package degubi;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PropertyFile {
	private final Map<String, String> storage;
	private final Path path;
	
	public PropertyFile(String filePath) {
		path = Paths.get(filePath);
		storage = readFile();
	}
	
	private Map<String, String> readFile(){
		try(Stream<String> lines = Files.lines(path)){
			return lines.map(line -> line.split(":", 2)).collect(Collectors.toMap(line -> line[0], line -> line[1], (k, v) -> k, LinkedHashMap::new));
		} catch (IOException e) {
			try {
				Files.createFile(path);
			} catch (IOException e1) {}
			
			return new HashMap<>(5);
		}
	}
	
	public String get(String key, String defaultValue) {
		for(Entry<String, String> entries : storage.entrySet()) {
			if(entries.getKey().equals(key)) {
				return entries.getValue();
			}
		}
		
		storage.put(key, defaultValue);
		save();
		return defaultValue;
	}
	
	public Color getColor(String key, int r, int g, int b) {
		String[] val = get(key, r + " " + g + " " + b).split(" ");
		return new Color(Integer.parseInt(val[0]), Integer.parseInt(val[1]), Integer.parseInt(val[2]));
	}
	
	public void setColor(String key, Color newColor) {
		set(key, newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
	}
	
	public boolean containsKey(String key) {
		return storage.containsKey(key);
	}
	
	public void set(String key, String value) {
		for(Entry<String, String> entries : storage.entrySet()) {
			if(entries.getKey().equals(key)) {
				entries.setValue(value);
			}
		}
	}
	
	public void save() {
		try {
			Files.write(path, storage.entrySet().stream()
					   				 .map(entry -> entry.getKey() + ':' + entry.getValue())
					   				 .collect(Collectors.toList()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}