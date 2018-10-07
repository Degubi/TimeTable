package degubi.gui;

import static java.awt.Toolkit.getDefaultToolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
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

import degubi.TimeTableMain;
import degubi.listeners.DataTableListener;
import degubi.tools.NIO;
import degubi.tools.PropertyFile;

public final class PopupGuis extends AbstractAction{
	public static final ImageIcon editIcon = new ImageIcon(getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/edit.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon deleteIcon = new ImageIcon(getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/delete.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon ignoreIcon = new ImageIcon(getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/ignore.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon unIgnore = new ImageIcon(getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/unignore.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	
	private final JTable dataTable;
	private final char key;
	
	public PopupGuis(char key, JTable dataTable) {
		this.key = key;
		this.dataTable = dataTable;
	}
	
	public static void showRoomFinder(JTable dataTable) {
		ButtonTable buildingTable = new ButtonTable(120, 40, 20, 20, ClassButton.roomData, (String) dataTable.getValueAt(5, 1));
		
		showNewDialog(true, "Teremv�laszt�", 800, 600, frame -> {
			buildingTable.findFirstButton(button -> button.getBackground() == Color.RED)
						 .ifPresent(button -> {
				dataTable.setValueAt(button.getText(), 5, 1);
				frame.dispose();
			});
		}, buildingTable);
	}
	
	public static void showEditorGui(boolean isNew, ClassButton dataButton) {
		JTable dataTable = new JTable(new TableModel());
		dataTable.addMouseListener(new DataTableListener(dataTable));
		dataTable.setBackground(Color.LIGHT_GRAY);
		dataTable.setRowHeight(20);
		dataTable.setBorder(new LineBorder(Color.BLACK, 2, true));
		CustomCellRenderer cellRenderer = new CustomCellRenderer();
		
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
		dataTable.setValueAt("�ra Neve", 0, 0);
		dataTable.setValueAt(dataButton.className, 0, 1);
		dataTable.setValueAt("Nap", 1, 0);
		dataTable.setValueAt(dataButton.day, 1, 1);
		dataTable.setValueAt("�ra T�pusa", 2, 0);
		dataTable.setValueAt(dataButton.classType, 2, 1);
		dataTable.setValueAt("Kezd�s Id�", 3, 0);
		dataTable.setValueAt(dataButton.startTime, 3, 1);
		dataTable.setValueAt("V�gz�s Id�", 4, 0);
		dataTable.setValueAt(dataButton.endTime, 4, 1);
		dataTable.setValueAt("Terem", 5, 0);
		dataTable.setValueAt(dataButton.room, 5, 1);
		
		showNewDialog(true, "Editor Gui", 400, 300, frame -> {
			if(dataTable.getCellEditor() != null) dataTable.getCellEditor().stopCellEditing();
			
			String newData = dataTable.getValueAt(1, 1).toString() + ' ' + dataTable.getValueAt(0, 1) + ' ' + dataTable.getValueAt(2, 1) + ' ' + dataTable.getValueAt(3, 1) + ' ' + dataTable.getValueAt(4, 1) + ' ' + dataTable.getValueAt(5, 1) + ' ' + dataButton.unImportant;
			ClassButton.addOrReplaceButton(isNew, dataButton, newData);
			frame.dispose();
		}, dataTable);
	}
	
	public static void showSettingsGui(@SuppressWarnings("unused") ActionEvent event) {
		Path scriptPath = Paths.get("iconScript.vbs");
		String cutPath = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\TimeTable.lnk";
		LocalTime now = LocalTime.now();
		String[] timeValues = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};

		var currentClass = newColorButton(40, PropertyFile.currentClassColor);
		var beforeClass = newColorButton(80, PropertyFile.upcomingClassColor);
		var otherClass = newColorButton(140, PropertyFile.otherClassColor);
		var pastClass = newColorButton(200, PropertyFile.pastClassColor);
		var unimportantClass = newColorButton(260, PropertyFile.unimportantClassColor);
		var dayTimeColor = newColorButton(320, PropertyFile.dayTimeColor);
		var nightTimeColor = newColorButton(380, PropertyFile.nightTimeColor);
		
		var startTimeBox = newComboBox(PropertyFile.dayTimeStart.toString(), 60, timeValues);
		var endTimeBox = newComboBox(PropertyFile.dayTimeEnd.toString(), 120, timeValues);
		var timeBeforeNoteBox = newComboBox(Integer.toString(PropertyFile.noteTime), 270, "30", "40", "50", "60", "70", "80", "90");
		var updateIntervalBox = newComboBox(Integer.toString(PropertyFile.updateInterval / 60), 340, "5", "10", "15", "20");
		
		JCheckBox popupCheckBox = new JCheckBox((String)null, PropertyFile.enablePopups);
		popupCheckBox.setOpaque(false);
		TimeTableMain.handleNightMode(popupCheckBox, now);
		popupCheckBox.setBounds(150, 660, 200, 20);
		JCheckBox startupBox = new JCheckBox((String)null, Files.exists(Paths.get(cutPath)));
		startupBox.setOpaque(false);
		TimeTableMain.handleNightMode(startupBox, now);
		startupBox.setBounds(150, 700, 200, 20);
		
		JDialog settingsFrame = new JDialog((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor(), "Be�ll�t�sok", true);
		settingsFrame.setResizable(false);
		settingsFrame.setBounds(0, 0, 800, 600);
		settingsFrame.setLocationRelativeTo(null);
		settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		BrightablePanel scrollPanel = new BrightablePanel();
		TimeTableMain.handleNightMode(scrollPanel, now);
		scrollPanel.setPreferredSize(new Dimension(500, 850));
		JScrollPane scrollPane = new JScrollPane(scrollPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setBorder(null);
		
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
		saveButton.setBounds(600, 800, 120, 40);
		saveButton.setBackground(Color.GRAY);
		saveButton.setForeground(Color.BLACK);
		saveButton.addActionListener(ev -> {
			try {
				PropertyFile.dayTimeStart = LocalTime.parse((CharSequence) startTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
				PropertyFile.setString("dayTimeStart", (String) startTimeBox.getSelectedItem());
				PropertyFile.dayTimeEnd = LocalTime.parse((CharSequence) endTimeBox.getSelectedItem(), DateTimeFormatter.ISO_LOCAL_TIME);
				PropertyFile.setString("dayTimeEnd", (String) endTimeBox.getSelectedItem());
				PropertyFile.setBoolean("enablePopups", PropertyFile.enablePopups = popupCheckBox.isSelected());
				PropertyFile.setColor("currentClassColor", PropertyFile.currentClassColor = currentClass.getBackground());
				PropertyFile.setColor("upcomingClassColor", PropertyFile.upcomingClassColor = beforeClass.getBackground());
				PropertyFile.setColor("otherClassColor", PropertyFile.otherClassColor = otherClass.getBackground());
				PropertyFile.setColor("pastClassColor", PropertyFile.pastClassColor = pastClass.getBackground());
				PropertyFile.setColor("unimportantClassColor", PropertyFile.unimportantClassColor = unimportantClass.getBackground());
				PropertyFile.setColor("dayTimeColor", PropertyFile.dayTimeColor = dayTimeColor.getBackground());
				PropertyFile.setColor("nightTimeColor", PropertyFile.nightTimeColor = nightTimeColor.getBackground());
				PropertyFile.setInt("noteTime", PropertyFile.noteTime = Integer.parseInt((String) timeBeforeNoteBox.getSelectedItem()));
				PropertyFile.setInt("updateInterval", PropertyFile.updateInterval = Integer.parseInt((String) updateIntervalBox.getSelectedItem()) * 60);
				
				ClassButton.updateAllButtons(false, TimeTableMain.dataTable);
				settingsFrame.dispose();
			}catch (NumberFormatException | DateTimeParseException e) {
				JOptionPane.showMessageDialog(settingsFrame, "Valamelyik adat nem megfelel� form�tum�!", "Rossz adat", JOptionPane.INFORMATION_MESSAGE);
			}
			
			if(startupBox.isSelected()) {
				Path jarPath = NIO.getFullPath("./TimeTable.jar");
				if(Files.exists(jarPath)) {
					Process proc = NIO.createLink(scriptPath, jarPath.toString(), cutPath);
					
					while(proc.isAlive()) Thread.onSpinWait();
				}
				
			}else{
				NIO.deleteIfExists(Paths.get(cutPath));
			}
			
			NIO.deleteIfExists(scriptPath);
		});
		scrollPanel.add(saveButton);
		
		settingsFrame.setContentPane(scrollPane);
		settingsFrame.setVisible(true);
	}
	
	private static void addSettingButton(JComponent component, int y, String labelText, JPanel mainPanel, LocalTime time) {
		component.setLocation(400, y);
		mainPanel.add(component);
		
		JLabel label = new JLabel(labelText);
		label.setFont(ButtonTable.tableHeaderFont);
		TimeTableMain.handleNightMode(label, time);
		label.setBounds(100, y + (component instanceof JCheckBox ? -5 : component instanceof JButton ? 5 : 0), 400, 30);
		mainPanel.add(label);
	}
	
	private static JComboBox<String> newComboBox(String selectedItem, int y, String... data){
		JComboBox<String> endTimeBox = new JComboBox<>(data);
		endTimeBox.setBounds(100, y, 75, 30);
		endTimeBox.setSelectedItem(selectedItem);
		return endTimeBox;
	}
	
	private static JButton newColorButton(int y, Color startColor) {
		JButton butt = new JButton();
		butt.setBackground(startColor);
		butt.setBounds(200, y, 48, 48);
		butt.setFocusable(false);
		butt.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(TimeTableMain.mainPanel, "Sz�nv�laszt�", butt.getBackground());
			if(newColor != null) {
				butt.setBackground(newColor);
			}
		});
		return butt;
	}
	
	public static void showNewDialog(boolean modal, String title, int width, int height, Consumer<JDialog> saveListener, JComponent... components) {
		JDialog frame = new JDialog((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor(), title, modal);
		BrightablePanel panel = new BrightablePanel();
		
		frame.setIconImage(TimeTableMain.icon);
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		panel.setLayout(null);
		frame.setResizable(false);
		frame.setBounds(0, 0, width, height);
		frame.setLocationRelativeTo(TimeTableMain.mainPanel);
		if(saveListener != null) {
			JButton saveButton = new JButton("Ment�s");
			saveButton.setFocusable(false);
			saveButton.setBounds(width / 2 - 70, height - 90, 120, 40);
			saveButton.setBackground(Color.GRAY);
			saveButton.setForeground(Color.BLACK);
			saveButton.addActionListener(e -> saveListener.accept(frame));
			panel.add(saveButton);
		}
		for(JComponent component : components) panel.add(component);
		TimeTableMain.handleNightMode(panel, LocalTime.now());
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
				dataTable.setValueAt(dataTable.getValueAt(2, 1).toString().charAt(0) == 'E' ? "Gyakorlat" : "El�ad�s", 2, 1);
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