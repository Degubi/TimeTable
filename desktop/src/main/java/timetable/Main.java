package timetable;

import jakarta.json.*;
import java.awt.*;
import java.awt.Color;
import java.awt.TrayIcon.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.nio.charset.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.imageio.*;
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
    private static final String BACKEND_URL = "https://timetable-backend.herokuapp.com/timetable";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static ClassButton currentClassButton;

    public static final String[] days = {"Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat", "Vasárnap"};
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
        popMenu.add(Components.newSideMenu("Importálás", "import.png", Components.newMenuItem("Neptun Órarend Excel", "excel.png", Main::importFromExcel), Components.newMenuItem("Felhő", "db.png", Main::importFromCloud)));
        popMenu.add(Components.newSideMenu("Mentés", "export.png", screenshotItem, Components.newMenuItem("Felhő", "db.png", Main::exportToCloud)));
        popMenu.add(Components.newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
        popMenu.addSeparator();
        popMenu.add(Components.newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));
        SwingUtilities.updateComponentTreeUI(popMenu);

        tray.addMouseListener(new SystemTrayListener(popMenu));
        SystemTray.getSystemTray().add(tray);
        Runtime.getRuntime().addShutdownHook(new Thread(Settings::saveSettings));
        Thread.currentThread().setName("Time Label Updater");

        var timer = Settings.updateIntervalSeconds - 100;
        var displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");
        var todayNames = sendRequest(HttpRequest.newBuilder(URI.create("https://api.abalin.net/today?country=hu")), ofObject())
                               .body()
                               .getJsonObject("data")
                               .getJsonObject("namedays")
                               .getString("hu");
        while(true) {
            var nowDate = LocalDateTime.now();

            if(frame.isVisible()) {
                dateLabel.setText(nowDate.format(displayTimeFormat) + ": " + todayNames);
            }

            if(++timer >= Settings.updateIntervalSeconds) {
                if(!sleepMode.isSelected() && !frame.isVisible()) {
                    var nowTime = nowDate.toLocalTime();

                    if(Settings.enablePopups && currentClassButton != null && nowTime.isBefore(currentClassButton.startTime)) {
                        var timeBetween = Duration.between(nowTime, currentClassButton.startTime);

                        if(timeBetween.toMinutes() < Settings.minutesBeforeFirstNotification) {
                            tray.displayMessage("Órarend", "Figyelem! Következő óra: " + timeBetween.toHoursPart() + " óra " +  timeBetween.toMinutesPart() + " perc múlva!\nÓra: " + currentClassButton.name + ' ' + currentClassButton.startTime + '-' + currentClassButton.endTime, MessageType.NONE);
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
                ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 20, window.y + 80, 990, 650)), "PNG", exportFile);
                Runtime.getRuntime().exec("explorer.exe /select," + exportFile);
                dialog.setVisible(false);
            } catch (HeadlessException | AWTException | IOException e1) {}
        };

        showTransferDialog("Mentés folyamatban...", exportFunction);
    }

    private static void exportToCloud(@SuppressWarnings("unused") ActionEvent event) {
        var userPwInput = JOptionPane.showInputDialog(mainPanel, "Írd be az órarend módosítás jelszavad!");

        if(userPwInput != null) {
            Consumer<JDialog> exportFunction = dialog -> {
                var classesArray = Settings.createClassesArray();
                var objectToSend = Json.createObjectBuilder()
                                       .add("classes", classesArray)
                                       .add("password", userPwInput)
                                       .build();

                var request = HttpRequest.newBuilder(URI.create(BACKEND_URL + "?id=" + Settings.cloudID))
                                         .POST(BodyPublishers.ofString(Settings.json.toJson(objectToSend)));

                var response = sendRequest(request, BodyHandlers.ofString());
                var maybeCreatedBody = response.body();

                if(!maybeCreatedBody.isBlank()) {
                    dialog.setVisible(false);
                    Settings.cloudID = maybeCreatedBody;
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(maybeCreatedBody), null);
                    JOptionPane.showMessageDialog(mainPanel, "Új órarend létrehozva, azonosító: " + maybeCreatedBody + " (másolva vágólapra)");
                }else {
                    var updatedStatusCode = response.statusCode();

                    if(updatedStatusCode == 200) {
                        dialog.setVisible(false);
                        JOptionPane.showMessageDialog(mainPanel, "Sikeres mentés!");
                    }else if(updatedStatusCode == 401) {
                        dialog.setVisible(false);
                        JOptionPane.showMessageDialog(mainPanel, "Sikertelen mentés! Hibás jelszó!");
                    }else{
                        dialog.setVisible(false);
                        Settings.cloudID = "null";
                        JOptionPane.showMessageDialog(mainPanel, "Sikertelen mentés! Nem található ilyen azonosítójú órarend...\nAz eddigi felhő azonosító törlésre került!");
                    }
                }
            };

            showTransferDialog("Mentés folyamatban...", exportFunction);
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
                    var format = DateTimeFormatter.ofPattern("uuuu.MM.dd. [HH:][H:]mm:ss");

                    Settings.updateClassesData(StreamSupport.stream(classesSheet.spliterator(), false)
                                                            .skip(1)  // Header
                                                            .map(row -> ClassButton.fromTimetableExcel(row, format))
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

    private static void importFromCloud(@SuppressWarnings("unused") ActionEvent event) {
        var userIDInput = JOptionPane.showInputDialog(mainPanel, "Írd be az órarend azonosítót!");

        if(userIDInput != null && !userIDInput.isBlank()) {
            Consumer<JDialog> importFunction = dialog -> {
                JsonObject response = sendRequest(HttpRequest.newBuilder(URI.create(BACKEND_URL + "?id=" + userIDInput)), ofObject()).body();

                if(response != null) {
                    Settings.updateClassesData(Settings.getArraySetting("classes", response).stream()
                            .map(JsonValue::asJsonObject)
                            .map(ClassButton::fromJson)
                            .collect(Collectors.groupingBy(k -> k.day)));

                    dialog.setVisible(false);
                    Settings.cloudID = userIDInput;
                    updateClassesGui();
                }else{
                    dialog.setVisible(false);
                    JOptionPane.showMessageDialog(mainPanel, "Nem található ilyen azonsítójú órarend! :(", "Hiba", JOptionPane.ERROR_MESSAGE);
                }
            };

            showTransferDialog("Importálás folyamatban...", importFunction);
        }
    }

    private static<T> HttpResponse<T> sendRequest(HttpRequest.Builder request, BodyHandler<T> bodyHandler) {
        try {
            return client.send(request.header("Content-Type", "application/json").build(), bodyHandler);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException("Huh?");
        }
    }

    private static BodyHandler<JsonObject> ofObject() {
        return info -> BodySubscribers.mapping(BodySubscribers.ofString(StandardCharsets.UTF_8),
               data -> info.statusCode() != 200 ? null : Settings.json.fromJson(data, JsonObject.class));
    }


    private static void showTransferDialog(String text, Consumer<JDialog> fun) {
        var jop = new JOptionPane(text, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[0]);
        var dialog = jop.createDialog(mainPanel, "Órarend");

        new Thread(() -> fun.accept(dialog)).start();
        dialog.setVisible(true);
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
                                 .forEach(clazz -> positionAndAddButtonToPanel(nowTime, yPosition, xPosition, isToday, clazz));
                });

        classesPanel.repaint();
    }

    private static void positionAndAddButtonToPanel(LocalTime nowTime, int[] yPosition, int xPosition, boolean isToday, ClassButton clazz) {
        clazz.button.setBounds(xPosition, yPosition[0] += 95, 175, 85);

        var isBefore = isToday && nowTime.isBefore(clazz.startTime);
        var isAfter = isToday && (nowTime.isAfter(clazz.startTime) || nowTime.equals(clazz.startTime));
        var isNext = currentClassButton == null && !clazz.unImportant && isBefore || (isToday && nowTime.equals(clazz.startTime));

        if(isNext) {
            currentClassButton = clazz;

            var between = Duration.between(nowTime, clazz.startTime);
            Main.tray.setToolTip("Következő óra " + between.toHoursPart() + " óra " + between.toMinutesPart() + " perc múlva: " + clazz.name + ' ' + clazz.type + "\nIdőpont: " + clazz.startTime + '-' + clazz.endTime + "\nTerem: " + clazz.room);
        }
        clazz.button.setBackground(clazz.unImportant ? Settings.unimportantClassColor : isNext ? Settings.currentClassColor : isBefore ? Settings.upcomingClassColor : isAfter ? Settings.pastClassColor : Settings.otherDayClassColor);
        clazz.button.setForeground(clazz.unImportant ? Color.LIGHT_GRAY : Color.BLACK);

        classesPanel.add(clazz.button);
        classButtons.add(clazz);
    }
}