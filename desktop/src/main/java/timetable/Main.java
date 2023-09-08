package timetable;

import java.awt.*;
import java.awt.Color;
import java.awt.TrayIcon.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.nimbus.*;
import timetable.listeners.*;

public final class Main {
    private static final ArrayList<ClassButton> classButtons = new ArrayList<>();
    private static final JLabel dateLabel = new JLabel("\0");
    private static ClassButton lastActiveClass = null;

    public static final String[] days = { "Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat", "Vasárnap" };
    public static final JPanel classesPanel = new JPanel(null);
    public static final String BACKEND_URL = "http://localhost:8080/timetable";

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        IntStream.range(0, 5).mapToObj(Main::newDayButton).forEach(classesPanel::add);

        dateLabel.setBounds(350, 5, 300, 40);
        dateLabel.setFont(Components.bigFont);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateLabel.setBorder(new EmptyBorder(15, 0, 20, 0));

        var mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(dateLabel, BorderLayout.PAGE_START);
        mainPanel.add(classesPanel, BorderLayout.CENTER);

        updateClassesGui();

        var screenshotItem = Components.newMenuItem("Kép", "screencap.png", TransferUtils::exportToImage);
        var frame = new JFrame("Órarend");
        frame.setBounds(0, 0, 1024, Math.min(768, Toolkit.getDefaultToolkit().getScreenSize().height - 50));
        frame.setLocationRelativeTo(null);
        frame.setContentPane(mainPanel);
        frame.addWindowListener(new WindowMinimizedListener(screenshotItem));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(Components.trayIcon);
        frame.setResizable(false);

        if(args.length == 0 || !args[0].equals("-nowindow")) {
            frame.setVisible(true);
        }

        var sleepMode = new JCheckBoxMenuItem("Alvó Mód", Components.getIcon("sleep.png", 24), false);
        var popMenu = new JPopupMenu();
        popMenu.setPreferredSize(new Dimension(170, 190));
        popMenu.add(Components.newMenuItem("Megnyitás", "open.png", SystemTrayListener::openFromTray));
        popMenu.addSeparator();
        popMenu.add(sleepMode);
        popMenu.add(Components.newSideMenu("Importálás", "import.png", Components.newMenuItem("Neptun Órarend Excel", "excel.png", TransferUtils::importFromExcel), Components.newMenuItem("Felhő", "db.png", TransferUtils::importFromCloud)));
        popMenu.add(Components.newSideMenu("Mentés", "export.png", screenshotItem, Components.newMenuItem("Felhő", "db.png", TransferUtils::exportToCloud)));
        popMenu.add(Components.newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
        popMenu.addSeparator();
        popMenu.add(Components.newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));

        var tray = new TrayIcon(Components.trayIcon.getScaledInstance(16, 16, Image.SCALE_SMOOTH), "Órarend");
        tray.addMouseListener(new SystemTrayListener(popMenu));
        SystemTray.getSystemTray().add(tray);
        Runtime.getRuntime().addShutdownHook(new Thread(Settings::saveSettings));
        Thread.currentThread().setName("Time Label Updater");

        var minuteCounter = 59;
        var lastNotificationClass = (ClassButton) null;
        var displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");

        while(true) {
            var nowDate = LocalDateTime.now();

            if(frame.isVisible()) {
                dateLabel.setText(nowDate.format(displayTimeFormat));
            }

            if(++minuteCounter == 60) {
                minuteCounter = 0;

                var currentClass = updateCurrentClass(nowDate);
                if(currentClass != null) {
                    var timeBetween = Duration.between(nowDate.toLocalTime(), currentClass.startTime);

                    if(!sleepMode.isSelected() && Settings.enablePopups && timeBetween.toMinutes() <= Settings.minutesBeforeNextClassNotification && lastNotificationClass != currentClass) {
                        lastNotificationClass = currentClass;
                        tray.displayMessage("Órarend", "Figyelem! Következő óra: " + timeBetween.toHoursPart() + " óra " +  timeBetween.toMinutesPart() +
                                            " perc múlva!\nÓra: " + currentClass.name + ' ' + currentClass.startTime + '-' + currentClass.endTime, MessageType.NONE);
                    }

                    tray.setToolTip("Következő óra " + timeBetween.toHoursPart() + " óra " + timeBetween.toMinutesPart() +
                                    " perc múlva: " + currentClass.name + ' ' + currentClass.type +
                                    "\nIdőpont: " + currentClass.startTime + '-' + currentClass.endTime + "\nTerem: " + currentClass.room);
                }else{
                    tray.setToolTip("Órarend");
                }
            }

            Thread.sleep(1000);
        }
    }

    private static JButton newDayButton(int dayIndex) {
        var currentDay = days[dayIndex];
        var topAdd = new JButton(currentDay);

        topAdd.setFocusable(false);
        topAdd.addMouseListener(new CreateClassListener(currentDay));
        topAdd.setBackground(Color.GRAY);
        topAdd.setForeground(Color.BLACK);
        topAdd.setFont(Components.bigFont);
        topAdd.setBounds(20 + (dayIndex * 200), 0, 175, 40);
        return topAdd;
    }

    public static void updateClassesGui() {
        classButtons.forEach(k -> classesPanel.remove(k.button));
        classButtons.clear();

        var nowDate = LocalDateTime.now();
        var today = days[nowDate.getDayOfWeek().ordinal()];
        var nowTime = nowDate.toLocalTime();

        Components.handleNightMode(classesPanel, nowTime);
        Components.handleNightMode(classesPanel.getParent(), nowTime);
        Components.handleNightMode(dateLabel, nowTime);

        Settings.classes.stream()
                .collect(Collectors.groupingBy(k -> k.day))
                .forEach((day, classesPerDay) -> {
                    var yPosition = new int[] { -40 };
                    var xPosition = 20 + indexOf(day, days) * 200;
                    var isToday = day.equals(today);

                    classesPerDay.stream()
                                 .sorted(ClassButton.timeBasedOrder)
                                 .forEach(clazz -> positionAndAddButtonToPanel(nowTime, yPosition, xPosition, isToday, clazz));
                });

        updateCurrentClass(nowDate);
        classesPanel.repaint();
    }

    public static<T> int indexOf(T find, T[] array) {
        for(var k = 0; k < array.length; ++k) {
            if(array[k].equals(find)) {
                return k;
            }
        }
        return -1;
    }

    private static void positionAndAddButtonToPanel(LocalTime nowTime, int[] yPosition, int xPosition, boolean isToday, ClassButton clazz) {
        clazz.button.setBounds(xPosition, yPosition[0] += 95, 175, 85);

        var isBefore = isToday && nowTime.isBefore(clazz.startTime);
        var isAfter = isToday && (nowTime.isAfter(clazz.startTime) || nowTime.equals(clazz.startTime));

        clazz.button.setBackground(clazz.unImportant ? Settings.unimportantClassColor : isBefore ? Settings.upcomingClassColor : isAfter ? Settings.pastClassColor : Settings.otherDayClassColor);
        clazz.button.setForeground(clazz.unImportant ? Color.LIGHT_GRAY : Color.BLACK);

        classesPanel.add(clazz.button);
        classButtons.add(clazz);
    }

    private static ClassButton updateCurrentClass(LocalDateTime nowDate) {
        var nowTime = nowDate.toLocalTime();
        var today = days[nowDate.getDayOfWeek().ordinal()];
        var currentClass = classButtons.stream()
                                       .filter(k -> k.day.equals(today) && !k.unImportant && (nowTime.isBefore(k.startTime) || nowTime.equals(k.startTime)))
                                       .findFirst()
                                       .orElse(null);

        if(currentClass != null) {
            currentClass.button.setBackground(Settings.currentClassColor);
        }

        if(currentClass != lastActiveClass && lastActiveClass != null) {
            lastActiveClass.button.setBackground(Settings.pastClassColor);
        }

        lastActiveClass = currentClass;
        return currentClass;
    }
}