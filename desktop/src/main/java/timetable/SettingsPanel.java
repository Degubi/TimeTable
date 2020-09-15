package timetable;

import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.qrcode.*;
import java.awt.*;
import java.awt.image.*;
import java.time.*;
import javax.swing.*;

public final class SettingsPanel extends JPanel {

    private final Image imageToDraw = createQRCode();

    public SettingsPanel() {
        super(null);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.drawImage(imageToDraw, 350, 672, null);
    }

    private static Image createQRCode() {
        if(Settings.cloudID.equals("null")) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
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

            return outputImage;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}