package timetable;

import static java.nio.file.StandardOpenOption.*;

import java.awt.*;
import java.awt.Color;
import java.awt.TrayIcon.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;
import javax.imageio.*;
import javax.json.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.nimbus.*;
import org.apache.poi.*;
import org.apache.poi.ss.usermodel.*;
import timetable.listeners.*;

public final class Main {
    private static final ArrayList<ClassButton> classButtons = new ArrayList<>();
    private static final JPanel mainPanel = new JPanel(new BorderLayout());
    public static ClassButton currentClassButton;

    public static final String[] days = {"Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat", "Vasárnap"};
    public static final JLabel dateLabel = new JLabel("\0");
    public static final JPanel classesPanel = new JPanel(null);
    public static final TrayIcon tray = new TrayIcon(Components.trayIcon.getScaledInstance(16, 16, Image.SCALE_SMOOTH), "Órarend");
    
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        IntStream.range(0, 5).mapToObj(Main::newDayButton).forEach(classesPanel::add);
        updateClassesGui();
        
        dateLabel.setBounds(350, 5, 300, 40);
        dateLabel.setFont(Components.tableHeaderFont);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateLabel.setBorder(new EmptyBorder(15, 0, 20, 0));
        mainPanel.add(dateLabel, BorderLayout.PAGE_START);
        mainPanel.add(classesPanel, BorderLayout.CENTER);
        
        var screenshotItem = Components.newMenuItem("Kép", "screencap.png", Main::exportToImage);
        var frame = new JFrame("Órarend");
        
        frame.setBounds(0, 0, 1024, 768);
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

        popMenu.setPreferredSize(new Dimension(170, 200));
        popMenu.add(Components.newMenuItem("Megnyitás", "open.png", SystemTrayListener::openFromTray));
        popMenu.addSeparator();
        popMenu.add(sleepMode);
        popMenu.add(Components.newSideMenu("Importálás", "import.png", Components.newMenuItem("Json", "json.png", Main::importFromJson), Components.newMenuItem("Excel", "excel.png", Main::importFromExcel)));
        popMenu.add(Components.newSideMenu("Exportálás", "export.png", Components.newMenuItem("Json", "json.png", Main::exportToJson), screenshotItem));
        popMenu.add(Components.newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
        popMenu.addSeparator();
        popMenu.add(Components.newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));
        SwingUtilities.updateComponentTreeUI(popMenu);
        
        tray.addMouseListener(new SystemTrayListener(popMenu));
        SystemTray.getSystemTray().add(tray);
        Runtime.getRuntime().addShutdownHook(new Thread(Settings::saveSettings));
        Thread.currentThread().setName("Time Label Updater");
        
        var timer = Settings.updateInterval - 100;
        var displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");
        
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
    
    private static JButton newDayButton(int dayIndex) {
        var currentDay = days[dayIndex];
        var topAdd = new JButton(currentDay);
        
        topAdd.setFocusable(false);
        topAdd.addMouseListener(new CreateClassListener(currentDay));
        topAdd.setBackground(Color.GRAY);
        topAdd.setForeground(Color.BLACK);
        topAdd.setFont(Components.tableHeaderFont);
        topAdd.setBounds(20 + (dayIndex * 200), 0, 175, 40);
        return topAdd;
    }
    
    private static void exportToImage(@SuppressWarnings("unused") ActionEvent event) {
        var window = classesPanel.getTopLevelAncestor().getLocationOnScreen();
        var fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) + ".png";
        var exportFile = new File(fileName);
        
        try {
            ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 20, window.y + 100, 990, 650)), "PNG", exportFile);
            Runtime.getRuntime().exec("explorer.exe /select," + exportFile);
        } catch (HeadlessException | AWTException | IOException e1) {}
    }
    
    private static void exportToJson(@SuppressWarnings("unused") ActionEvent event) {
        var exportFile = Path.of(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) + ".json");
        var classesArray = Settings.createClassesArray();
        var classesObject = Json.createObjectBuilder().add("classes", classesArray).build();
        
        try {
            Files.writeString(exportFile, Settings.json.toJson(classesObject), WRITE, CREATE);
            Runtime.getRuntime().exec("explorer.exe /select," + exportFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void importFromJson(@SuppressWarnings("unused") ActionEvent event) {
        var chooser = new JFileChooser("./");
        chooser.setFileFilter(new FileNameExtensionFilter("Json Files", "json"));

        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                var file = Files.readString(chooser.getSelectedFile().toPath());
                var parsedJson = Settings.json.fromJson(file, JsonObject.class);
                
                Settings.updateClassesData(Settings.getArraySetting("classes", parsedJson).stream()
                                                   .map(JsonValue::asJsonObject)
                                                   .map(ClassButton::new)
                                                   .collect(Collectors.groupingBy(k -> k.day)));
                updateClassesGui();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void importFromExcel(@SuppressWarnings("unused") ActionEvent event) {
        var chooser = new JFileChooser("./");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xls", "xlsx"));

        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try(var book = WorkbookFactory.create(chooser.getSelectedFile().getAbsoluteFile())){
                var classesSheet = book.getSheetAt(0);
                var format = DateTimeFormatter.ofPattern("uuuu.MM.dd. [HH:][H:]mm:ss");
                
                Settings.updateClassesData(StreamSupport.stream(classesSheet.spliterator(), false)
                                                        .skip(1)
                                                        .map(row -> new ClassButton(row, format))
                                                        .collect(Collectors.groupingBy(k -> k.day)));
                updateClassesGui();
            } catch (EncryptedDocumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void updateClassesGui() {
        classButtons.forEach(k -> classesPanel.remove(k.button));
        classButtons.clear();
        currentClassButton = null;
        
        var today = days[LocalDateTime.now().getDayOfWeek().ordinal()];
        var now = LocalTime.now();
        
        Components.handleNightMode(mainPanel, now);
        Components.handleNightMode(classesPanel, now);
        Components.handleNightMode(dateLabel, now);
        
        Settings.classes
                .forEach((day, classesPerDay) -> {
                    var yPosition = new int[] {-40};
                    var xPosition = 20 + Settings.indexOf(day, days) * 200;

                    classesPerDay.stream()
                                 .sorted(ClassButton.timeBasedOrder)
                                 .forEach(clazz -> {
                                     clazz.button.setBounds(xPosition, yPosition[0] += 95, 175, 85);
                                  
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
                                  
                                     classesPanel.add(clazz.button);
                                     classButtons.add(clazz);
                                 });
                });
        
        classesPanel.repaint();
    }
}