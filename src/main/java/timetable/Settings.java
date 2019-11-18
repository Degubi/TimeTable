package timetable;

import static java.nio.file.StandardOpenOption.*;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import javax.json.*;
import javax.json.bind.*;

public final class Settings {
    public static final Jsonb json = JsonbBuilder.create(new JsonbConfig().withFormatting(Boolean.TRUE));

    public static boolean enablePopups;
    public static LocalTime dayTimeStart;
    public static LocalTime dayTimeEnd;
    public static Color dayTimeColor;
    public static Color nightTimeColor;
    public static Color currentClassColor;
    public static Color upcomingClassColor;
    public static Color otherDayClassColor;
    public static Color pastClassColor;
    public static Color unimportantClassColor;
    public static int timeBeforeNotification;
    public static int updateInterval;
    public static Map<String, List<ClassButton>> classes;
    
    static {
        var filePath = Path.of("settings.json");
        
        if(!Files.exists(filePath)) {
            try {
                Files.writeString(filePath, "{}", WRITE, CREATE);
            } catch (IOException e) {}
        }
        
        try {
            var settingsObject = json.fromJson(Files.readString(filePath), JsonObject.class);
            
            enablePopups = getOrDefaultBoolean("enablePopups", true, settingsObject);
            timeBeforeNotification = getOrDefaultInt("timeBeforeNotification", 60, settingsObject);
            updateInterval = getOrDefaultInt("updateInterval", 600, settingsObject);
            dayTimeStart = LocalTime.parse(getOrDefaultString("dayTimeStart", "07:00", settingsObject), DateTimeFormatter.ISO_LOCAL_TIME);
            dayTimeEnd = LocalTime.parse(getOrDefaultString("dayTimeEnd", "19:00", settingsObject), DateTimeFormatter.ISO_LOCAL_TIME);
            
            dayTimeColor = getOrDefaultColor("dayTimeColor", 235, 235, 235, settingsObject);
            nightTimeColor = getOrDefaultColor("nightTimeColor", 64, 64, 64, settingsObject);
            currentClassColor = getOrDefaultColor("currentClassColor", 255, 69, 69, settingsObject);
            upcomingClassColor = getOrDefaultColor("upcomingClassColor", 0, 147, 3, settingsObject);
            otherDayClassColor = getOrDefaultColor("otherDayClassColor", 84, 113, 142, settingsObject);
            pastClassColor = getOrDefaultColor("pastClassColor", 247, 238, 90, settingsObject);
            unimportantClassColor = getOrDefaultColor("unimportantClassColor", 192, 192, 192, settingsObject);
            updateClassesData(getArraySetting("classes", settingsObject).stream()
                                                                        .map(JsonValue::asJsonObject)
                                                                        .map(ClassButton::new)
                                                                        .collect(Collectors.groupingBy(k -> k.day)));
        } catch (JsonbException | IOException e) {
            throw new IllegalStateException("Something is fucked with settings brah");
        }
    }
    
    public static<T> int indexOf(T find, T[] array) {
        for(var k = 0; k < array.length; ++k) {
            if(array[k].equals(find)) {
                return k;
            }
        }
        return -1;
    }
    
    private Settings() {}
    
    public static JsonArray createClassesArray() {
        return classes.values().stream()
                       .flatMap(List::stream)
                       .map(k -> Json.createObjectBuilder()
                                     .add("day", k.day)
                                     .add("className", k.className)
                                     .add("classType", k.classType)
                                     .add("startTime", k.startTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
                                     .add("endTime", k.endTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
                                     .add("room", k.room)
                                     .add("unImportant", k.unImportant)
                                     .build())
                       .reduce(Json.createArrayBuilder(), JsonArrayBuilder::add, JsonArrayBuilder::addAll)
                       .build();
    }
    
    public static void updateClassesData(Map<String, List<ClassButton>> newClasses) {
        classes = newClasses;
        Arrays.stream(Main.days, 0, 5).forEach(day -> classes.computeIfAbsent(day, ignore -> new ArrayList<>()));
    }
    
    public static void saveSettings() {
        var settingsObject = Json.createObjectBuilder()
                                 .add("enablePopups", enablePopups)
                                 .add("timeBeforeNotification", timeBeforeNotification)
                                 .add("updateInterval", updateInterval)
                                 .add("dayTimeEnd", dayTimeEnd.format(DateTimeFormatter.ISO_LOCAL_TIME))
                                 .add("dayTimeStart", dayTimeStart.format(DateTimeFormatter.ISO_LOCAL_TIME))
                                 .add("dayTimeColor", colorToString(dayTimeColor))
                                 .add("nightTimeColor", colorToString(nightTimeColor))
                                 .add("currentClassColor", colorToString(currentClassColor))
                                 .add("upcomingClassColor", colorToString(upcomingClassColor))
                                 .add("otherDayClassColor", colorToString(otherDayClassColor))
                                 .add("pastClassColor", colorToString(pastClassColor))
                                 .add("unimportantClassColor", colorToString(unimportantClassColor))
                                 .add("classes", createClassesArray());
        
        try {
            Files.writeString(Path.of("settings.json"), json.toJson(settingsObject.build()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static String colorToString(Color color) {
        return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
    }
    
    private static int getOrDefaultInt(String key, int defaultValue, JsonObject settingsObject) {
        if(!settingsObject.containsKey(key)) {
            return defaultValue;
        }
        return settingsObject.getInt(key);
    }
    
    private static Color getOrDefaultColor(String key, int r, int g, int b, JsonObject settingsObject) {
        var color = getOrDefaultString(key, r + " " + g + " " + b, settingsObject).split(" ", 3);
        return new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
    }
    
    private static boolean getOrDefaultBoolean(String key, boolean defaultValue, JsonObject settingsObject) {
        if(!settingsObject.containsKey(key)) {
            return defaultValue;
        }
        
        return settingsObject.getBoolean(key);
    }
    
    public static JsonArray getArraySetting(String key, JsonObject settingsObject){
        if(!settingsObject.containsKey(key)) {
            return Json.createArrayBuilder().build();
        }
        
        return settingsObject.getJsonArray(key);
    }
    
    private static String getOrDefaultString(String key, String defaultValue, JsonObject settingsObject) {
        if(!settingsObject.containsKey(key)) {
            return defaultValue;
        }
        return settingsObject.getString(key);
    }
    
    public static void createStartupLink(String toSavePath) {
        var sourceDir = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1)).getParent().toString().replace("%20", " ");
        var command = "Set oWS = WScript.CreateObject(\"WScript.Shell\")\n" + 
                      "Set oLink = oWS.CreateShortcut(\"" + toSavePath + "\")\n" + 
                          "oLink.TargetPath = \"" + sourceDir + "\\bin\\javaw.exe" + "\"\n" + 
                          "oLink.Arguments = \"-jar \"\"" + sourceDir + "\\TimeTable.jar\"\" -nowindow" + "\"\n" +
                          "oLink.IconLocation = \"" + sourceDir + "\\icon.ico" + "\"\n" +
                          "oLink.WorkingDirectory = \"" + sourceDir + "\"\n" +
                          "oLink.Save\n";
        try {
            var scriptPath = Path.of("iconScript.vbs");

            Files.writeString(scriptPath, command);
            Runtime.getRuntime().exec("wscript.exe iconScript.vbs").waitFor();
            Files.delete(scriptPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}