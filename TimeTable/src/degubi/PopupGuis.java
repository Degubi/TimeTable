package degubi;

import static java.awt.Toolkit.getDefaultToolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class PopupGuis extends AbstractAction implements MouseListener{
	public static final ImageIcon editIcon = new ImageIcon(getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/edit.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon deleteIcon = new ImageIcon(getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/delete.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon ignoreIcon = new ImageIcon(getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/ignore.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon unIgnore = new ImageIcon(getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/unignore.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));

	private final JTable dataTable;
	private final char key;
	
	public PopupGuis(char key, JTable dataTable) {
		this.key = key;
		this.dataTable = dataTable;
	}
	
	public static void showRoomFinder(JTable dataTable) {
		ButtonTable<JButton> buildingTable = new ButtonTable<>(120, 40, 20, 20, ClassButton.roomData, (String) dataTable.getValueAt(5, 1));
		
		showNewDialog("Temerválasztó", 800, 600, frame -> {
			buildingTable.findFirstButton(button -> button.getBackground() == Color.RED).ifPresent(button -> {
				dataTable.setValueAt(button.getText(), 5, 1);
				frame.dispose();
			});
		}, buildingTable);
	}
	
	public static void showEditorGui(boolean isNew, ClassButton dataButton) {
		JTable dataTable = new JTable(new TableModel());
		dataTable.addMouseListener(new PopupGuis('0', dataTable));
		dataTable.setBackground(Color.LIGHT_GRAY);
		dataTable.setRowHeight(20);
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
		
		showNewDialog("Editor Gui", 400, 300, frame -> {
			if(dataTable.getCellEditor() != null) dataTable.getCellEditor().stopCellEditing();
			
			String newData = dataTable.getValueAt(1, 1).toString() + ' ' + dataTable.getValueAt(0, 1) + ' ' + dataTable.getValueAt(2, 1) + ' ' + dataTable.getValueAt(3, 1) + ' ' + dataTable.getValueAt(4, 1) + ' ' + dataTable.getValueAt(5, 1) + ' ' + dataButton.unImportant;
			ClassButton.addOrReplaceButton(isNew, dataButton, newData);
			frame.dispose();
		}, dataTable);
	}
	
	public static void showSettingsGui(@SuppressWarnings("unused") ActionEvent event) {
		JButton currentClass = newColorButton(20, ClassButton.currentClassColor);
		JButton beforeClass = newColorButton(80, ClassButton.upcomingClassColor);
		JButton otherClass = newColorButton(140, ClassButton.otherClassColor);
		JButton pastClass = newColorButton(200, ClassButton.pastClassColor);
		JButton unimportantClass = newColorButton(260, ClassButton.unimportantClassColor);
		JButton dayTimeColor = newColorButton(320, Main.dayTimeColor);
		JButton nightTimeColor = newColorButton(380, Main.nightTimeColor);
		
		JTextField startField = new JTextField(Main.dayTimeStart.toString());
		startField.setBounds(400, 60, 40, 20);
		JTextField endField = new JTextField(Main.dayTimeEnd.toString());
		endField.setBounds(400, 120, 40, 20);
		
		JCheckBox popupCheckBox = new JCheckBox("Üzenetek Bekapcsolva", Main.enablePopups);
		popupCheckBox.setOpaque(false);
		popupCheckBox.setForeground(Main.isDarkMode(LocalTime.now()) ? Color.WHITE : Color.BLACK);
		popupCheckBox.setBounds(350, 160, 200, 20);
		
		Path scriptPath = Paths.get("iconScript.vbs");
		String cutPath = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\TimeTable.lnk";
		
		JCheckBox startupBox = new JCheckBox("Indítás PC Indításakor", Files.exists(Paths.get(cutPath)));
		startupBox.setOpaque(false);
		startupBox.setForeground(Main.isDarkMode(LocalTime.now()) ? Color.WHITE : Color.BLACK);
		startupBox.setBounds(350, 200, 200, 20);
		
		showNewDialog("Beállítások", 600, 600, frame -> {
			Main.settingsFile.setColor("currentClassColor", ClassButton.currentClassColor = currentClass.getBackground());
			Main.settingsFile.setColor("upcomingClassColor", ClassButton.upcomingClassColor = beforeClass.getBackground());
			Main.settingsFile.setColor("otherClassColor", ClassButton.otherClassColor = otherClass.getBackground());
			Main.settingsFile.setColor("pastClassColor", ClassButton.pastClassColor = pastClass.getBackground());
			Main.settingsFile.setColor("unimportantClassColor", ClassButton.unimportantClassColor = unimportantClass.getBackground());
			Main.settingsFile.setColor("dayTimeColor", Main.dayTimeColor = dayTimeColor.getBackground());
			Main.settingsFile.setColor("nightTimeColor", Main.nightTimeColor = nightTimeColor.getBackground());
			
			Main.dayTimeStart = LocalTime.parse(startField.getText(), DateTimeFormatter.ISO_LOCAL_TIME);
			Main.settingsFile.set("dayTimeStart", startField.getText());
			Main.dayTimeEnd = LocalTime.parse(endField.getText(), DateTimeFormatter.ISO_LOCAL_TIME);
			Main.settingsFile.set("dayTimeEnd", endField.getText());
			Main.settingsFile.setBoolean("enablePopups", Main.enablePopups = popupCheckBox.isSelected());
			
			try {
				if(startupBox.isSelected()) {
					Path jarPath = Paths.get("./TimeTable.jar").toRealPath();
					if(Files.exists(jarPath)) {
						Process proc = createLink(scriptPath, jarPath.toString(), cutPath);
						
						while(proc.isAlive()) {
							Thread.onSpinWait();
						}
					}
					
				}else{
					Path shortcutPath = Paths.get(cutPath);
					if(Files.exists(shortcutPath)) {
						Files.delete(shortcutPath);
					}
				}
				
				if(Files.exists(scriptPath)) {
					Files.delete(scriptPath);
				}
			
			}catch(IOException ex) {}
			
			ClassButton.updateAllButtons(false);
			frame.dispose();
		}, newLabel("Jelenlegi Óra Színe:", 30, 20), newLabel("Következõ Órák Színe:", 30, 80), newLabel("Más Napok Óráinak Színe:", 30, 140),
					 newLabel("Elmúlt Órák Színe:", 30, 200), newLabel("Nem Fontos Órák Színe:", 30, 260), newLabel("Nappali Mód Háttérszíne:", 30, 320), newLabel("Éjszakai Mód Háttérszíne:", 30, 380),
					 currentClass, beforeClass, otherClass, pastClass, unimportantClass, dayTimeColor, nightTimeColor,
					 newLabel("Nappali Idõszak Kezdete:", 350, 20), newLabel("Nappali Idõszak Vége:", 350, 80), startField, endField, popupCheckBox, startupBox);
	}
	
	private static Process createLink(Path tempScriptFile, String filePath, String toSavePath) {
		var command = ("Set oWS = WScript.CreateObject(\"WScript.Shell\")\n" + 
						  "Set oLink = oWS.CreateShortcut(\"" + toSavePath + "\")\n" + 
						  	  "oLink.TargetPath = \"" + filePath + "\"\n" + 
						  	  "oLink.Arguments = \"-window\"\n" + 
						  	  "oLink.WorkingDirectory = \"" + filePath.substring(0, filePath.lastIndexOf("\\")) + "\"\n" +
							  "oLink.Save\n").getBytes();
		try {
			Files.write(tempScriptFile, command);
			return Runtime.getRuntime().exec("wscript.exe iconScript.vbs");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	private static JButton newColorButton(int y, Color startColor) {
		JButton butt = new JButton();
		butt.setBackground(startColor);
		butt.setBorder(Main.blackBorder);
		butt.setBounds(200, y, 48, 48);
		butt.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(Main.frame, "Színválasztó", butt.getBackground());
			if(newColor != null) {
				butt.setBackground(newColor);
			}
		});
		return butt;
	}
	
	private static JLabel newLabel(String labelText, int x, int y) {
		JLabel label = new JLabel(labelText);
		label.setForeground(Main.isDarkMode(LocalTime.now()) ? Color.WHITE : Color.BLACK);
		label.setBounds(x, y + 12, 200, 20);
		return label;
	}
	
	private static void showNewDialog(String title, int width, int height, Consumer<JDialog> saveListener, JComponent... components) {
		JDialog dial = new JDialog(Main.frame, title, true);
		dial.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dial.setLayout(null);
		dial.setResizable(false);
		dial.setBounds(0, 0, width, height);
		dial.setLocationRelativeTo(Main.frame);
		if(saveListener != null) {
			JButton saveButton = new JButton("Mentés");
			saveButton.setFocusable(false);
			saveButton.setBounds(width / 2 - 70, height - 90, 120, 40);
			saveButton.setBorder(Main.blackBorder);
			saveButton.setBackground(Color.GRAY);
			saveButton.setForeground(Color.BLACK);
			saveButton.addActionListener(e -> saveListener.accept(dial));
			dial.add(saveButton);
		}
		for(JComponent component : components) dial.add(component);
		Main.handleNightMode(dial.getContentPane());
		dial.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		int row = dataTable.getSelectedRow();
		
		if(row == 1) {
			if(key == 'R') {
				dataTable.setValueAt(Main.dataTable.getNextOrPrevColumn(true, dataTable.getValueAt(1, 1).toString()), 1, 1);
			}else if(key == 'L') {
				dataTable.setValueAt(Main.dataTable.getNextOrPrevColumn(false, dataTable.getValueAt(1, 1).toString()), 1, 1);
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
	

	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getClickCount() == 2 && dataTable.getSelectedColumn() == 1 && dataTable.getSelectedRow() == 5) {
			showRoomFinder(dataTable);
		}
	}

	@Override public void mouseClicked(MouseEvent e) {} @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
	
	public static final class TableModel extends DefaultTableModel{
		@Override public int getRowCount() { return 6; }
		@Override public int getColumnCount() { return 2; }
		@Override public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == 1 && rowIndex != 1 && rowIndex != 2 && rowIndex != 5; }
	}
	
	public static final class CustomCellRenderer extends DefaultTableCellRenderer{
		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(column == 0 ? Color.DARK_GRAY : Color.BLACK);
			return cell;
		}
	}
}