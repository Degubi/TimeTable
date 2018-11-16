package degubi.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.google.gson.JsonObject;

import degubi.TimeTableMain;
import degubi.listeners.DataTableListener;
import degubi.tools.GuiTools;
import degubi.tools.Settings;

public final class PopupGuis extends AbstractAction{
	public static final ImageIcon editIcon = GuiTools.getIcon("edit.png", 32);
	public static final ImageIcon deleteIcon = GuiTools.getIcon("delete.png", 32);
	public static final ImageIcon ignoreIcon = GuiTools.getIcon("ignore.png", 32);
	public static final ImageIcon unIgnore = GuiTools.getIcon("unignore.png", 32);
	
	private final JTable dataTable;
	private final char key;
	
	public PopupGuis(char key, JTable dataTable) {
		this.key = key;
		this.dataTable = dataTable;
	}
	
	public static void showEditorGui(JsonObject currentObj, ClassButton dataButton) {
		JTable dataTable = new JTable(new TableModel());
		dataTable.addMouseListener(new DataTableListener(dataTable));
		dataTable.setBackground(Color.LIGHT_GRAY);
		dataTable.setRowHeight(20);
		dataTable.setBorder(new LineBorder(Color.BLACK, 2, true));
		var cellRenderer = new CustomCellRenderer();
		
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
		dataTable.getActionMap().put("DOWN", new PopupGuis('D', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
		dataTable.getActionMap().put("LEFT", new PopupGuis('L', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
		dataTable.getActionMap().put("RIGHT", new PopupGuis('R', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
		dataTable.getActionMap().put("UP", new PopupGuis('U', dataTable));
		dataTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		dataTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
		dataTable.setFont(new Font("Arial", Font.BOLD, 12));
		dataTable.setBounds(20, 20, 340, 122);
		dataTable.setValueAt("Óra Neve", 0, 0);
		dataTable.setValueAt(dataButton.className, 0, 1);
		dataTable.setValueAt("Nap", 1, 0);
		dataTable.setValueAt(dataButton.day, 1, 1);
		dataTable.setValueAt("Óra Típusa", 2, 0);
		dataTable.setValueAt(dataButton.classType, 2, 1);
		dataTable.setValueAt("Kezdés Idõ", 3, 0);
		dataTable.setValueAt(dataButton.startTime, 3, 1);
		dataTable.setValueAt("Végzés Idõ", 4, 0);
		dataTable.setValueAt(dataButton.endTime, 4, 1);
		dataTable.setValueAt("Terem", 5, 0);
		dataTable.setValueAt(dataButton.room, 5, 1);
		
		showNewDialog(true, "Editor Gui", 400, 300, frame -> {
			if(dataTable.getCellEditor() != null) dataTable.getCellEditor().stopCellEditing();
			
			if(currentObj == null) {
				TimeTableMain.dataTable.addNewClass(Settings.classes, Settings.newClassObject(dataTable, dataButton.unImportant));
			}else{
				TimeTableMain.dataTable.editClass(Settings.classes, currentObj, Settings.newClassObject(dataTable, dataButton.unImportant));
			}
			
			frame.dispose();
		}, dataTable);
	}
	
	public static void showSettingsGui(@SuppressWarnings("unused") ActionEvent event) { // NO_UCD (unused code)
		var startupLinkPath = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\TimeTable.lnk";
		var desktopLinkPath = System.getProperty("user.home") + "\\Desktop\\TimeTable.lnk";
		var now = LocalTime.now();
		String[] timeValues = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
		
		Consumer<JButton> colorButtonListener = button -> {
			Color newColor = JColorChooser.showDialog(TimeTableMain.mainPanel, "Színválasztó", button.getBackground());
			if(newColor != null) {
				button.setBackground(newColor);
			}
		};
		
		var currentClass = GuiTools.newColorButton(200, 40, colorButtonListener, Settings.currentClassColor);
		var beforeClass = GuiTools.newColorButton(200, 80, colorButtonListener, Settings.upcomingClassColor);
		var otherClass = GuiTools.newColorButton(200, 140, colorButtonListener, Settings.otherDayClassColor);
		var pastClass = GuiTools.newColorButton(200, 200, colorButtonListener, Settings.pastClassColor);
		var unimportantClass = GuiTools.newColorButton(200, 260, colorButtonListener, Settings.unimportantClassColor);
		var dayTimeColor = GuiTools.newColorButton(200, 320, colorButtonListener, Settings.dayTimeColor);
		var nightTimeColor = GuiTools.newColorButton(200, 380, colorButtonListener, Settings.nightTimeColor);
		
		var startTimeBox = GuiTools.newComboBox(Settings.dayTimeStart.toString(), 60, timeValues);
		var endTimeBox = GuiTools.newComboBox(Settings.dayTimeEnd.toString(), 120, timeValues);
		var timeBeforeNoteBox = GuiTools.newComboBox(Integer.toString(Settings.noteTime), 270, "30", "40", "50", "60", "70", "80", "90");
		var updateIntervalBox = GuiTools.newComboBox(Integer.toString(Settings.updateInterval / 60), 340, "5", "10", "15", "20");
		
		var popupCheckBox = new JCheckBox((String)null, Settings.enablePopups);
		popupCheckBox.setOpaque(false);
		GuiTools.handleNightMode(popupCheckBox, now);
		popupCheckBox.setBounds(150, 660, 200, 20);
		var startupBox = new JCheckBox((String)null, Files.exists(Path.of(startupLinkPath)));
		startupBox.setOpaque(false);
		GuiTools.handleNightMode(startupBox, now);
		startupBox.setBounds(150, 700, 200, 20);
		var desktopIconBox = new JCheckBox((String)null, Files.exists(Path.of(desktopLinkPath)));
		desktopIconBox.setOpaque(false);
		GuiTools.handleNightMode(desktopIconBox, now);
		desktopIconBox.setBounds(150, 740, 200, 20);

		var scrollPanel = new BrightablePanel();
		GuiTools.handleNightMode(scrollPanel, now);
		scrollPanel.setPreferredSize(new Dimension(500, 850));
		var scrollPane = new JScrollPane(scrollPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setBorder(null);
		
		var settingsFrame = new JDialog((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor(), "Beállítások", true);
		settingsFrame.setResizable(false);
		settingsFrame.setBounds(0, 0, 800, 600);
		settingsFrame.setLocationRelativeTo(null);
		settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsFrame.setContentPane(scrollPane);
		
		addSettingButton(currentClass, 30, "Jelenlegi Óra Színe", scrollPanel, now);
		addSettingButton(beforeClass, 80, "Következõ Órák Színe", scrollPanel, now);
		addSettingButton(otherClass, 130, "Más Napok Óráinak Színe", scrollPanel, now);
		addSettingButton(pastClass, 180, "Elmúlt Órák Színe", scrollPanel, now);
		addSettingButton(unimportantClass, 230, "Nem Fontos Órák Színe", scrollPanel, now);
		addSettingButton(dayTimeColor, 280, "Nappali Mód Háttérszíne", scrollPanel, now);
		addSettingButton(nightTimeColor, 330, "Éjszakai Mód Háttérszíne", scrollPanel, now);
		
		addSettingButton(startTimeBox, 430, "Nappali Idõszak Kezdete", scrollPanel, now);
		addSettingButton(endTimeBox, 480, "Nappali Idõszak Vége", scrollPanel, now);
		addSettingButton(timeBeforeNoteBox, 530, "Értesítések Frissítési Idõzítései", scrollPanel, now);
		addSettingButton(updateIntervalBox, 580, "Óra Elõtti Értesítések Percben", scrollPanel, now);
		
		addSettingButton(popupCheckBox, 680, "Üzenetek Bekapcsolva", scrollPanel, now);
		addSettingButton(startupBox, 730, "Indítás PC Indításakor", scrollPanel, now);
		addSettingButton(desktopIconBox, 780, "Asztali Parancsikon", scrollPanel, now);
		
		JButton saveButton = new JButton("Mentés");
		saveButton.setBounds(600, 800, 120, 40);
		saveButton.setBackground(Color.GRAY);
		saveButton.setForeground(Color.BLACK);
		saveButton.addActionListener(ev -> {
			try {
				Settings.dayTimeStart = LocalTime.parse((CharSequence) startTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
				Settings.updateString("dayTimeStart", (String) startTimeBox.getSelectedItem());
				Settings.dayTimeEnd = LocalTime.parse((CharSequence) endTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
				Settings.updateString("dayTimeEnd", (String) endTimeBox.getSelectedItem());
				Settings.updateBoolean("enablePopups", Settings.enablePopups = popupCheckBox.isSelected());
				Settings.updateColor("currentClassColor", Settings.currentClassColor = currentClass.getBackground());
				Settings.updateColor("currentClassColor", Settings.currentClassColor = currentClass.getBackground());
				Settings.updateColor("upcomingClassColor", Settings.upcomingClassColor = beforeClass.getBackground());
				Settings.updateColor("otherDayClassColor", Settings.otherDayClassColor = otherClass.getBackground());
				Settings.updateColor("pastClassColor", Settings.pastClassColor = pastClass.getBackground());
				Settings.updateColor("unimportantClassColor", Settings.unimportantClassColor = unimportantClass.getBackground());
				Settings.updateColor("dayTimeColor", Settings.dayTimeColor = dayTimeColor.getBackground());
				Settings.updateColor("nightTimeColor", Settings.nightTimeColor = nightTimeColor.getBackground());
				Settings.updateInt("noteTime", Settings.noteTime = Integer.parseInt((String) timeBeforeNoteBox.getSelectedItem()));
				Settings.updateInt("updateInterval", Settings.updateInterval = Integer.parseInt((String) updateIntervalBox.getSelectedItem()) * 60);
				Settings.save();
				
				ClassButton.updateAllButtons(false, TimeTableMain.dataTable);
				settingsFrame.dispose();
			}catch (NumberFormatException | DateTimeParseException e) {
				JOptionPane.showMessageDialog(settingsFrame, "Valamelyik adat nem megfelelõ formátumú!", "Rossz adat", JOptionPane.INFORMATION_MESSAGE);
			}
			var jarPath = Settings.getFullPath("./TimeTable.jar");
			
			if(startupBox.isSelected()) {
				Settings.createLink(jarPath.toString(), startupLinkPath, "-window");
			}else{
				Settings.deleteIfExists(Path.of(startupLinkPath));
			}
			
			if(desktopIconBox.isSelected()) {
				Settings.createLink(jarPath.toString(), desktopLinkPath, "");
			}else{
				Settings.deleteIfExists(Path.of(desktopLinkPath));
			}
		});
		scrollPanel.add(saveButton);
		settingsFrame.setVisible(true);
	}
	
	private static void addSettingButton(JComponent component, int y, String labelText, JPanel mainPanel, LocalTime time) {
		component.setLocation(400, y);
		mainPanel.add(component);
		
		var label = new JLabel(labelText);
		label.setFont(ButtonTable.tableHeaderFont);
		GuiTools.handleNightMode(label, time);
		label.setBounds(100, y + (component instanceof JCheckBox ? -5 : component instanceof JButton ? 5 : 0), 400, 30);
		mainPanel.add(label);
	}
	
	public static void showNewDialog(boolean modal, String title, int width, int height, Consumer<JDialog> saveListener, JComponent... components) {
		var frame = new JDialog((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor(), title, modal);
		var panel = new BrightablePanel();
		
		frame.setIconImage(TimeTableMain.icon);
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		panel.setLayout(null);
		frame.setResizable(false);
		frame.setBounds(0, 0, width, height);
		frame.setLocationRelativeTo(TimeTableMain.mainPanel);
		if(saveListener != null) {
			JButton saveButton = new JButton("Mentés");
			saveButton.setFocusable(false);
			saveButton.setBounds(width / 2 - 70, height - 90, 120, 40);
			saveButton.setBackground(Color.GRAY);
			saveButton.setForeground(Color.BLACK);
			saveButton.addActionListener(e -> saveListener.accept(frame));
			panel.add(saveButton);
		}
		
		for(var component : components) panel.add(component);
		GuiTools.handleNightMode(panel, LocalTime.now());
		frame.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		int row = dataTable.getSelectedRow();
		
		if(row == 1) {
			if(key == 'R') {
				dataTable.setValueAt(TimeTableMain.dataTable.getNextOrPrevColumn(true, dataTable.getValueAt(1, 1).toString()), 1, 1);
			}else if(key == 'L') {
				dataTable.setValueAt(TimeTableMain.dataTable.getNextOrPrevColumn(false, dataTable.getValueAt(1, 1).toString()), 1, 1);
			}
		}else if(row == 2) {
			if(key == 'R' || key == 'L') {
				dataTable.setValueAt(dataTable.getValueAt(2, 1).toString().charAt(0) == 'E' ? "Gyakorlat" : "Elõadás", 2, 1);
			}
		}else if(row == 3 || row == 4) {
			String[] split = dataTable.getValueAt(row, 1).toString().split(":");
			
			if(key == 'D') {
				int hours = Integer.parseInt(split[0]);
				dataTable.setValueAt((hours == 0 ? "23" : hours < 11 ? "0" + --hours : Integer.toString(--hours)) + ":" + split[1], row, 1);
			}else if(key == 'L') {
				int minutes = Integer.parseInt(split[1]);
				dataTable.setValueAt(split[0] + ":" + (minutes == 0 ? "59" : minutes < 11 ? "0" + --minutes : Integer.toString(--minutes)), row, 1);
			}else if(key == 'R') {
				int minutes = Integer.parseInt(split[1]);
				dataTable.setValueAt(split[0] + ":" + (minutes == 59 ? "00" : minutes < 9 ? "0" + ++minutes : Integer.toString(++minutes)), row, 1);
			}else if(key == 'U'){
				int hours = Integer.parseInt(split[0]);
				dataTable.setValueAt((hours == 23 ? "00" : hours < 9 ? "0" + ++hours : Integer.toString(++hours)) + ":" + split[1], row, 1);
			}
		}
	}
	
	
	private static final class TableModel extends DefaultTableModel{
		@Override public int getRowCount() { return 6; }
		@Override public int getColumnCount() { return 2; }
		@Override public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == 1 && rowIndex != 1 && rowIndex != 2 && rowIndex != 5; }
	}
	
	private static final class CustomCellRenderer extends DefaultTableCellRenderer{
		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(column == 0 ? Color.DARK_GRAY : Color.BLACK);
			return cell;
		}
	}
}