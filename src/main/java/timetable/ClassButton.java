package timetable;

import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.json.*;
import javax.swing.*;
import org.apache.poi.ss.usermodel.*;
import timetable.listeners.*;

public final class ClassButton extends MouseAdapter {
    public static final Font importantClassFont = new Font("SansSerif", Font.BOLD, 12);
    public static final Comparator<ClassButton> timeBasedOrder = Comparator.comparingInt((ClassButton button) -> Settings.indexOf(button.day, Main.days))
                                                                           .thenComparing(button -> button.startTime)
                                                                           .thenComparing(button -> button.className);
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
        this.button = initButton();
    }
    
    public ClassButton(Row classRow, DateTimeFormatter format) {
        var beginDate = LocalDateTime.parse(classRow.getCell(0).getStringCellValue(), format);
        var endDate = LocalDateTime.parse(classRow.getCell(1).getStringCellValue(), format);
        var summary = classRow.getCell(2).getStringCellValue();
        var codeBeginParamIndex = summary.indexOf('(');
        var code = summary.substring(codeBeginParamIndex + 1, summary.indexOf(')', codeBeginParamIndex));
        var lastCodeChar = Character.toUpperCase(code.charAt(code.length() - 1));
        
        this.day = Main.days[beginDate.getDayOfWeek().ordinal()];
        this.className = summary.substring(0, codeBeginParamIndex - 1);
        this.classType = lastCodeChar == 'G' || lastCodeChar == 'L' ? "Gyakorlat" : "Elõadás";
        this.startTime = beginDate.toLocalTime();
        this.endTime = endDate.toLocalTime();
        this.room = classRow.getCell(3).getStringCellValue();
        this.unImportant = false;
        this.button = initButton();
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
            var panel = new JPanel(null);
            panel.add(Components.newClassToolButton(0, PopupGuis.editIcon, e -> PopupGuis.showEditorForOldClass(day, this)));
            
            panel.add(Components.newClassToolButton(32, PopupGuis.deleteIcon, e -> {
                if(JOptionPane.showConfirmDialog(Main.classesPanel, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
                    Settings.classes.get(day).removeIf(this::equals);
                    Main.updateClassesGui();
                }
            }));
            
            panel.add(Components.newClassToolButton(64, unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> {
                Settings.classes.get(day).stream()
                        .filter(this::equals)
                        .findFirst()
                        .ifPresent(butt -> {
                            butt.unImportant = !butt.unImportant;
                            Main.updateClassesGui();
                            ((JDialog)panel.getTopLevelAncestor()).dispose();
                        });
            }));
            
            var frame = new JDialog((JFrame)Main.classesPanel.getTopLevelAncestor());
            var mouse = MouseInfo.getPointerInfo().getLocation();
            
            frame.setContentPane(panel);
            frame.addWindowFocusListener(new FocusLostListener(frame));
            frame.setUndecorated(true);
            frame.setLocationRelativeTo(null);
            frame.setBounds(mouse.x, mouse.y - 48, 32, 96);
            frame.setVisible(true);
        }
    }
    
    private JButton initButton() {
        var button = new JButton("<html>Óra: " + className.replace('_', ' ') +
                                 "<br>Idõ: " + startTime + "-" + endTime +
                                 "<br>Típus: " + classType +
                                 "<br>Terem: " + room);

        if(classType.charAt(0) == 'G') {
            button.setFont(importantClassFont);
        }
        
        button.setFocusable(false);
        button.addMouseListener(this);
        
        return button;
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
}