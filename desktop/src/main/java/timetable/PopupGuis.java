package timetable;

import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.qrcode.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.function.*;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.border.*;
import timetable.listeners.*;

public final class PopupGuis {
    public static final ImageIcon editIcon   = Components.getIcon("edit.png", 32);
    public static final ImageIcon deleteIcon = Components.getIcon("delete.png", 32);
    public static final ImageIcon ignoreIcon = Components.getIcon("ignore.png", 32);
    public static final ImageIcon unIgnore   = Components.getIcon("unignore.png", 32);

    private PopupGuis() {}

    public static void showEditorForNewClass(ClassButton dataButton) {
        var editorTable = Components.createClassEditorTable(dataButton);

        showClassEditorDialog(frame -> {
            if(editorTable.getCellEditor() != null) editorTable.getCellEditor().stopCellEditing();

            Settings.classes.add(ClassButton.fromEditorTable(editorTable, dataButton.unImportant));

            Main.updateClassesGui();
            frame.dispose();
        }, editorTable);
    }

    public static void showEditorForOldClass(ClassButton dataButton) {
        var editorTable = Components.createClassEditorTable(dataButton);

        showClassEditorDialog(frame -> {
            if(editorTable.getCellEditor() != null) editorTable.getCellEditor().stopCellEditing();

            Settings.classes.remove(dataButton);
            Settings.classes.add(ClassButton.fromEditorTable(editorTable, dataButton.unImportant));

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

        var dayModeStartTimeBox = Components.newComboBox(Settings.dayTimeStart.toString(), 60, timeValues);
        var dayModeEndTimeBox = Components.newComboBox(Settings.dayTimeEnd.toString(), 120, timeValues);
        var minutesBeforeClassNoteBox = Components.newComboBox(Integer.toString(Settings.minutesBeforeNextClassNotification), 270, "30", "40", "50", "60", "70", "80", "90");

        var now = LocalTime.now();
        var popupCheckBox = new JCheckBox((String)null, Settings.enablePopups);
        popupCheckBox.setOpaque(false);
        Components.handleNightMode(popupCheckBox, now);
        popupCheckBox.setSize(200, 20);
        var startupBox = new JCheckBox((String)null, Files.exists(Path.of(startupLinkPath)));
        startupBox.setOpaque(false);
        Components.handleNightMode(startupBox, now);
        startupBox.setSize(200, 20);

        var scrollPanel = new JPanel(null);
        Components.handleNightMode(scrollPanel, now);
        scrollPanel.setPreferredSize(new java.awt.Dimension(500, 1000));

        Components.addSettingsSection("Színek", 10, now, scrollPanel);
        Components.addSettingButton(currentClass, 50, "Jelenlegi Óra Színe", scrollPanel, now);
        Components.addSettingButton(beforeClass, 100, "Következő Órák Színe", scrollPanel, now);
        Components.addSettingButton(otherClass, 150, "Más Napok Óráinak Színe", scrollPanel, now);
        Components.addSettingButton(pastClass, 200, "Elmúlt Órák Színe", scrollPanel, now);
        Components.addSettingButton(unimportantClass, 250, "Nem Fontos Órák Színe", scrollPanel, now);
        Components.addSettingButton(dayTimeColor, 300, "Nappali Mód Háttérszíne", scrollPanel, now);
        Components.addSettingButton(nightTimeColor, 350, "Éjszakai Mód Háttérszíne", scrollPanel, now);

        Components.addSettingsSection("Idő", 410, now, scrollPanel);
        Components.addSettingButton(dayModeStartTimeBox, 450, "Nappali Időszak Kezdete", scrollPanel, now);
        Components.addSettingButton(dayModeEndTimeBox, 500, "Nappali Időszak Vége", scrollPanel, now);
        Components.addSettingButton(minutesBeforeClassNoteBox, 550, "Óra előtti értesítés előtti idő percekben", scrollPanel, now);

        Components.addSettingsSection("Felhő", 590, now, scrollPanel);
        Components.addSettingInfoLabel(640, "Felhő Azonosító: " + (Settings.cloudID.equals(Settings.NULL_CLOUD_ID) ? "nincs" : Settings.cloudID), scrollPanel, now);

        Components.addSettingsSection("Egyéb", 700, now, scrollPanel);
        Components.addSettingButton(popupCheckBox, 750, "Üzenetek Bekapcsolva", scrollPanel, now);
        Components.addSettingButton(startupBox, 800, "Indítás PC Indításakor", scrollPanel, now);

        Components.addSettingsSection("Veszély Zóna", 840, now, scrollPanel);
        var deleteClassesButton = new JButton("Órarend Törlése");
        deleteClassesButton.setBounds(100, 880, 120, 40);
        deleteClassesButton.setBackground(Color.GRAY);
        deleteClassesButton.setForeground(Color.RED);
        deleteClassesButton.addActionListener(e -> handleClassReset(scrollPanel));

        var qrCodeImage = new JLabel(generateQRCodeImage());
        qrCodeImage.setBounds(350, 602, 300, 150);

        scrollPanel.add(deleteClassesButton);
        scrollPanel.add(qrCodeImage);

        Runnable saveAction = () -> {
            try {
                Settings.dayTimeStart = LocalTime.parse((CharSequence) dayModeStartTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
                Settings.dayTimeEnd = LocalTime.parse((CharSequence) dayModeEndTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
                Settings.enablePopups = popupCheckBox.isSelected();
                Settings.currentClassColor = currentClass.getBackground();
                Settings.currentClassColor = currentClass.getBackground();
                Settings.upcomingClassColor = beforeClass.getBackground();
                Settings.otherDayClassColor = otherClass.getBackground();
                Settings.pastClassColor = pastClass.getBackground();
                Settings.unimportantClassColor = unimportantClass.getBackground();
                Settings.dayTimeColor = dayTimeColor.getBackground();
                Settings.nightTimeColor = nightTimeColor.getBackground();
                Settings.minutesBeforeNextClassNotification = Integer.parseInt((String) minutesBeforeClassNoteBox.getSelectedItem());
                Settings.saveSettings();

                Main.updateClassesGui();
                settingsFrame.dispose();
            }catch (NumberFormatException | DateTimeParseException e) {
                JOptionPane.showMessageDialog(settingsFrame, "Valamelyik adat nem megfelelő formátumú!", "Rossz adat", JOptionPane.INFORMATION_MESSAGE);
            }

            if(startupBox.isSelected()) {
                Settings.createStartupLink(startupLinkPath);
            }else{
                var maybeExistingFilePath = Path.of(startupLinkPath);
                    if(Files.exists(maybeExistingFilePath)) {
                    try {
                        Files.delete(maybeExistingFilePath);
                    } catch (IOException e) {}
                }
            }
        };

        var scrollPane = new JScrollPane(scrollPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setBorder(null);
        settingsFrame.addWindowListener(new WindowClosedListener(saveAction));
        settingsFrame.setBounds(0, 0, 800, 600);
        settingsFrame.setIconImage(Components.trayIcon);
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setContentPane(scrollPane);
        settingsFrame.setVisible(true);
    }

    private static ImageIcon generateQRCodeImage() {
        if(Settings.cloudID.equals(Settings.NULL_CLOUD_ID)) {
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        }

        try {
            var bitmapImage = MatrixToImageWriter.toBufferedImage(new QRCodeWriter().encode(Settings.cloudID, BarcodeFormat.QR_CODE, 300, 150));
            var outputImage = new BufferedImage(300, 150, BufferedImage.TYPE_INT_RGB);
            var time = LocalTime.now();
            var isDarkMode = time.isAfter(Settings.dayTimeEnd) || time.isBefore(Settings.dayTimeStart);
            var backgroundColor = (isDarkMode ? Settings.nightTimeColor : Settings.dayTimeColor).getRGB();
            var foregroundColor = (isDarkMode ? Color.WHITE : Color.BLACK).getRGB();

            for(var x = 0; x < 300; ++x) {
                for(var y = 0; y < 150; ++y) {
                    var originalPixel = bitmapImage.getRGB(x, y);
                    var replacementPixel = originalPixel == -1 ? backgroundColor : foregroundColor;

                    outputImage.setRGB(x, y, replacementPixel);
                }
            }

            return new ImageIcon(outputImage);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void handleClassReset(JPanel scrollPanel) {
        if(JOptionPane.showConfirmDialog(scrollPanel, "Biztos törlöd az összes órát?", "Órarend", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Settings.classes.clear();
            Main.updateClassesGui();
        }
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

        var frame = new JDialog(settingsFrame, null, false);
        var buttonPosition = button.getLocationOnScreen();

        frame.setContentPane(panel);
        frame.setUndecorated(true);
        frame.setLocationRelativeTo(Main.classesPanel);
        frame.addWindowFocusListener(new FocusLostListener(frame));
        frame.setBounds(buttonPosition.x + 48, buttonPosition.y, 192, 192);
        frame.setVisible(true);
    }

    private static void showClassEditorDialog(Consumer<JDialog> saveListener, JComponent... components) {
        var frame = new JDialog((JFrame)Main.classesPanel.getTopLevelAncestor(), "Óra szerkesztő", true);
        var panel = new JPanel(null);

        frame.setIconImage(Components.trayIcon);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setBounds(0, 0, 400, 300);
        frame.setLocationRelativeTo(Main.classesPanel);

        if(saveListener != null) {
            var saveButton = new JButton("Mentés");
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