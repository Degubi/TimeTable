package timetable;

import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public final class PopupGuis extends AbstractAction{
    public static final ImageIcon editIcon = Components.getIcon("edit.png", 32);
    public static final ImageIcon deleteIcon = Components.getIcon("delete.png", 32);
    public static final ImageIcon ignoreIcon = Components.getIcon("ignore.png", 32);
    public static final ImageIcon unIgnore = Components.getIcon("unignore.png", 32);
    
    private final JTable dataTable;
    private final char key;
    
    public PopupGuis(char key, JTable dataTable) {
        this.key = key;
        this.dataTable = dataTable;
    }
    
    public static void showEditorGui(String day, boolean isNew, ClassButton dataButton) {
        var dataTable = new JTable(new TableModel());
        dataTable.addMouseListener(new DataTableListener(dataTable));
        dataTable.setBackground(Color.LIGHT_GRAY);
        dataTable.setRowHeight(20);
        dataTable.setBorder(new LineBorder(Color.BLACK, 2, true));
        
        var inputMap = dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        var actionMap = dataTable.getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
        actionMap.put("DOWN", new PopupGuis('D', dataTable));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
        actionMap.put("LEFT", new PopupGuis('L', dataTable));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
        actionMap.put("RIGHT", new PopupGuis('R', dataTable));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
        actionMap.put("UP", new PopupGuis('U', dataTable));
        dataTable.setFont(new Font("Arial", Font.BOLD, 12));
        dataTable.setBounds(20, 20, 340, 122);
        
        dataTable.setValueAt("�ra Neve", 0, 0);
        dataTable.setValueAt(dataButton.className, 0, 1);
        dataTable.setValueAt("Nap", 1, 0);
        dataTable.setValueAt(dataButton.day, 1, 1);
        dataTable.setValueAt("�ra T�pusa", 2, 0);
        dataTable.setValueAt(dataButton.classType, 2, 1);
        dataTable.setValueAt("Kezd�s Id�", 3, 0);
        dataTable.setValueAt(dataButton.startTime.toString(), 3, 1);
        dataTable.setValueAt("V�gz�s Id�", 4, 0);
        dataTable.setValueAt(dataButton.endTime.toString(), 4, 1);
        dataTable.setValueAt("Terem", 5, 0);
        dataTable.setValueAt(dataButton.room, 5, 1);
        
        showNewDialog(true, "Editor Gui", 400, 300, frame -> {
            if(dataTable.getCellEditor() != null) dataTable.getCellEditor().stopCellEditing();
            
            if(!isNew) {
                Settings.classes.get(day).remove(dataButton);
            }
            
            var newDay = (String) dataTable.getValueAt(1, 1);
            Settings.classes.get(newDay)
                    .add(new ClassButton(newDay, (String) dataTable.getValueAt(0, 1),
                                                   (String) dataTable.getValueAt(2, 1),
                                                   LocalTime.parse((String) dataTable.getValueAt(3, 1), DateTimeFormatter.ISO_LOCAL_TIME),
                                                   LocalTime.parse((String) dataTable.getValueAt(4, 1), DateTimeFormatter.ISO_LOCAL_TIME),
                                                   (String) dataTable.getValueAt(5, 1), dataButton.unImportant));
            Main.updateClasses();
            frame.dispose();
        }, dataTable);
    }
    
    @SuppressWarnings("boxing")
    public static void showSettingsGui(@SuppressWarnings("unused") ActionEvent event) {
        var startupLinkPath = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\TimeTable.lnk";
        var settingsFrame = new JDialog((JFrame)Main.mainPanel.getTopLevelAncestor(), "Be�ll�t�sok", true);
        var timeValues = IntStream.range(0, 24)
                                  .mapToObj(k -> String.format("%02d:00", k))
                                  .toArray(String[]::new);
        
        Consumer<JButton> colorButtonListener = button -> {
            var frame = new JDialog(settingsFrame, null, false);
            var panel = new JPanel(null);
            panel.setBorder(new LineBorder(Color.GRAY));

            Consumer<JButton> colorButtonPressedListener = e -> {
                button.setBackground(e.getBackground());
                frame.dispose();
            };
            
            panel.add(Components.newColorButton(0, 0, colorButtonPressedListener, new Color(235, 235, 235)));
            panel.add(Components.newColorButton(48, 0, colorButtonPressedListener, new Color(0, 147, 3)));
            panel.add(Components.newColorButton(96, 0, colorButtonPressedListener, new Color(255, 69, 69)));
            panel.add(Components.newColorButton(144, 0, colorButtonPressedListener, new Color(64, 64, 64)));
            panel.add(Components.newColorButton(0, 48, colorButtonPressedListener, new Color(84, 113, 142)));
            panel.add(Components.newColorButton(48, 48, colorButtonPressedListener, new Color(247, 238, 90)));
            panel.add(Components.newColorButton(96, 48, colorButtonPressedListener, new Color(192, 192, 192)));
            panel.add(Components.newColorButton(144, 48, colorButtonPressedListener, Color.ORANGE));
            panel.add(Components.newColorButton(0, 96, colorButtonPressedListener, Color.CYAN));
            panel.add(Components.newColorButton(48, 96, colorButtonPressedListener, Color.MAGENTA));
            panel.add(Components.newColorButton(96, 96, colorButtonPressedListener, Color.YELLOW));
            panel.add(Components.newColorButton(144, 96, colorButtonPressedListener, Color.RED));
            panel.add(Components.newColorButton(0, 144, colorButtonPressedListener, Color.GREEN));
            panel.add(Components.newColorButton(48, 144, colorButtonPressedListener, Color.GRAY));
            panel.add(Components.newColorButton(96, 144, colorButtonPressedListener, Color.PINK));
            panel.add(Components.newColorButton(144, 144, colorButtonPressedListener, new Color(100, 70, 80)));
            
            Components.handleNightMode(panel, LocalTime.now());
            
            var mouse = MouseInfo.getPointerInfo().getLocation();
            frame.setContentPane(panel);
            frame.setUndecorated(true);
            frame.setLocationRelativeTo(Main.mainPanel);
            frame.addFocusListener(new FocusLostListener(frame));
            frame.setBounds(mouse.x, mouse.y, 192, 192);
            frame.setVisible(true);
        };
        
        var currentClass = Components.newColorButton(200, 40, colorButtonListener, Settings.currentClassColor);
        var beforeClass = Components.newColorButton(200, 80, colorButtonListener, Settings.upcomingClassColor);
        var otherClass = Components.newColorButton(200, 140, colorButtonListener, Settings.otherDayClassColor);
        var pastClass = Components.newColorButton(200, 200, colorButtonListener, Settings.pastClassColor);
        var unimportantClass = Components.newColorButton(200, 260, colorButtonListener, Settings.unimportantClassColor);
        var dayTimeColor = Components.newColorButton(200, 320, colorButtonListener, Settings.dayTimeColor);
        var nightTimeColor = Components.newColorButton(200, 380, colorButtonListener, Settings.nightTimeColor);
        
        var startTimeBox = Components.newComboBox(Settings.dayTimeStart.toString(), 60, timeValues);
        var endTimeBox = Components.newComboBox(Settings.dayTimeEnd.toString(), 120, timeValues);
        var timeBeforeNoteBox = Components.newComboBox(Integer.toString(Settings.timeBeforeNotification), 270, "30", "40", "50", "60", "70", "80", "90");
        var updateIntervalBox = Components.newComboBox(Integer.toString(Settings.updateInterval / 60), 340, "5", "10", "15", "20");
        
        var now = LocalTime.now();
        var popupCheckBox = new JCheckBox((String)null, Settings.enablePopups);
        popupCheckBox.setOpaque(false);
        Components.handleNightMode(popupCheckBox, now);
        popupCheckBox.setBounds(150, 660, 200, 20);
        var startupBox = new JCheckBox((String)null, Files.exists(Path.of(startupLinkPath)));
        startupBox.setOpaque(false);
        Components.handleNightMode(startupBox, now);
        startupBox.setBounds(150, 700, 200, 20);

        var scrollPanel = new JPanel(null);
        Components.handleNightMode(scrollPanel, now);
        scrollPanel.setPreferredSize(new Dimension(500, 800));
        var scrollPane = new JScrollPane(scrollPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setBorder(null);
        
        settingsFrame.setResizable(false);
        settingsFrame.setBounds(0, 0, 800, 600);
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setContentPane(scrollPane);
        
        addSettingButton(currentClass, 30, "Jelenlegi �ra Sz�ne", scrollPanel, now);
        addSettingButton(beforeClass, 80, "K�vetkez� �r�k Sz�ne", scrollPanel, now);
        addSettingButton(otherClass, 130, "M�s Napok �r�inak Sz�ne", scrollPanel, now);
        addSettingButton(pastClass, 180, "Elm�lt �r�k Sz�ne", scrollPanel, now);
        addSettingButton(unimportantClass, 230, "Nem Fontos �r�k Sz�ne", scrollPanel, now);
        addSettingButton(dayTimeColor, 280, "Nappali M�d H�tt�rsz�ne", scrollPanel, now);
        addSettingButton(nightTimeColor, 330, "�jszakai M�d H�tt�rsz�ne", scrollPanel, now);
        
        addSettingButton(startTimeBox, 430, "Nappali Id�szak Kezdete", scrollPanel, now);
        addSettingButton(endTimeBox, 480, "Nappali Id�szak V�ge", scrollPanel, now);
        addSettingButton(timeBeforeNoteBox, 530, "�rtes�t�sek Friss�t�si Id�z�t�sei", scrollPanel, now);
        addSettingButton(updateIntervalBox, 580, "�ra El�tti �rtes�t�sek Percben", scrollPanel, now);
        
        addSettingButton(popupCheckBox, 680, "�zenetek Bekapcsolva", scrollPanel, now);
        addSettingButton(startupBox, 730, "Ind�t�s PC Ind�t�sakor", scrollPanel, now);
        
        JButton saveButton = new JButton("Ment�s");
        saveButton.setBounds(600, 720, 120, 40);
        saveButton.setBackground(Color.GRAY);
        saveButton.setForeground(Color.BLACK);
        saveButton.addActionListener(ev -> {
            try {
                Settings.dayTimeStart = LocalTime.parse((CharSequence) startTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
                Settings.dayTimeEnd = LocalTime.parse((CharSequence) endTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
                Settings.enablePopups = popupCheckBox.isSelected();
                Settings.currentClassColor = currentClass.getBackground();
                Settings.currentClassColor = currentClass.getBackground();
                Settings.upcomingClassColor = beforeClass.getBackground();
                Settings.otherDayClassColor = otherClass.getBackground();
                Settings.pastClassColor = pastClass.getBackground();
                Settings.unimportantClassColor = unimportantClass.getBackground();
                Settings.dayTimeColor = dayTimeColor.getBackground();
                Settings.nightTimeColor = nightTimeColor.getBackground();
                Settings.timeBeforeNotification = Integer.parseInt((String) timeBeforeNoteBox.getSelectedItem());
                Settings.updateInterval = Integer.parseInt((String) updateIntervalBox.getSelectedItem()) * 60;
                Settings.saveSettings();
                
                Main.updateClasses();
                settingsFrame.dispose();
            }catch (NumberFormatException | DateTimeParseException e) {
                JOptionPane.showMessageDialog(settingsFrame, "Valamelyik adat nem megfelel� form�tum�!", "Rossz adat", JOptionPane.INFORMATION_MESSAGE);
            }
            //TODO: Fix this
            /*if(startupBox.isSelected()) {
                Settings.createLink(Settings.getFullPath("./TimeTable.jar").toString(), startupLinkPath, "-window");
            }else{
                Settings.deleteIfExists(Path.of(startupLinkPath));
            }*/
        });
        scrollPanel.add(saveButton);
        settingsFrame.setVisible(true);
    }
    
    private static void addSettingButton(JComponent component, int y, String labelText, JPanel mainPanel, LocalTime time) {
        component.setLocation(400, y);
        mainPanel.add(component);
        
        var label = new JLabel(labelText);
        label.setFont(Main.tableHeaderFont);
        Components.handleNightMode(label, time);
        label.setBounds(100, y + (component instanceof JCheckBox ? -5 : component instanceof JButton ? 5 : 0), 400, 30);
        mainPanel.add(label);
    }
    
     private static JDialog newFrame(String title, int width, int height, int minWidth, int minHeight, boolean modal) {
         var frame = new JDialog((JFrame)Main.mainPanel.getTopLevelAncestor(), title, modal);

         frame.setIconImage(Main.icon);
         frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         frame.setBounds(0, 0, width, height);
         frame.setLocationRelativeTo(Main.mainPanel);
         frame.setMinimumSize(new Dimension(minWidth, minHeight));
         return frame;
     }
    
    public static void showNewDialog(boolean modal, String title, int width, int height, Consumer<JDialog> saveListener, JComponent... components) {
        var frame = new JDialog((JFrame)Main.mainPanel.getTopLevelAncestor(), title, modal);
        var panel = new JPanel(null);
        
        frame.setIconImage(Main.icon);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        panel.setLayout(null);
        frame.setResizable(false);
        frame.setBounds(0, 0, width, height);
        frame.setLocationRelativeTo(Main.mainPanel);
        
        if(saveListener != null) {
            JButton saveButton = new JButton("Ment�s");
            saveButton.setFocusable(false);
            saveButton.setBounds(width / 2 - 70, height - 90, 120, 40);
            saveButton.setBackground(Color.GRAY);
            saveButton.setForeground(Color.BLACK);
            saveButton.addActionListener(e -> saveListener.accept(frame));
            panel.add(saveButton);
        }
        
        for(var component : components) panel.add(component);
        Components.handleNightMode(panel, LocalTime.now());
        frame.setVisible(true);
    }
    
    private static String getNextOrPrevColumn(boolean isNext, String[] columns, String day) {
        int currentIndex = Settings.indexOf(day, columns);
        return isNext ? columns[currentIndex == columns.length - 1 ? 0 : ++currentIndex] : columns[currentIndex == 0 ? columns.length - 1 : --currentIndex];
    }
    
    @SuppressWarnings("boxing")
    @Override
    public void actionPerformed(ActionEvent event) {
        int row = dataTable.getSelectedRow();
        
        if(row == 1) {
            if(key == 'R') {
                dataTable.setValueAt(getNextOrPrevColumn(true, Main.days, dataTable.getValueAt(1, 1).toString()), 1, 1);
            }else if(key == 'L') {
                dataTable.setValueAt(getNextOrPrevColumn(false, Main.days, dataTable.getValueAt(1, 1).toString()), 1, 1);
            }
        }else if(row == 2) {
            if(key == 'R' || key == 'L') {
                dataTable.setValueAt(dataTable.getValueAt(2, 1).toString().charAt(0) == 'E' ? "Gyakorlat" : "El�ad�s", 2, 1);
            }
        }else if(row == 3 || row == 4) {
            String[] split = dataTable.getValueAt(row, 1).toString().split(":");
            
            if(key == 'D') {
                int hours = Integer.parseInt(split[0]);
                dataTable.setValueAt((hours == 0 ? "23" : String.format("%02d", --hours)) + ':' + split[1], row, 1);
            }else if(key == 'L') {
                int minutes = Integer.parseInt(split[1]);
                dataTable.setValueAt(split[0] + ':' + (minutes == 0 ? "45" : String.format("%02d", minutes -= 15)), row, 1);
            }else if(key == 'R') {
                int minutes = Integer.parseInt(split[1]);
                dataTable.setValueAt(split[0] + ':' + (minutes == 45 ? "00" : String.format("%02d", minutes += 15)), row, 1);
            }else if(key == 'U'){
                int hours = Integer.parseInt(split[0]);
                dataTable.setValueAt((hours == 23 ? "00" : String.format("%02d", ++hours)) + ':' + split[1], row, 1);
            }
        }
    }
    
    
    private static final class TableModel extends DefaultTableModel{
        @Override public int getRowCount() { return 6; }
        @Override public int getColumnCount() { return 2; }
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == 1 && rowIndex != 1 && rowIndex != 2 && rowIndex != 5; }
    }
    
    private static final class FocusLostListener extends FocusAdapter{
        private final JDialog component;
        
        public FocusLostListener(JDialog component) {
            this.component = component;
        }

        @Override
        public void focusLost(FocusEvent e) {
            component.dispose();
        }
    }
    
    private static final class DataTableListener extends MouseAdapter{
        private final JTable dataTable;
        
        public DataTableListener(JTable dataTable) {
            this.dataTable = dataTable;
        }

        @Override
        public void mousePressed(MouseEvent event) {
            if(event.getClickCount() == 2 && dataTable.getSelectedColumn() == 1 && dataTable.getSelectedRow() == 5) {
                var frame = newFrame("Teremv�laszt�", 700, 600, 550, 500, true);
                var topPanel = new JPanel(new GridBagLayout());
                var cons = new GridBagConstraints(0, -1, 1, 1, 1D, 0D, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null, 0, 0);
                var buttonDimension = new Dimension(120, 40);
                var topInsent = new Insets(10, 5, 20, 5);
                var bottomInsent = new Insets(0, 5, 5, 5);
                var roomButtons = new ArrayList<JButton>(ClassButton.roomData.values().size());
                var currentRoom = dataTable.getValueAt(5, 1).toString();

                ClassButton.roomData.forEach((building, rooms) -> {
                    cons.insets = topInsent;
                    topPanel.add(Components.newButton(building, Color.BLACK, Color.GRAY, buttonDimension), cons);
                    cons.insets = bottomInsent;
                    
                    Arrays.stream(rooms).forEach(room -> {
                        var isCurrentRoom = currentRoom != null && room.equals(currentRoom);
                        var roomButton = Components.newButton(room, isCurrentRoom ? Color.BLACK : Color.GRAY, isCurrentRoom ? Color.RED : Color.LIGHT_GRAY, buttonDimension);
                        
                        roomButton.addActionListener(e -> {
                            roomButtons.forEach(roomButts -> {
                                roomButts.setForeground(Color.GRAY);
                                roomButts.setBackground(Color.LIGHT_GRAY);
                            });
                            roomButton.setForeground(Color.BLACK);
                            roomButton.setBackground(Color.RED);
                        });
                        roomButtons.add(roomButton);
                        topPanel.add(roomButton, cons);
                    });
                    ++cons.gridx;
                });
                
                var bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                var saveButton = Components.newButton("Save", Color.BLACK, Color.GRAY, new Dimension(120, 40));
                saveButton.addActionListener(e -> roomButtons.stream()
                                                             .filter(button -> button.getBackground() == Color.RED)
                                                             .findFirst()
                                                             .ifPresent(button -> {
                                                                 dataTable.setValueAt(button.getText(), 5, 1);
                                                                 frame.dispose();
                                                             }));
                bottomPanel.add(saveButton);

                frame.add(topPanel, BorderLayout.NORTH);
                frame.add(bottomPanel, BorderLayout.SOUTH);
                frame.setVisible(true);
            }
        }
    }
}