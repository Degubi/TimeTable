package timetable;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.function.*;
import javax.swing.*;

public final class Components {
    private Components() {}
    
    public static JMenuItem newMenuItem(String text, String iconPath, ActionListener listener) {
        var item = new JMenuItem(text, iconPath == null ? null : getIcon(iconPath, 24));
        item.addActionListener(listener);
        return item;
    }
    
    public static JComboBox<String> newComboBox(String selectedItem, int y, String... data){
        var endTimeBox = new JComboBox<>(data);
        endTimeBox.setBounds(100, y, 75, 30);
        endTimeBox.setEditable(true);
        endTimeBox.setSelectedItem(selectedItem);
        return endTimeBox;
   }

    public static JButton newColorButton(int x, int y, Consumer<JButton> listener, Color startColor) {
        var butt = new JButton();
        butt.setBackground(startColor);
        butt.setBounds(x, y, 48, 48);
        butt.setFocusable(false);
        butt.setBorder(null);
        butt.addActionListener(e -> listener.accept(butt));
        return butt;
    }

    public static JButton newButton(String text, Color foreground, Color background, Dimension preferredSize) {
        var butt = new JButton(text);
        butt.setFocusPainted(false);
        butt.setForeground(foreground);
        butt.setBackground(background);
        butt.setPreferredSize(preferredSize);
        return butt;
    }
    
    
    public static ImageIcon getIcon(String path, int scale) {
        var image = Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/assets/" + path));
        if(scale == 0 || image.getWidth(null) == scale) {
            return new ImageIcon(image);
        }
        return new ImageIcon(image.getScaledInstance(scale, scale, Image.SCALE_SMOOTH));
    }
    
    public static void handleNightMode(Container container, LocalTime time) {
        var isDarkMode = time.isAfter(Settings.dayTimeEnd) || time.isBefore(Settings.dayTimeStart);
    
        if(container instanceof JLabel || container instanceof JCheckBox) {
            container.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        }else{
            container.setBackground(isDarkMode ? Settings.nightTimeColor : Settings.dayTimeColor);
        }
    }
}