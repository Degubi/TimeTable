package timetable;

import static java.nio.file.StandardOpenOption.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.*;
import java.awt.*;
import java.io.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public final class Settings {
    private static final ObjectMapper json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(SerializationFeature.INDENT_OUTPUT, true).registerModule(new JavaTimeModule());

    private static final String SETTING_CLOUD_ID = "cloudID";
    private static final String SETTING_DAY_TIME_END = "dayTimeEnd";
    private static final String SETTING_DAY_TIME_START = "dayTimeStart";
    private static final String SETTING_MINUTES_BEFORE_NEXT_CLASS_NOTIFICATION = "minutesBeforeNextClassNotification";
    private static final String SETTING_ENABLE_POPUPS = "enablePopups";

    public static final String NULL_CLOUD_ID = "null";
    public static final TypeReference<ArrayList<ClassButton>> CLASSES_TYPEREF = new TypeReference<>() {};

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
    public static int minutesBeforeNextClassNotification;
    public static String cloudID;
    public static ArrayList<ClassButton> classes;

    static {
        var settingsPath = Path.of("./settings.json");

        if(!Files.exists(settingsPath)) {
            try {
                Files.writeString(settingsPath, "{}", WRITE, CREATE);
            } catch (IOException e) {}
        }

        try {
            var settingsObject = json.readValue(Files.readAllBytes(settingsPath), JsonNode.class);

            enablePopups = settingsObject.has(SETTING_ENABLE_POPUPS) ? settingsObject.get(SETTING_ENABLE_POPUPS).asBoolean() : true;
            minutesBeforeNextClassNotification = settingsObject.has(SETTING_MINUTES_BEFORE_NEXT_CLASS_NOTIFICATION) ? settingsObject.get(SETTING_MINUTES_BEFORE_NEXT_CLASS_NOTIFICATION).asInt() : 60;
            dayTimeStart = settingsObject.has(SETTING_DAY_TIME_START) ? json.readValue(json.treeAsTokens(settingsObject.get(SETTING_DAY_TIME_START)), LocalTime.class) : LocalTime.of(7, 0);
            dayTimeEnd = settingsObject.has(SETTING_DAY_TIME_END) ? json.readValue(json.treeAsTokens(settingsObject.get(SETTING_DAY_TIME_END)), LocalTime.class) : LocalTime.of(19, 0);

            dayTimeColor = getOrDefaultColor("dayTimeColor", 235, 235, 235, settingsObject);
            nightTimeColor = getOrDefaultColor("nightTimeColor", 64, 64, 64, settingsObject);
            currentClassColor = getOrDefaultColor("currentClassColor", 255, 69, 69, settingsObject);
            upcomingClassColor = getOrDefaultColor("upcomingClassColor", 0, 147, 3, settingsObject);
            otherDayClassColor = getOrDefaultColor("otherDayClassColor", 84, 113, 142, settingsObject);
            pastClassColor = getOrDefaultColor("pastClassColor", 247, 238, 90, settingsObject);
            unimportantClassColor = getOrDefaultColor("unimportantClassColor", 192, 192, 192, settingsObject);
            cloudID = settingsObject.has(SETTING_CLOUD_ID) ? settingsObject.get(SETTING_CLOUD_ID).asText() : NULL_CLOUD_ID;
            classes = settingsObject.has("classes") ? json.readValue(json.treeAsTokens(settingsObject.get("classes")), CLASSES_TYPEREF) : new ArrayList<>();
        } catch (IOException e) {
            throw new IllegalStateException("Something is fucked with settings brah");
        }
    }

    private Settings() {}

    public static void saveSettings() {
        var settingsObject = Map.ofEntries(
                                 Map.entry(SETTING_ENABLE_POPUPS, enablePopups),
                                 Map.entry(SETTING_MINUTES_BEFORE_NEXT_CLASS_NOTIFICATION, minutesBeforeNextClassNotification),
                                 Map.entry(SETTING_DAY_TIME_END, dayTimeEnd),
                                 Map.entry(SETTING_DAY_TIME_START, dayTimeStart),
                                 Map.entry("dayTimeColor", colorToString(dayTimeColor)),
                                 Map.entry("nightTimeColor", colorToString(nightTimeColor)),
                                 Map.entry("currentClassColor", colorToString(currentClassColor)),
                                 Map.entry("upcomingClassColor", colorToString(upcomingClassColor)),
                                 Map.entry("otherDayClassColor", colorToString(otherDayClassColor)),
                                 Map.entry("pastClassColor", colorToString(pastClassColor)),
                                 Map.entry("unimportantClassColor", colorToString(unimportantClassColor)),
                                 Map.entry(SETTING_CLOUD_ID, cloudID),
                                 Map.entry("classes", classes));
        try {
            Files.write(Path.of("./settings.json"), toBytes(settingsObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String colorToString(Color color) {
        return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
    }

    private static Color getOrDefaultColor(String key, int r, int g, int b, JsonNode settingsObject) {
        var color = (settingsObject.has(key) ? settingsObject.get(key).asText() : (r + " " + g + " " + b)).split(" ", 3);
        return new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
    }

    public static void createStartupLink(String toSavePath) {
        var currentPath = Path.of("").toAbsolutePath();
        var command = "Set oWS = WScript.CreateObject(\"WScript.Shell\")\n" +
                      "Set oLink = oWS.CreateShortcut(\"" + toSavePath + "\")\n" +
                          "oLink.TargetPath = \"" + currentPath + "/TimeTable.exe\"\n" +
                          "oLink.WorkingDirectory = \"" + currentPath + "\"\n" +
                          "oLink.Arguments = \"-nowindow\"\n" +
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


    public static byte[] toBytes(Object obj) {
        try {
            return json.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static<T> BodyHandler<T> of(Class<T> type) {
        return info -> BodySubscribers.mapping(BodySubscribers.ofByteArray(), data -> {
            try {
                return data.length == 0 ? null : json.readValue(data, type);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public static<T> BodyHandler<T> of(TypeReference<T> typeRef) {
        return info -> BodySubscribers.mapping(BodySubscribers.ofByteArray(), data -> {
            try {
                return data.length == 0 ? null : json.readValue(data, typeRef);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public static BodyPublisher publisherOf(Object data) {
        try {
            return BodyPublishers.ofByteArray(json.writeValueAsBytes(data));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static BodyHandler<JsonNode> ofJsonObject() {
        return of(JsonNode.class);
    }
}