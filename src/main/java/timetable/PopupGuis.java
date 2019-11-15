package timetable;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.function.*;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.border.*;
import timetable.listeners.*;

public final class PopupGuis{
    public static final ImageIcon editIcon = Components.getIcon("edit.png", 32);
    public static final ImageIcon deleteIcon = Components.getIcon("delete.png", 32);
    public static final ImageIcon ignoreIcon = Components.getIcon("ignore.png", 32);
    public static final ImageIcon unIgnore = Components.getIcon("unignore.png", 32);
    
    private PopupGuis() {}
    
    public static void showEditorForNewClass(ClassButton dataButton) {
        var editorTable = Components.createClassEditorTable(dataButton);
        
        showClassEditorDialog(frame -> {
            if(editorTable.getCellEditor() != null) editorTable.getCellEditor().stopCellEditing();
            
            Settings.classes.get(editorTable.getValueAt(1, 1))
                    .add(new ClassButton(editorTable, dataButton.unImportant));
            
            Main.updateClassesGui();
            frame.dispose();
        }, editorTable);
    }
    
    public static void showEditorForOldClass(String day, ClassButton dataButton) {
        var editorTable = Components.createClassEditorTable(dataButton);
        
        showClassEditorDialog(frame -> {
            if(editorTable.getCellEditor() != null) editorTable.getCellEditor().stopCellEditing();
            
            Settings.classes.get(day).remove(dataButton);
            Settings.classes.get(editorTable.getValueAt(1, 1))
                    .add(new ClassButton(editorTable, dataButton.unImportant));
            
            Main.updateClassesGui();
            frame.dispose();
        }, editorTable);
    }
    
    @SuppressWarnings("boxing")
    public static void showSettingsGui(@SuppressWarnings("unused") ActionEvent event) {
        var startupLinkPath = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\TimeTable.lnk";
        var settingsFrame = new JDialog((JFrame)Main.classesPanel.getTopLevelAncestor(), "Beállítások", true);
        var timeValues = IntStream.range(0, 24)
                                  .mapToObj(k -> String.format("%02d:00", k))
                                  .toArray(String[]::new);
        
        Consumer<JButton> colorButtonListener = button -> handleColorSettingButtonPress(settingsFrame, button);
        
        var currentClass = Components.newColorButton(200, 40, colorButtonListener, Settings.currentClassColor);
        var beforeClass = Components.newColorButton(200, 80, colorButtonListener, Settings.upcomingClassColor);
        var otherClass = Components.newColorButton(200, 140, colorButtonListener, Settings.otherDayClassColor);
        var pastClass = Components.newColorButton(200, 200, colorButtonListener, Settings.pastClassColor);
        var unimportantClass = Components.newColorButton(200, 260, colorButtonListener, Settings.unimportantClassColor);
        var dayTimeColor = Components.newColorButton(200, 320, colorButtonListener, Settings.dayTimeColor);
        var nightTimeColor = Components.newColorButton(200, 380, colorButtonListener, Settings.nightTimeColor);
        
        var startTimeBox = Components.newComboBox(Settings.dayTimeStart.toString(), 60, timeValues);
        var endTimeBox = Components.newComboBox(Settings.dayTimeEnd.toString(), 120, timeValues);
        var timeBeforeNoteBox = Components.newComboBox(Integer.toString(Settings.timeBeforeNotification), 270, "30", "40", "50", "60", "70", "80", "90");
        var updateIntervalBox = Components.newComboBox(Integer.toString(Settings.updateInterval / 60), 340, "5", "10", "15", "20");
        
        var now = LocalTime.now();
        var popupCheckBox = new JCheckBox((String)null, Settings.enablePopups);
        popupCheckBox.setOpaque(false);
        Components.handleNightMode(popupCheckBox, now);
        popupCheckBox.setBounds(150, 660, 200, 20);
        var startupBox = new JCheckBox((String)null, Files.exists(Path.of(startupLinkPath)));
        startupBox.setOpaque(false);
        Components.handleNightMode(startupBox, now);
        startupBox.setBounds(150, 700, 200, 20);

        var scrollPanel = new JPanel(null);
        Components.handleNightMode(scrollPanel, now);
        scrollPanel.setPreferredSize(new Dimension(500, 800));
        var scrollPane = new JScrollPane(scrollPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setBorder(null);
        
        settingsFrame.setResizable(false);
        settingsFrame.setBounds(0, 0, 800, 600);
        settingsFrame.setIconImage(Components.trayIcon);
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setContentPane(scrollPane);
        
        Components.addSettingButton(currentClass, 30, "Jelenlegi Óra Színe", scrollPanel, now);
        Components.addSettingButton(beforeClass, 80, "Következõ Órák Színe", scrollPanel, now);
        Components.addSettingButton(otherClass, 130, "Más Napok Óráinak Színe", scrollPanel, now);
        Components.addSettingButton(pastClass, 180, "Elmúlt Órák Színe", scrollPanel, now);
        Components.addSettingButton(unimportantClass, 230, "Nem Fontos Órák Színe", scrollPanel, now);
        Components.addSettingButton(dayTimeColor, 280, "Nappali Mód Háttérszíne", scrollPanel, now);
        Components.addSettingButton(nightTimeColor, 330, "Éjszakai Mód Háttérszíne", scrollPanel, now);
        
        Components.addSettingButton(startTimeBox, 430, "Nappali Idõszak Kezdete", scrollPanel, now);
        Components.addSettingButton(endTimeBox, 480, "Nappali Idõszak Vége", scrollPanel, now);
        Components.addSettingButton(timeBeforeNoteBox, 530, "Értesítések Frissítési Idõzítései", scrollPanel, now);
        Components.addSettingButton(updateIntervalBox, 580, "Óra Elõtti Értesítések Percben", scrollPanel, now);
        
        Components.addSettingButton(popupCheckBox, 680, "Üzenetek Bekapcsolva", scrollPanel, now);
        Components.addSettingButton(startupBox, 730, "Indítás PC Indításakor", scrollPanel, now);
        
        var saveButton = new JButton("Mentés");
        saveButton.setBounds(600, 720, 120, 40);
        saveButton.setBackground(Color.GRAY);
        saveButton.setForeground(Color.BLACK);
        saveButton.addActionListener(ev -> {
            try {
                Settings.dayTimeStart = LocalTime.parse((CharSequence) startTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
                Settings.dayTimeEnd = LocalTime.parse((CharSequence) endTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
                Settings.enablePopups = popupCheckBox.isSelected();
                Settings.currentClassColor = currentClass.getBackground();
                Settings.currentClassColor = currentClass.getBackground();
                Settings.upcomingClassColor = beforeClass.getBackground();
                Settings.otherDayClassColor = otherClass.getBackground();
                Settings.pastClassColor = pastClass.getBackground();
                Settings.unimportantClassColor = unimportantClass.getBackground();
                Settings.dayTimeColor = dayTimeColor.getBackground();
                Settings.nightTimeColor = nightTimeColor.getBackground();
                Settings.timeBeforeNotification = Integer.parseInt((String) timeBeforeNoteBox.getSelectedItem());
                Settings.updateInterval = Integer.parseInt((String) updateIntervalBox.getSelectedItem()) * 60;
                Settings.saveSettings();
                
                Main.updateClassesGui();
                settingsFrame.dispose();
            }catch (NumberFormatException | DateTimeParseException e) {
                JOptionPane.showMessageDialog(settingsFrame, "Valamelyik adat nem megfelelõ formátumú!", "Rossz adat", JOptionPane.INFORMATION_MESSAGE);
            }
            
            if(startupBox.isSelected()) {
                Settings.createStartupLink(startupLinkPath);
            }else{
                try {
                    Files.delete(Path.of(startupLinkPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        scrollPanel.add(saveButton);
        settingsFrame.setVisible(true);
    }

    private static void handleColorSettingButtonPress(JDialog settingsFrame, JButton button) {
        var panel = new JPanel(null);
        panel.setBorder(new LineBorder(Color.GRAY));

        Consumer<JButton> colorButtonPressedListener = e -> {
            button.setBackground(e.getBackground());
            ((JDialog)panel.getTopLevelAncestor()).dispose();
        };
        
        panel.add(Components.newColorButton(0, 0, colorButtonPressedListener, new Color(235, 235, 235)));
        panel.add(Components.newColorButton(48, 0, colorButtonPressedListener, new Color(0, 147, 3)));
        panel.add(Components.newColorButton(96, 0, colorButtonPressedListener, new Color(255, 69, 69)));
        panel.add(Components.newColorButton(144, 0, colorButtonPressedListener, new Color(64, 64, 64)));
        panel.add(Components.newColorButton(0, 48, colorButtonPressedListener, new Color(84, 113, 142)));
        panel.add(Components.newColorButton(48, 48, colorButtonPressedListener, new Color(247, 238, 90)));
        panel.add(Components.newColorButton(96, 48, colorButtonPressedListener, new Color(192, 192, 192)));
        panel.add(Components.newColorButton(144, 48, colorButtonPressedListener, Color.ORANGE));
        panel.add(Components.newColorButton(0, 96, colorButtonPressedListener, Color.CYAN));
        panel.add(Components.newColorButton(48, 96, colorButtonPressedListener, Color.MAGENTA));
        panel.add(Components.newColorButton(96, 96, colorButtonPressedListener, Color.YELLOW));
        panel.add(Components.newColorButton(144, 96, colorButtonPressedListener, Color.RED));
        panel.add(Components.newColorButton(0, 144, colorButtonPressedListener, Color.GREEN));
        panel.add(Components.newColorButton(48, 144, colorButtonPressedListener, Color.GRAY));
        panel.add(Components.newColorButton(96, 144, colorButtonPressedListener, Color.PINK));
        panel.add(Components.newColorButton(144, 144, colorButtonPressedListener, new Color(100, 70, 80)));
        
        Components.handleNightMode(panel, LocalTime.now());
        
        var mouse = MouseInfo.getPointerInfo().getLocation();
        var frame = new JDialog(settingsFrame, null, false);
        
        frame.setContentPane(panel);
        frame.setUndecorated(true);
        frame.setLocationRelativeTo(Main.classesPanel);
        frame.addWindowFocusListener(new FocusLostListener(frame));
        frame.setBounds(mouse.x, mouse.y, 192, 192);
        frame.setVisible(true);
    }
    
    private static void showClassEditorDialog(Consumer<JDialog> saveListener, JComponent... components) {
        var frame = new JDialog((JFrame)Main.classesPanel.getTopLevelAncestor(), "Óra szerkesztõ", true);
        var panel = new JPanel(null);
        
        frame.setIconImage(Components.trayIcon);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setBounds(0, 0, 400, 300);
        frame.setLocationRelativeTo(Main.classesPanel);
        
        if(saveListener != null) {
            JButton saveButton = new JButton("Mentés");
            saveButton.setFocusable(false);
            saveButton.setBounds(400 / 2 - 70, 300 - 90, 120, 40);
            saveButton.setBackground(Color.GRAY);
            saveButton.setForeground(Color.BLACK);
            saveButton.addActionListener(e -> saveListener.accept(frame));
            panel.add(saveButton);
        }
        
        for(var component : components) panel.add(component);
        Components.handleNightMode(panel, LocalTime.now());
        frame.setVisible(true);
    }
}