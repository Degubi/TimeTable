package timetable;

import com.fasterxml.jackson.annotation.*;
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
    public static final Comparator<ClassButton> timeBasedOrder = Comparator.comparing((ClassButton button) -> button.startTime)
                                                                           .thenComparing(button -> button.name);
    public final String day;
    public final LocalTime startTime;
    public final LocalTime endTime;
    public final String name;
    public final String type;
    public final String room;
    public boolean unImportant;
    public final transient JButton button;

    @JsonCreator
    public ClassButton(@JsonProperty("day") String day,
                       @JsonProperty("name") String name,
                       @JsonProperty("type") String type,
                       @JsonProperty("startTime") LocalTime startTime,
                       @JsonProperty("endTime") LocalTime endTime,
                       @JsonProperty("room") String room,
                       @JsonProperty("unImportant") boolean unImportant) {

        this.day = day;
        this.name = name;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.unImportant = unImportant;

        var button = new JButton("<html>Óra: " + name.replace('_', ' ') +
                                 "<br>Idő: " + startTime + "-" + endTime +
                                 "<br>Típus: " + type +
                                 "<br>Terem: " + room + "</html>");

        if(type.charAt(0) == 'G') {
            button.setFont(importantClassFont);
        }

        button.setFocusable(false);
        button.addMouseListener(this);
        this.button = button;
    }

    public static ClassButton fromEditorTable(JTable table, boolean unImportant) {
        return new ClassButton((String) table.getValueAt(1, 1),
                               (String) table.getValueAt(0, 1),
                               (String) table.getValueAt(2, 1),
                               LocalTime.parse((String) table.getValueAt(3, 1)),
                               LocalTime.parse((String) table.getValueAt(4, 1)),
                               (String) table.getValueAt(5, 1), unImportant);
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
            panel.add(Components.newClassToolButton(0, PopupGuis.editIcon, e -> PopupGuis.showEditorForOldClass(this)));
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
        unImportant = !unImportant;

        Main.updateClassesGui();
        ((JDialog)panel.getTopLevelAncestor()).dispose();
    }

    private void onDeleteButtonPressed() {
        if(JOptionPane.showConfirmDialog(Main.classesPanel, "Tényleg Törlöd?", "Törlés Megerősítés", JOptionPane.YES_NO_OPTION) == 0) {
            Settings.classes.remove(this);
            Main.updateClassesGui();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
              (obj instanceof ClassButton button && name.equals(button.name) &&
                                                    day.equals(button.day) &&
                                                    type.equals(button.type) &&
                                                    startTime.equals(button.startTime) &&
                                                    endTime.equals(button.endTime) &&
                                                    room.equals(button.room));
    }
}