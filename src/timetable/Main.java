package timetable;

import java.awt.*;
import java.awt.TrayIcon.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;
import javax.imageio.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.plaf.nimbus.*;
import timetable.listeners.*;

public final class Main {
    private static final ArrayList<ClassButton> classButtons = new ArrayList<>();
    public static ClassButton currentClassButton;

    public static final String[] days = {"Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat", "Vasárnap"};
    public static final JLabel dateLabel = new JLabel();
    public static final JPanel mainPanel = new JPanel(null);
    public static final TrayIcon tray = new TrayIcon(Components.trayIcon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
    
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        IntStream.range(0, 5).forEach(Main::addDayButton);
        updateClasses();
        
        dateLabel.setBounds(325, 5, 300, 40);
        dateLabel.setFont(Components.tableHeaderFont);
        mainPanel.add(dateLabel);
        
        var screenshotItem = Components.newMenuItem("Órarend Fénykép", "screencap.png", Main::createScreenshot);
        var frame = new JFrame("Órarend");
        frame.setBounds(0, 0, 950, 713);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(mainPanel);
        frame.addWindowListener(new ScreenshotWindowListener(screenshotItem));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(Components.trayIcon);
        frame.setResizable(false);
        
        if(args.length == 0 || !args[0].equals("-nowindow")) {
            frame.setVisible(true);
        }
        
        //System tray menu
        var sleepMode = new JCheckBoxMenuItem("Alvó Mód", Components.getIcon("sleep.png", 24), false);
        var popMenu = new JPopupMenu();

        popMenu.setPreferredSize(new Dimension(170, 200));
        popMenu.add(Components.newMenuItem("Megnyitás", "open.png", Main::openFromTray));
        popMenu.addSeparator();
        popMenu.add(sleepMode);
        popMenu.add(screenshotItem);
        popMenu.add(Components.newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
        popMenu.addSeparator();
        popMenu.add(Components.newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));
        SwingUtilities.updateComponentTreeUI(popMenu);
        
        tray.addMouseListener(new SystemTrayListener(popMenu));
        SystemTray.getSystemTray().add(tray);
        Runtime.getRuntime().addShutdownHook(new Thread(Settings::saveSettings));
        
        
        Thread.currentThread().setName("Time Label Updater");
        var displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");
        var timer = Settings.updateInterval - 100;
        
        while(true) {
            if(frame.isVisible()) {
                dateLabel.setText(LocalDateTime.now().format(displayTimeFormat));
            }

            if(++timer == Settings.updateInterval) {
                if(!sleepMode.isSelected() && !frame.isVisible()) {
                    var now = LocalTime.now();
                    
                    if(Settings.enablePopups && currentClassButton != null && now.isBefore(currentClassButton.startTime)) {
                        var timeBetween = Duration.between(now, currentClassButton.startTime);

                        if(timeBetween.toMinutes() < Settings.timeBeforeNotification) {
                            tray.displayMessage("Órarend", "Figyelem! Következõ óra: " + timeBetween.toHoursPart() + " óra " +  timeBetween.toMinutesPart() + " perc múlva!\nÓra: " + currentClassButton.className + ' ' + currentClassButton.startTime + '-' + currentClassButton.endTime, MessageType.NONE);

                            try(var stream = AudioSystem.getAudioInputStream(Main.class.getClassLoader().getResource("assets/beep.wav"));
                                var line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, stream.getFormat(), 8900))){

                                line.open();
                                ((FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-20);
                                line.start();
                                var buf = new byte[8900];
                                stream.read(buf);
                                line.write(buf, 0, 8900);
                                line.drain();
                            }catch(IOException | UnsupportedAudioFileException | LineUnavailableException e1) {}
                        }
                    }
                    Components.handleNightMode(dateLabel, now);
                }
                timer = 0;
            }

            Thread.sleep(1000);
        }
    }
    
    private static void addDayButton(int dayIndex) {
        var currentDay = days[dayIndex];
        var topAdd = new JButton(currentDay);
        
        topAdd.setFocusable(false);
        topAdd.addMouseListener(new CreateClassListener(currentDay));
        topAdd.setBackground(Color.GRAY);
        topAdd.setForeground(Color.BLACK);
        topAdd.setFont(Components.tableHeaderFont);
        topAdd.setBounds(20 + (dayIndex * 180), 80, 150, 40);
        mainPanel.add(topAdd);
    }
    
    private static void createScreenshot(@SuppressWarnings("unused") ActionEvent event) {
        var window = mainPanel.getTopLevelAncestor().getLocationOnScreen();
        var fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) + ".png";
        
        try {
            ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 20, window.y + 90, 890, 600)), "PNG", new File(fileName));
            JOptionPane.showMessageDialog(mainPanel.getTopLevelAncestor(), "TimeTable saved as: " + fileName);
        } catch (HeadlessException | AWTException | IOException e1) {}
    }
    
    private static void openFromTray(@SuppressWarnings("unused") ActionEvent event) {
        updateClasses();
        var top = (JFrame) mainPanel.getTopLevelAncestor();
        top.setVisible(true);
        top.setExtendedState(JFrame.NORMAL);
    }
    
    public static void updateClasses() {
        classButtons.forEach(k -> mainPanel.remove(k.button));
        classButtons.clear();
        currentClassButton = null;
        
        var today = days[LocalDateTime.now().getDayOfWeek().ordinal()];
        var now = LocalTime.now();
        
        Components.handleNightMode(mainPanel, now);
        Components.handleNightMode(dateLabel, now);
        
        Settings.classes
                .forEach((day, classesPerDay) -> {
                    var yPosition = new int[] {20};
                    var xPosition = 20 + Settings.indexOf(day, days) * 180;

                    classesPerDay.stream()
                                 .sorted(Comparator.comparingInt((ClassButton button) -> Settings.indexOf(button.day, days))
                                                   .thenComparing(button -> button.startTime)
                                                   .thenComparing(button -> button.className))
                                 .forEach(clazz -> {
                                     clazz.button.setBounds(xPosition, yPosition[0] += 110, 150, 100);
                                  
                                     var isToday = day.equalsIgnoreCase(today);
                                     var isBefore = isToday && now.isBefore(clazz.startTime);
                                     var isAfter = isToday && (now.isAfter(clazz.startTime) || now.equals(clazz.startTime));
                                     var isNext = currentClassButton == null && !clazz.unImportant && isBefore || (isToday && now.equals(clazz.startTime));
                                    
                                     if(isNext) {
                                         currentClassButton = clazz;
                                        
                                         var between = Duration.between(now, clazz.startTime);
                                         Main.tray.setToolTip("Következõ óra " + between.toHoursPart() + " óra " + between.toMinutesPart() + " perc múlva: " + clazz.className + ' ' + clazz.classType + "\nIdõpont: " + clazz.startTime + '-' + clazz.endTime + "\nTerem: " + clazz.room);
                                     }
                                     clazz.button.setBackground(clazz.unImportant ? Settings.unimportantClassColor : isNext ? Settings.currentClassColor : isBefore ? Settings.upcomingClassColor : isAfter ? Settings.pastClassColor : Settings.otherDayClassColor);
                                     clazz.button.setForeground(clazz.unImportant ? Color.LIGHT_GRAY : Color.BLACK);
                                  
                                     mainPanel.add(clazz.button);
                                     classButtons.add(clazz);
                              });
                    });
        
        mainPanel.repaint();
    }
}