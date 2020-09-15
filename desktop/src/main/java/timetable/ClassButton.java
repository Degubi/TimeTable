package timetable;

import jakarta.json.*;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.swing.*;
import org.apache.poi.ss.usermodel.*;
import timetable.listeners.*;

public final class ClassButton extends MouseAdapter {
    public static final Font importantClassFont = new Font("SansSerif", Font.BOLD, 12);
    public static final Comparator<ClassButton> timeBasedOrder = Comparator.comparingInt((ClassButton button) -> Settings.indexOf(button.day, Main.days))
                                                                           .thenComparing(button -> button.startTime)
                                                                           .thenComparing(button -> button.name);
    public final String day;
    public final LocalTime startTime;
    public final LocalTime endTime;
    public final String name;
    public final String type;
    public final String room;
    public boolean unImportant;
    public final JButton button;

    public ClassButton(String day, String name, String type, LocalTime startTime, LocalTime endTime, String room, boolean unImportant) {
        this.day = day;
        this.name = name;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.unImportant = unImportant;
        this.button = initButton();
    }

    public static ClassButton fromEditorTable(JTable table, boolean unImportant) {
        return new ClassButton((String) table.getValueAt(1, 1),
                               (String) table.getValueAt(0, 1),
                               (String) table.getValueAt(2, 1),
                               LocalTime.parse((String) table.getValueAt(3, 1), DateTimeFormatter.ISO_LOCAL_TIME),
                               LocalTime.parse((String) table.getValueAt(4, 1), DateTimeFormatter.ISO_LOCAL_TIME),
                               (String) table.getValueAt(5, 1), unImportant);
    }

    public static ClassButton fromJson(JsonObject object) {
        return new ClassButton(object.getString("day"), object.getString("name"), object.getString("type"),
                               LocalTime.parse(object.getString("startTime"), DateTimeFormatter.ISO_LOCAL_TIME),
                               LocalTime.parse(object.getString("endTime"), DateTimeFormatter.ISO_LOCAL_TIME),
                               object.getString("room"), object.getBoolean("unImportant"));
    }

    public static ClassButton fromTimetableExcel(Row classRow, DateTimeFormatter format) {
        var beginDate = LocalDateTime.parse(classRow.getCell(0).getStringCellValue(), format);
        var endDate = LocalDateTime.parse(classRow.getCell(1).getStringCellValue(), format);
        var summary = classRow.getCell(2).getStringCellValue();
        var codeBeginParamIndex = summary.indexOf('(');
        var code = summary.substring(codeBeginParamIndex + 1, summary.indexOf(')', codeBeginParamIndex));
        var lastCodeChar = Character.toUpperCase(code.charAt(code.length() - 1));

        var day = Main.days[beginDate.getDayOfWeek().ordinal()];
        var className = summary.substring(0, codeBeginParamIndex - 1);
        var classType = code.contains("SZV") ? "Szabvál" : lastCodeChar == 'G' || lastCodeChar == 'L' ? "Gyakorlat" : "Előadás";
        var room = classRow.getCell(3).getStringCellValue();

        return new ClassButton(day, className, classType, beginDate.toLocalTime(), endDate.toLocalTime(), room, false);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if(event.getButton() == MouseEvent.BUTTON3) {
            var panel = new JPanel(null);
            panel.add(Components.newClassToolButton(0, PopupGuis.editIcon, e -> PopupGuis.showEditorForOldClass(day, this)));
            panel.add(Components.newClassToolButton(32, PopupGuis.deleteIcon, e -> onDeleteButtonPressed()));
            panel.add(Components.newClassToolButton(64, unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> onImportantButtonPressed(panel)));

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

    private void onImportantButtonPressed(JPanel panel) {
        Settings.classes.get(day).stream()
                .filter(this::equals)
                .findFirst()
                .ifPresent(butt -> {
                    butt.unImportant = !butt.unImportant;
                    Main.updateClassesGui();
                    ((JDialog)panel.getTopLevelAncestor()).dispose();
                });
    }

    private void onDeleteButtonPressed() {
        if(JOptionPane.showConfirmDialog(Main.classesPanel, "Tényleg Törlöd?", "Törlés Megerősítés", JOptionPane.YES_NO_OPTION) == 0) {
            Settings.classes.get(day).removeIf(this::equals);
            Main.updateClassesGui();
        }
    }

    private JButton initButton() {
        var button = new JButton("<html>Óra: " + name.replace('_', ' ') +
                                 "<br>Idő: " + startTime + "-" + endTime +
                                 "<br>Típus: " + type +
                                 "<br>Terem: " + room);

        if(type.charAt(0) == 'G') {
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
            return name.equals(button.name) && day.equals(button.day) && type.equals(button.type) && startTime.equals(button.startTime) && endTime.equals(button.endTime) && room.equals(button.room);
        }
        return false;
    }

    @Override
    public String toString() {
        return day + ' ' + name + ' ' + type + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + unImportant;
    }
}