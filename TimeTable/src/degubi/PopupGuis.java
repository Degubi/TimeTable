package degubi;

import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public final class PopupGuis extends AbstractAction{
	public static final ImageIcon editIcon = Main.getIcon("edit.png", 32);
	public static final ImageIcon deleteIcon = Main.getIcon("delete.png", 32);
	public static final ImageIcon ignoreIcon = Main.getIcon("ignore.png", 32);
	public static final ImageIcon unIgnore = Main.getIcon("unignore.png", 32);
	
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
		
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
		dataTable.getActionMap().put("DOWN", new PopupGuis('D', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
		dataTable.getActionMap().put("LEFT", new PopupGuis('L', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
		dataTable.getActionMap().put("RIGHT", new PopupGuis('R', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
		dataTable.getActionMap().put("UP", new PopupGuis('U', dataTable));
		dataTable.setFont(new Font("Arial", Font.BOLD, 12));
		dataTable.setBounds(20, 20, 340, 122);
		
		dataTable.setValueAt("Óra Neve", 0, 0);
		dataTable.setValueAt(dataButton.className, 0, 1);
		dataTable.setValueAt("Nap", 1, 0);
		dataTable.setValueAt(dataButton.day, 1, 1);
		dataTable.setValueAt("Óra Típusa", 2, 0);
		dataTable.setValueAt(dataButton.classType, 2, 1);
		dataTable.setValueAt("Kezdés Idõ", 3, 0);
		dataTable.setValueAt(dataButton.startTime.toString(), 3, 1);
		dataTable.setValueAt("Végzés Idõ", 4, 0);
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
	
	public static void showSettingsGui(@SuppressWarnings("unused") ActionEvent event) { // NO_UCD (unused code)
		var startupLinkPath = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\TimeTable.lnk";
		var desktopLinkPath = System.getProperty("user.home") + "\\Desktop\\TimeTable.lnk";
		var now = LocalTime.now();
		String[] timeValues = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
		
		Consumer<JButton> colorButtonListener = button -> {
			Color newColor = JColorChooser.showDialog(Main.mainPanel, "Színválasztó", button.getBackground());
			if(newColor != null) {
				button.setBackground(newColor);
			}
		};
		
		var currentClass = newColorButton(200, 40, colorButtonListener, Settings.currentClassColor);
		var beforeClass = newColorButton(200, 80, colorButtonListener, Settings.upcomingClassColor);
		var otherClass = newColorButton(200, 140, colorButtonListener, Settings.otherDayClassColor);
		var pastClass = newColorButton(200, 200, colorButtonListener, Settings.pastClassColor);
		var unimportantClass = newColorButton(200, 260, colorButtonListener, Settings.unimportantClassColor);
		var dayTimeColor = newColorButton(200, 320, colorButtonListener, Settings.dayTimeColor);
		var nightTimeColor = newColorButton(200, 380, colorButtonListener, Settings.nightTimeColor);
		
		var startTimeBox = newComboBox(Settings.dayTimeStart.toString(), 60, timeValues);
		var endTimeBox = newComboBox(Settings.dayTimeEnd.toString(), 120, timeValues);
		var timeBeforeNoteBox = newComboBox(Integer.toString(Settings.timeBeforeNotification), 270, "30", "40", "50", "60", "70", "80", "90");
		var updateIntervalBox = newComboBox(Integer.toString(Settings.updateInterval / 60), 340, "5", "10", "15", "20");
		
		var popupCheckBox = new JCheckBox((String)null, Settings.enablePopups);
		popupCheckBox.setOpaque(false);
		Main.handleNightMode(popupCheckBox, now);
		popupCheckBox.setBounds(150, 660, 200, 20);
		var startupBox = new JCheckBox((String)null, Files.exists(Path.of(startupLinkPath)));
		startupBox.setOpaque(false);
		Main.handleNightMode(startupBox, now);
		startupBox.setBounds(150, 700, 200, 20);
		var desktopIconBox = new JCheckBox((String)null, Files.exists(Path.of(desktopLinkPath)));
		desktopIconBox.setOpaque(false);
		Main.handleNightMode(desktopIconBox, now);
		desktopIconBox.setBounds(150, 740, 200, 20);

		var scrollPanel = new JPanel(null);
		Main.handleNightMode(scrollPanel, now);
		scrollPanel.setPreferredSize(new Dimension(500, 850));
		var scrollPane = new JScrollPane(scrollPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setBorder(null);
		
		var settingsFrame = new JDialog((JFrame)Main.mainPanel.getTopLevelAncestor(), "Beállítások", true);
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
		label.setFont(Main.tableHeaderFont);
		Main.handleNightMode(label, time);
		label.setBounds(100, y + (component instanceof JCheckBox ? -5 : component instanceof JButton ? 5 : 0), 400, 30);
		mainPanel.add(label);
	}
	
	 private static JComboBox<String> newComboBox(String selectedItem, int y, String... data){
		 var endTimeBox = new JComboBox<>(data);
		 endTimeBox.setBounds(100, y, 75, 30);
		 endTimeBox.setSelectedItem(selectedItem);
		 return endTimeBox;
	}

	 private static JButton newColorButton(int x, int y, Consumer<JButton> listener, Color startColor) {
		 var butt = new JButton();
		 butt.setBackground(startColor);
		 butt.setBounds(x, y, 48, 48);
		 butt.setFocusable(false);
		 butt.setBorder(null);
		 butt.addActionListener(e -> listener.accept(butt));
		 return butt;
	 }

	 private static JButton newButton(String text, Color foreground, Color background, Dimension preferredSize) {
		 var butt = new JButton(text);
		 butt.setFocusPainted(false);
		 butt.setForeground(foreground);
		 butt.setBackground(background);
		 butt.setPreferredSize(preferredSize);
		 return butt;
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
			JButton saveButton = new JButton("Mentés");
			saveButton.setFocusable(false);
			saveButton.setBounds(width / 2 - 70, height - 90, 120, 40);
			saveButton.setBackground(Color.GRAY);
			saveButton.setForeground(Color.BLACK);
			saveButton.addActionListener(e -> saveListener.accept(frame));
			panel.add(saveButton);
		}
		
		for(var component : components) panel.add(component);
		Main.handleNightMode(panel, LocalTime.now());
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
				dataTable.setValueAt(dataTable.getValueAt(2, 1).toString().charAt(0) == 'E' ? "Gyakorlat" : "Elõadás", 2, 1);
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
	
	private static final class DataTableListener extends MouseAdapter{
		private final JTable dataTable;
		
		public DataTableListener(JTable dataTable) {
			this.dataTable = dataTable;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if(event.getClickCount() == 2 && dataTable.getSelectedColumn() == 1 && dataTable.getSelectedRow() == 5) {
				var frame = newFrame("Teremválasztó", 700, 600, 550, 500, true);
				var topPanel = new JPanel(new GridBagLayout());
				var cons = new GridBagConstraints(0, -1, 1, 1, 1D, 0D, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null, 0, 0);
				var buttonDimension = new Dimension(120, 40);
				var topInsent = new Insets(10, 5, 20, 5);
				var bottomInsent = new Insets(0, 5, 5, 5);
				var roomButtons = new ArrayList<JButton>(ClassButton.roomData.values().size());
				var currentRoom = dataTable.getValueAt(5, 1).toString();

				ClassButton.roomData.forEach((building, rooms) -> {
					cons.insets = topInsent;
					topPanel.add(newButton(building, Color.BLACK, Color.GRAY, buttonDimension), cons);
					cons.insets = bottomInsent;
					
					Arrays.stream(rooms).forEach(room -> {
						var isCurrentRoom = currentRoom != null && room.equals(currentRoom);
						var roomButton = newButton(room, isCurrentRoom ? Color.BLACK : Color.GRAY, isCurrentRoom ? Color.RED : Color.LIGHT_GRAY, buttonDimension);
						
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
				var saveButton = newButton("Save", Color.BLACK, Color.GRAY, new Dimension(120, 40));
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