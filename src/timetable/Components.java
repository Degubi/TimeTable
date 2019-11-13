package timetable;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import timetable.listeners.*;

public final class Components {
    public static final Font tableHeaderFont = new Font("SansSerif", Font.PLAIN, 20);
    public static final Image trayIcon = getIcon("tray.png", 0).getImage();

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

    public static JButton newClassToolButton(int yPos, ImageIcon icon, ActionListener listener) {
        var butt = new JButton(icon);
        butt.setBackground(Color.LIGHT_GRAY);
        butt.setBounds(0, yPos, 32, 32);
        butt.addActionListener(listener);
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
    
    public static void addSettingButton(JComponent component, int y, String labelText, JPanel mainPanel, LocalTime time) {
        component.setLocation(400, y);
        mainPanel.add(component);
        
        var label = new JLabel(labelText);
        label.setFont(Components.tableHeaderFont);
        Components.handleNightMode(label, time);
        label.setBounds(100, y + (component instanceof JCheckBox ? -5 : component instanceof JButton ? 5 : 0), 400, 30);
        mainPanel.add(label);
    }
    
    public static JTable createClassEditorTable(ClassButton dataButton) {
        var editorTable = new JTable(new ClassEditorTableModel());
        editorTable.setBackground(Color.LIGHT_GRAY);
        editorTable.setRowHeight(20);
        editorTable.setBorder(new LineBorder(Color.BLACK, 2, true));
        
        var inputMap = editorTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        var actionMap = editorTable.getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
        actionMap.put("DOWN", new ClassEditorTableKeyListener('D', editorTable));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
        actionMap.put("LEFT", new ClassEditorTableKeyListener('L', editorTable));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
        actionMap.put("RIGHT", new ClassEditorTableKeyListener('R', editorTable));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
        actionMap.put("UP", new ClassEditorTableKeyListener('U', editorTable));
        editorTable.setFont(new Font("Arial", Font.BOLD, 12));
        editorTable.setBounds(20, 20, 340, 122);
        
        editorTable.setValueAt("�ra Neve", 0, 0);
        editorTable.setValueAt(dataButton.className, 0, 1);
        editorTable.setValueAt("Nap", 1, 0);
        editorTable.setValueAt(dataButton.day, 1, 1);
        editorTable.setValueAt("�ra T�pusa", 2, 0);
        editorTable.setValueAt(dataButton.classType, 2, 1);
        editorTable.setValueAt("Kezd�s Id�", 3, 0);
        editorTable.setValueAt(dataButton.startTime.toString(), 3, 1);
        editorTable.setValueAt("V�gz�s Id�", 4, 0);
        editorTable.setValueAt(dataButton.endTime.toString(), 4, 1);
        editorTable.setValueAt("Terem", 5, 0);
        editorTable.setValueAt(dataButton.room, 5, 1);
        
        return editorTable;
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
    
    
    private static final class ClassEditorTableModel extends DefaultTableModel{
        @Override public int getRowCount() { return 6; }
        @Override public int getColumnCount() { return 2; }
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == 1 && rowIndex != 1 && rowIndex != 2; }
    }
}