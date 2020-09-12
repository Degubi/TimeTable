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
    public static final Font bigFont = new Font("SansSerif", Font.PLAIN, 20);
    public static final Font bigBaldFont = new Font("SansSerif", Font.BOLD, 20);
    public static final Font smallFont = new Font("SansSerif", Font.PLAIN, 16);
    public static final Image trayIcon = getIcon("tray.png", 0).getImage();

    private Components() {}

    public static JMenuItem newMenuItem(String text, String iconPath, ActionListener listener) {
        var item = new JMenuItem(text, iconPath == null ? null : getIcon(iconPath, 24));
        item.addActionListener(listener);
        return item;
    }

    public static JMenu newSideMenu(String text, String iconPath, JMenuItem... menuItems) {
        var importMenu = new JMenu(text);
        importMenu.setIcon(Components.getIcon(iconPath, 24));
        importMenu.setHorizontalTextPosition(SwingConstants.RIGHT);
        importMenu.setHorizontalAlignment(SwingConstants.LEFT);
        importMenu.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        importMenu.setMargin(new Insets(2, 25, 2, 0));
        for(var item : menuItems) importMenu.add(item);
        return importMenu;
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
        label.setFont(smallFont);
        Components.handleNightMode(label, time);
        label.setBounds(100, y + (component instanceof JCheckBox ? -5 : component instanceof JButton ? 5 : 0), 400, 30);
        mainPanel.add(label);
    }

    public static void addSettingInfoLabel(int y, String labelText, JPanel mainPanel, LocalTime time) {
        var fake = new JTextField(labelText);
        fake.setFont(smallFont);
        fake.setBounds(100, y, 400, 30);
        fake.setForeground(Color.WHITE);
        fake.setEditable(false);
        fake.setBackground(null);
        fake.setOpaque(false);
        fake.setBorder(null);

        Components.handleNightMode(fake, time);
        mainPanel.add(fake);
    }

    public static void addSettingsSection(String text, int y, LocalTime time, JPanel mainPanel) {
        var label = new JLabel(text);
        label.setBounds(100, y, text.length() * 12, 30);
        label.setFont(bigBaldFont);
        Components.handleNightMode(label, time);
        mainPanel.add(label);

        var separatorBottom = new JSeparator(SwingConstants.HORIZONTAL);
        separatorBottom.setBounds(0, y + 30, 800, 2);
        mainPanel.add(separatorBottom);
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

        editorTable.setValueAt("Óra Neve", 0, 0);
        editorTable.setValueAt(dataButton.name, 0, 1);
        editorTable.setValueAt("Nap", 1, 0);
        editorTable.setValueAt(dataButton.day, 1, 1);
        editorTable.setValueAt("Óra Típusa", 2, 0);
        editorTable.setValueAt(dataButton.type, 2, 1);
        editorTable.setValueAt("Kezdés Idő", 3, 0);
        editorTable.setValueAt(dataButton.startTime.toString(), 3, 1);
        editorTable.setValueAt("Végzés Idő", 4, 0);
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

        if(container instanceof JLabel || container instanceof JCheckBox || container instanceof JTextField) {
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