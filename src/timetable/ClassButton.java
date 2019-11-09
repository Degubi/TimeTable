package timetable;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.Map.*;
import javax.json.*;
import javax.swing.*;
import timetable.listeners.*;

public final class ClassButton extends MouseAdapter {
    public static final Font importantClassFont = new Font("SansSerif", Font.BOLD, 12);

    public final String day;
    public final LocalTime startTime, endTime;
    public final String className, classType, room;
    public boolean unImportant;
    public final JButton button;
    
    public ClassButton(String day, String className, String classType, LocalTime startTime, LocalTime endTime, String room, boolean unImportant) {
        this.day = day;
        this.className = className;
        this.classType = classType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.unImportant = unImportant;
        
        button = new JButton("<html>Óra: " + className.replace('_', ' ') +
                             "<br>Idõ: " + startTime + "-" + endTime +
                             "<br>Típus: " + classType +
                             "<br>Épület: " + getBuildingForRoom(room) +
                             "<br>Terem: " + room);
        
        if(classType.charAt(0) == 'G') {
            button.setFont(importantClassFont);
        }
        
        button.setFocusable(false);
        button.addMouseListener(this);
    }
    
    public ClassButton(JTable editorTable, boolean unImportant) {
        this((String) editorTable.getValueAt(1, 1),
             (String) editorTable.getValueAt(0, 1),
             (String) editorTable.getValueAt(2, 1),
             LocalTime.parse((String) editorTable.getValueAt(3, 1), DateTimeFormatter.ISO_LOCAL_TIME),
             LocalTime.parse((String) editorTable.getValueAt(4, 1), DateTimeFormatter.ISO_LOCAL_TIME),
             (String) editorTable.getValueAt(5, 1), unImportant);
    }
    
    public ClassButton(JsonObject object) {
        this(object.getString("day"), object.getString("className"), object.getString("classType"), 
                LocalTime.parse(object.getString("startTime"), DateTimeFormatter.ISO_LOCAL_TIME),
                LocalTime.parse(object.getString("endTime"), DateTimeFormatter.ISO_LOCAL_TIME),
                object.getString("room"), object.getBoolean("unImportant"));
    }
    
    @Override
    public void mousePressed(MouseEvent event) {
        if(event.getButton() == MouseEvent.BUTTON3) {
            var frame = new JDialog((JFrame)Main.mainPanel.getTopLevelAncestor());
            var panel = new JPanel(null);
            var buttonLocation = button.getLocationOnScreen();
            
            frame.setContentPane(panel);
            frame.addWindowFocusListener(new FocusLostListener(frame));
            frame.setUndecorated(true);
            frame.setLocationRelativeTo(null);
            frame.setBounds(buttonLocation.x + 118, buttonLocation.y, 32, 96);
            
            panel.add(Components.newClassToolButton(0, PopupGuis.editIcon, e -> PopupGuis.showEditorForOldClass(day, this)));
            
            panel.add(Components.newClassToolButton(32, PopupGuis.deleteIcon, e -> {
                if(JOptionPane.showConfirmDialog(Main.mainPanel, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
                    Settings.classes.get(day).removeIf(this::equals);
                    
                    Main.updateClasses();
                }
            }));
            
            panel.add(Components.newClassToolButton(64, unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> {
                Settings.classes.get(day).stream()
                        .filter(this::equals)
                        .findFirst()
                        .ifPresent(butt -> {
                            butt.unImportant = !butt.unImportant;
                            Main.updateClasses();
                            frame.dispose();
                        });
            }));
            
            frame.setVisible(true);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj instanceof ClassButton) {
            var button = (ClassButton) obj;
            return className.equals(button.className) && day.equals(button.day) && classType.equals(button.classType) && startTime.equals(button.startTime) && endTime.equals(button.endTime) && room.equals(button.room);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + unImportant;
    }
    
    public static final Map<String, String[]> roomData = Map.of(
                    "TIK", new String[] {"Kongresszusi", "Alagsor"},
                    "Irinyi", new String[] {"214", "217", "218", "219", "222", "224", "225"},
                    "Bolyai", new String[] {"Kerékjártó", "Farkas", "Kiss Árpád"},
                    "Külvilág", new String[] {"Teniszpálya"});

    private static String getBuildingForRoom(String room) {
        return roomData.entrySet().stream()
                       .filter(entry -> Arrays.stream(entry.getValue()).anyMatch(checkRoom -> checkRoom.equals(room)))
                       .map(Entry::getKey)
                       .findFirst()
                       .orElse("Ismeretlen");
    }
}