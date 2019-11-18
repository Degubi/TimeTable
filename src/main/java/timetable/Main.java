package timetable;

import static java.nio.file.StandardOpenOption.*;

import java.awt.*;
import java.awt.Color;
import java.awt.TrayIcon.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;
import javax.imageio.*;
import javax.json.*;
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

    public static final String[] days = {"H�tf�", "Kedd", "Szerda", "Cs�t�rt�k", "P�ntek", "Szombat", "Vas�rnap"};
    public static final JLabel dateLabel = new JLabel("\0");
    public static final JPanel classesPanel = new JPanel(null);
    public static final TrayIcon tray = new TrayIcon(Components.trayIcon.getScaledInstance(16, 16, Image.SCALE_SMOOTH), "�rarend");
    
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
        
        var screenshotItem = Components.newMenuItem("K�p", "screencap.png", Main::exportToImage);
        var frame = new JFrame("�rarend");
        
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
        
        var sleepMode = new JCheckBoxMenuItem("Alv� M�d", Components.getIcon("sleep.png", 24), false);
        var popMenu = new JPopupMenu();

        popMenu.setPreferredSize(new Dimension(170, 200));
        popMenu.add(Components.newMenuItem("Megnyit�s", "open.png", SystemTrayListener::openFromTray));
        popMenu.addSeparator();
        popMenu.add(sleepMode);
        popMenu.add(Components.newSideMenu("Import�l�s", "import.png", Components.newMenuItem("Json", "json.png", Main::importFromJson), Components.newMenuItem("Excel", "excel.png", Main::importFromExcel)));
        popMenu.add(Components.newSideMenu("Export�l�s", "export.png", Components.newMenuItem("Json", "json.png", Main::exportToJson), screenshotItem));
        popMenu.add(Components.newMenuItem("Be�ll�t�sok", "settings.png", PopupGuis::showSettingsGui));
        popMenu.addSeparator();
        popMenu.add(Components.newMenuItem("Bez�r�s", "exit.png", e -> System.exit(0)));
        SwingUtilities.updateComponentTreeUI(popMenu);
        
        tray.addMouseListener(new SystemTrayListener(popMenu));
        SystemTray.getSystemTray().add(tray);
        Runtime.getRuntime().addShutdownHook(new Thread(Settings::saveSettings));
        Thread.currentThread().setName("Time Label Updater");
        
        var timer = Settings.updateInterval - 100;
        var displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");
        var todayNames = HttpClient.newHttpClient()
                                   .send(newRequest("https://api.abalin.net/get/today?country=hu"), ofJsonObject())
                                   .body()
                                   .getJsonObject("data")
                                   .getString("name_hu");
        while(true) {
            var nowDate = LocalDateTime.now();
            
            if(frame.isVisible()) {
                dateLabel.setText(nowDate.format(displayTimeFormat) + ": " + todayNames);
            }

            if(++timer == Settings.updateInterval) {
                if(!sleepMode.isSelected() && !frame.isVisible()) {
                    var nowTime = nowDate.toLocalTime();
                    
                    if(Settings.enablePopups && currentClassButton != null && nowTime.isBefore(currentClassButton.startTime)) {
                        var timeBetween = Duration.between(nowTime, currentClassButton.startTime);

                        if(timeBetween.toMinutes() < Settings.timeBeforeNotification) {
                            tray.displayMessage("�rarend", "Figyelem! K�vetkez� �ra: " + timeBetween.toHoursPart() + " �ra " +  timeBetween.toMinutesPart() + " perc m�lva!\n�ra: " + currentClassButton.className + ' ' + currentClassButton.startTime + '-' + currentClassButton.endTime, MessageType.NONE);
                        }
                    }
                    Components.handleNightMode(dateLabel, nowTime);
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
    
    private static HttpRequest newRequest(String url) {
        return HttpRequest.newBuilder(URI.create(url)).build();
    }
    
    private static BodyHandler<JsonObject> ofJsonObject(){
        return info -> BodySubscribers.mapping(BodySubscribers.ofString(StandardCharsets.ISO_8859_1), data -> Settings.json.fromJson(data, JsonObject.class));
    }
    
    public static void updateClassesGui() {
        classButtons.forEach(k -> classesPanel.remove(k.button));
        classButtons.clear();
        currentClassButton = null;
        
        var nowDate = LocalDateTime.now();
        var today = days[nowDate.getDayOfWeek().ordinal()];
        var nowTime = nowDate.toLocalTime();
        
        Components.handleNightMode(mainPanel, nowTime);
        Components.handleNightMode(classesPanel, nowTime);
        Components.handleNightMode(dateLabel, nowTime);
        
        Settings.classes
                .forEach((day, classesPerDay) -> {
                    var yPosition = new int[] {-40};
                    var xPosition = 20 + Settings.indexOf(day, days) * 200;

                    classesPerDay.stream()
                                 .sorted(ClassButton.timeBasedOrder)
                                 .forEach(clazz -> {
                                     clazz.button.setBounds(xPosition, yPosition[0] += 95, 175, 85);
                                  
                                     var isToday = day.equalsIgnoreCase(today);
                                     var isBefore = isToday && nowTime.isBefore(clazz.startTime);
                                     var isAfter = isToday && (nowTime.isAfter(clazz.startTime) || nowTime.equals(clazz.startTime));
                                     var isNext = currentClassButton == null && !clazz.unImportant && isBefore || (isToday && nowTime.equals(clazz.startTime));
                                    
                                     if(isNext) {
                                         currentClassButton = clazz;
                                        
                                         var between = Duration.between(nowTime, clazz.startTime);
                                         Main.tray.setToolTip("K�vetkez� �ra " + between.toHoursPart() + " �ra " + between.toMinutesPart() + " perc m�lva: " + clazz.className + ' ' + clazz.classType + "\nId�pont: " + clazz.startTime + '-' + clazz.endTime + "\nTerem: " + clazz.room);
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