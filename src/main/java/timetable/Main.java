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
import java.util.List;
import java.util.Map.*;
import java.util.function.*;
import java.util.stream.*;
import javax.imageio.*;
import javax.json.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.nimbus.*;
import org.apache.poi.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
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
        dateLabel.setFont(Components.bigFont);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateLabel.setBorder(new EmptyBorder(15, 0, 20, 0));
        mainPanel.add(dateLabel, BorderLayout.PAGE_START);
        mainPanel.add(classesPanel, BorderLayout.CENTER);
        
        var screenshotItem = Components.newMenuItem("Kép", "screencap.png", Main::exportToImage);
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

        popMenu.setPreferredSize(new Dimension(170, 200));
        popMenu.add(Components.newMenuItem("Megnyitás", "open.png", SystemTrayListener::openFromTray));
        popMenu.addSeparator();
        popMenu.add(sleepMode);
        popMenu.add(Components.newSideMenu("Importálás", "import.png", Components.newMenuItem("Json", "json.png", Main::importFromJson), Components.newMenuItem("Excel", "excel.png", Main::importFromExcel)));
        popMenu.add(Components.newSideMenu("Exportálás", "export.png", Components.newMenuItem("Json", "json.png", Main::exportToJson), Components.newMenuItem("Excel", "excel.png", Main::exportToExcel), screenshotItem));
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
                            tray.displayMessage("Órarend", "Figyelem! Következõ óra: " + timeBetween.toHoursPart() + " óra " +  timeBetween.toMinutesPart() + " perc múlva!\nÓra: " + currentClassButton.className + ' ' + currentClassButton.startTime + '-' + currentClassButton.endTime, MessageType.NONE);
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
        topAdd.setFont(Components.bigFont);
        topAdd.setBounds(20 + (dayIndex * 200), 0, 175, 40);
        return topAdd;
    }
    
    private static void exportToImage(@SuppressWarnings("unused") ActionEvent event) {
        Consumer<JDialog> exportFunction = dialog -> {
            var window = classesPanel.getTopLevelAncestor().getLocationOnScreen();
            var fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) + ".png";
            var exportFile = new File(fileName);
            
            try {
                ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 20, window.y + 100, 990, 650)), "PNG", exportFile);
                Runtime.getRuntime().exec("explorer.exe /select," + exportFile);
                dialog.setVisible(false);
            } catch (HeadlessException | AWTException | IOException e1) {}
        };
        
        showTransferDialog("Exportálás folyamatban...", exportFunction);
    }
    
    private static void exportToJson(@SuppressWarnings("unused") ActionEvent event) {
        Consumer<JDialog> exportFunction = dialog -> {
            var exportFile = Path.of(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) + ".json");
            var classesArray = Settings.createClassesArray();
            var classesObject = Json.createObjectBuilder().add("classes", classesArray).build();
            
            try {
                Files.writeString(exportFile, Settings.json.toJson(classesObject), WRITE, CREATE);
                Runtime.getRuntime().exec("explorer.exe /select," + exportFile);
                dialog.setVisible(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        
        showTransferDialog("Exportálás folyamatban...", exportFunction);
    }
    
    private static void exportToExcel(@SuppressWarnings("unused") ActionEvent event) {
        Consumer<JDialog> exportFunction = dialog -> {
            var exportFile = Path.of(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) + ".xlsx");
            
            try(var workbook = new XSSFWorkbook();
                var output = Files.newOutputStream(exportFile, WRITE, CREATE)){
                
                var sheet = workbook.createSheet("Órák");
                var headerRow = sheet.createRow(0);
                var headerText = new String[] {"Kezdés", "Befejezés", "Összefoglalás", "Helyszín"};
                
                IntStream.range(0, 4).forEach(i -> headerRow.createCell(i).setCellValue(headerText[i]));
                
                var rowIndex = new int[] {1};
                var today = LocalDateTime.now();
                var todayDayOffset = today.getDayOfWeek().ordinal();
                
                Settings.classes.entrySet().stream()
                        .map(Entry::getValue)
                        .flatMap(List::stream)
                        .forEach(clazz -> {
                            var row = sheet.createRow(rowIndex[0]++);
                            var indexOfDay = Settings.indexOf(clazz.day, days);
                            var dateOffset = indexOfDay - todayDayOffset;
                            var classDayFormatted = LocalDate.now()
                                                             .plusDays(dateOffset)
                                                             .toString()
                                                             .replace('-', '.');
                            
                            var classType = clazz.classType.equals("Szabvál") ? " (_SZV_" 
                                                                              : (" (_" + clazz.classType.charAt(0));
                            
                            row.createCell(0).setCellValue(classDayFormatted + ". " + clazz.startTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
                            row.createCell(1).setCellValue(classDayFormatted + ". " + clazz.endTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
                            row.createCell(2).setCellValue(clazz.className + classType + ")");
                            row.createCell(3).setCellValue(clazz.room);
                        });
                
                IntStream.range(0, 4).forEach(sheet::autoSizeColumn);
                
                workbook.write(output);
                Runtime.getRuntime().exec("explorer.exe /select," + exportFile);
                dialog.setVisible(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        
        showTransferDialog("Exportálás folyamatban...", exportFunction);
    }
    
    private static void importFromJson(@SuppressWarnings("unused") ActionEvent event) {
        var chooser = new JFileChooser(System.getProperty("user.home") + "/Desktop");
        chooser.setDialogTitle("Órarend Importálás Választó");
        chooser.setFileFilter(new FileNameExtensionFilter("Json Files", "json"));

        if(chooser.showOpenDialog(classesPanel) == JFileChooser.APPROVE_OPTION) {
            Consumer<JDialog> importFunction = dialog -> {
                try {
                    var file = Files.readString(chooser.getSelectedFile().toPath());
                    var parsedJson = Settings.json.fromJson(file, JsonObject.class);
                    
                    Settings.updateClassesData(Settings.getArraySetting("classes", parsedJson).stream()
                                                       .map(JsonValue::asJsonObject)
                                                       .map(ClassButton::new)
                                                       .collect(Collectors.groupingBy(k -> k.day)));
                    updateClassesGui();
                    dialog.setVisible(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            
            showTransferDialog("Importálás folyamatban...", importFunction);
        }
    }
    
    private static void importFromExcel(@SuppressWarnings("unused") ActionEvent event) {
        var chooser = new JFileChooser(System.getProperty("user.home") + "/Desktop");
        chooser.setDialogTitle("Órarend Importálás Választó");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xls", "xlsx"));

        if(chooser.showOpenDialog(classesPanel) == JFileChooser.APPROVE_OPTION) {
            Consumer<JDialog> importFunction = dialog -> {
                try(var book = WorkbookFactory.create(chooser.getSelectedFile().getAbsoluteFile())){
                    var classesSheet = book.getSheetAt(0);
                    var columnCount = classesSheet.getRow(0).getPhysicalNumberOfCells();
                    var format = DateTimeFormatter.ofPattern("uuuu.MM.dd. [HH:][H:]mm:ss");
                    
                    Function<Row, ClassButton> creatorFunction = columnCount == 5 ? row -> ClassButton.fromTimetableExcelExport(row, format)
                                                                                  : ClassButton::fromCourseExcelExport;
                    
                    Settings.updateClassesData(StreamSupport.stream(classesSheet.spliterator(), false)
                                                            .skip(1)
                                                            .map(creatorFunction)
                                                            .collect(Collectors.groupingBy(k -> k.day)));
                    updateClassesGui();
                    dialog.setVisible(false);
                }catch (FileNotFoundException e) {
                    dialog.setVisible(false);
                    JOptionPane.showMessageDialog(mainPanel, "Az excel fájl használatban van!", "Hiba", JOptionPane.ERROR_MESSAGE);
                } catch (EncryptedDocumentException | IOException e) {
                    e.printStackTrace();
                }
            };
            
            showTransferDialog("Importálás folyamatban...", importFunction);
        }
    }
    
    
    private static void showTransferDialog(String text, Consumer<JDialog> fun) {
        var jop = new JOptionPane(text, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[0]);
        var dialog = jop.createDialog(mainPanel, "Órarend");
        
        new Thread(() -> fun.accept(dialog)).start();
        dialog.setVisible(true);
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
                    var isToday = day.equals(today);
                    
                    classesPerDay.stream()
                                 .sorted(ClassButton.timeBasedOrder)
                                 .forEach(clazz -> {
                                     clazz.button.setBounds(xPosition, yPosition[0] += 95, 175, 85);
                                  
                                     var isBefore = isToday && nowTime.isBefore(clazz.startTime);
                                     var isAfter = isToday && (nowTime.isAfter(clazz.startTime) || nowTime.equals(clazz.startTime));
                                     var isNext = currentClassButton == null && !clazz.unImportant && isBefore || (isToday && nowTime.equals(clazz.startTime));
                                    
                                     if(isNext) {
                                         currentClassButton = clazz;
                                        
                                         var between = Duration.between(nowTime, clazz.startTime);
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