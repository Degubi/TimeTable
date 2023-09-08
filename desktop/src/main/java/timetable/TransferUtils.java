package timetable;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import org.apache.poi.*;
import org.apache.poi.ss.usermodel.*;

public final class TransferUtils {
    private static final HttpClient client = HttpClient.newHttpClient();

	public static void exportToImage(@SuppressWarnings("unused") ActionEvent event) {
        Consumer<JDialog> exportFunction = dialog -> {
            var window = Main.classesPanel.getTopLevelAncestor().getLocationOnScreen();
            var fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) + ".png";
            var exportFile = new File(fileName);

            try {
                ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 20, window.y + 80, 990, 650)), "PNG", exportFile);
                Runtime.getRuntime().exec(new String[] { "explorer.exe", "/select," + exportFile });
                dialog.setVisible(false);
            } catch (HeadlessException | AWTException | IOException e1) {}
        };

        showTransferDialog("Mentés folyamatban...", exportFunction);
    }

	public static void exportToCloud(@SuppressWarnings("unused") ActionEvent event) {
		var mainPanel = Main.classesPanel.getTopLevelAncestor();
        var userPwInput = JOptionPane.showInputDialog(mainPanel, "Írd be az órarend módosítás jelszavad!");

        if(userPwInput != null) {
            Consumer<JDialog> exportFunction = dialog -> {
                var objectToSend = Map.of("classes", Settings.classes, "password", userPwInput);

                var request = HttpRequest.newBuilder(URI.create(Main.BACKEND_URL + (!Settings.cloudID.equals(Settings.NULL_CLOUD_ID) ? ("?id=" + Settings.cloudID) : "")))
                                         .POST(Settings.publisherOf(objectToSend));

                var response = sendRequest(request, BodyHandlers.ofString());
                var responseStatusCode = response.statusCode();

                dialog.setVisible(false);

                if(responseStatusCode == 401) {
                    JOptionPane.showMessageDialog(mainPanel, "Sikertelen mentés! Hibás jelszó!");
                }else if(responseStatusCode == 400) {
                    Settings.cloudID = Settings.NULL_CLOUD_ID;
                    JOptionPane.showMessageDialog(mainPanel, "Sikertelen mentés! Nem található ilyen azonosítójú órarend...\nAz eddigi felhő azonosító törlésre került!");
                }else if(responseStatusCode == 200) {
                    var optionalReceivedCloudID = response.body();

                    if(optionalReceivedCloudID != null && !optionalReceivedCloudID.isBlank()) {
                        Settings.cloudID = optionalReceivedCloudID;
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(optionalReceivedCloudID), null);
                        JOptionPane.showMessageDialog(mainPanel, "Új órarend létrehozva, azonosító: " + optionalReceivedCloudID + " (másolva vágólapra)");
                    }else{
                        JOptionPane.showMessageDialog(mainPanel, "Sikeres mentés!");
                    }
                }
            };

            showTransferDialog("Mentés folyamatban...", exportFunction);
        }
    }

    public static void importFromExcel(@SuppressWarnings("unused") ActionEvent event) {
        var chooser = new JFileChooser(System.getProperty("user.home") + "/Desktop");
        chooser.setDialogTitle("Órarend Importálás Választó");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xls", "xlsx"));

        if(chooser.showOpenDialog(Main.classesPanel) == JFileChooser.APPROVE_OPTION) {
            Consumer<JDialog> importFunction = dialog -> {
                try(var book = WorkbookFactory.create(chooser.getSelectedFile().getAbsoluteFile())){
                    var classesSheet = book.getSheetAt(0);
                    var format = DateTimeFormatter.ofPattern("M[M]/d[d]/uuuu h[h]:mm:ss a", Locale.ENGLISH);

                    Settings.classes = StreamSupport.stream(classesSheet.spliterator(), false)
                                                    .skip(1)  // Header
                                                    .map(row -> createClassButtonFromExcelRow(row, format))
                                                    .collect(Collectors.toCollection(ArrayList::new));
                    Main.updateClassesGui();
                    dialog.setVisible(false);
                }catch (FileNotFoundException e) {
                    dialog.setVisible(false);
                    JOptionPane.showMessageDialog(Main.classesPanel.getTopLevelAncestor(), "Az excel fájl használatban van!", "Hiba", JOptionPane.ERROR_MESSAGE);
                } catch (EncryptedDocumentException | IOException e) {
                    e.printStackTrace();
                }
            };

            showTransferDialog("Importálás folyamatban...", importFunction);
        }
    }

    public static void importFromCloud(@SuppressWarnings("unused") ActionEvent event) {
    	var mainPanel = Main.classesPanel.getTopLevelAncestor();
        var userIDInput = JOptionPane.showInputDialog(mainPanel, "Írd be az órarend azonosítót!");

        if(userIDInput != null && !userIDInput.isBlank()) {
            Consumer<JDialog> importFunction = dialog -> {
                var responseClasses = sendRequest(HttpRequest.newBuilder(URI.create(Main.BACKEND_URL + "?id=" + userIDInput)), Settings.of(Settings.CLASSES_TYPEREF)).body();

                if(responseClasses != null) {
                    Settings.classes = responseClasses;

                    dialog.setVisible(false);
                    Settings.cloudID = userIDInput;
                    Main.updateClassesGui();
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

    private static void showTransferDialog(String text, Consumer<JDialog> fun) {
        var jop = new JOptionPane(text, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[0]);
        var dialog = jop.createDialog(Main.classesPanel.getTopLevelAncestor(), "Órarend");

        new Thread(() -> fun.accept(dialog)).start();
        dialog.setVisible(true);
    }

    private static ClassButton createClassButtonFromExcelRow(Row classRow, DateTimeFormatter format) {
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

	private TransferUtils() {}
}