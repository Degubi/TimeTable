package degubi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class ButtonEditorGui{
	public static final ImageIcon editIcon = new ImageIcon(Main.class.getClassLoader().getResource("assets/edit.png"));
	public static final ImageIcon deleteIcon = new ImageIcon(Main.class.getClassLoader().getResource("assets/delete.png"));
	public static final ImageIcon ignoreIcon = new ImageIcon(Main.class.getClassLoader().getResource("assets/ignore.png"));
	public static final ImageIcon unIgnore = new ImageIcon(Main.class.getClassLoader().getResource("assets/unignore.png"));

	public static void showEditorGui(ClassDataButton dataButton) {
		JDialog frame = new JDialog(Main.frame, "Editor Gui");
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		frame.setBounds(0, 0, 400, 300);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		JTable dataTable = new JTable(new TableModel());
		dataTable.setBackground(Color.LIGHT_GRAY);
		dataTable.setRowHeight(20);
		CustomCellRenderer render = new CustomCellRenderer();
		dataTable.getColumnModel().getColumn(0).setCellRenderer(render);
		dataTable.getColumnModel().getColumn(1).setCellRenderer(render);
		dataTable.setFont(new Font("Arial", Font.BOLD, 12));
		dataTable.setBounds(20, 20, 340, 81);
		
		dataTable.setValueAt("Óra Neve", 0, 0);
		dataTable.setValueAt(dataButton.className, 0, 1);
		dataTable.setValueAt("Kezdés Idõ", 1, 0);
		dataTable.setValueAt(dataButton.startTime, 1, 1);
		dataTable.setValueAt("Végzés Idõ", 2, 0);
		dataTable.setValueAt(dataButton.endTime, 2, 1);
		dataTable.setValueAt("Terem", 3, 0);
		dataTable.setValueAt(dataButton.room, 3, 1);
		
		JButton typeSwitchButton = new JButton(dataButton.classType);
		typeSwitchButton.setBounds(240, 130, 120, 40);
		typeSwitchButton.setForeground(Color.BLACK);
		typeSwitchButton.setBackground(Color.LIGHT_GRAY);
		typeSwitchButton.setBorder(Main.blackBorder);
		typeSwitchButton.addActionListener(e -> typeSwitchButton.setText(typeSwitchButton.getText().charAt(0) == 'E' ? "Gyakorlat" : "Elõadás"));
		
		JButton daySwitchButton = new JButton(getHungarianDay(dataButton.day));
		daySwitchButton.setBounds(20, 130, 120, 40);
		daySwitchButton.setForeground(Color.BLACK);
		daySwitchButton.setBackground(Color.LIGHT_GRAY);
		daySwitchButton.setBorder(Main.blackBorder);
		daySwitchButton.addActionListener(e -> daySwitchButton.setText(getHungarianDay(getNextDay(daySwitchButton.getText()))));
		
		JButton saveButton = new JButton("Mentés");
		saveButton.setBounds(125, 210, 120, 40);
		saveButton.setForeground(Color.BLACK);
		saveButton.setBackground(Color.LIGHT_GRAY);
		saveButton.setBorder(Main.blackBorder);
		saveButton.addActionListener(e -> {
			if(dataTable.getCellEditor() != null) dataTable.getCellEditor().stopCellEditing();
			
			String newData = getEnglishDay(daySwitchButton.getText()) + ' ' + dataTable.getValueAt(0, 1) + ' ' + typeSwitchButton.getText() + ' ' + dataTable.getValueAt(1, 1) + ' ' + dataTable.getValueAt(2, 1) + ' ' + dataTable.getValueAt(3, 1) + ' ' + dataButton.unImportant;
			
			if(isDataValid(dataTable.getValueAt(1, 1).toString(), dataTable.getValueAt(2, 1).toString())) {
				ClassDataButton.replaceButton(dataButton, newData);
				frame.dispose();
			}else{
				JOptionPane.showMessageDialog(frame, "Az idõ formátum nem megfelelõ!");
			}
		});
		
		frame.getContentPane().setBackground(LocalTime.now().isAfter(LocalTime.of(18, 00)) ? Color.DARK_GRAY : new Color(240, 240, 240));
		frame.add(dataTable);
		frame.add(daySwitchButton);
		frame.add(typeSwitchButton);
		frame.add(saveButton);
		frame.setVisible(true);
	}
	
	public static void openNewButtonGui() {
		ClassDataButton butt = new ClassDataButton("MONDAY ÓRANÉV Elõadás 08:00 10:00 Terem false");
		ClassDataButton.classData.add(butt);
		ButtonEditorGui.showEditorGui(butt);
	}
	
	private static boolean isDataValid(String startTime, String endTime) {
		try {
			LocalTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_TIME);
			LocalTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_TIME);
			
			return true;
		}catch (Exception e) {
			return false;
		}
	}
	
	private static String getNextDay(String day) {
		int currentIndex = DayOfWeek.valueOf(getEnglishDay(day)).ordinal();
		return DayOfWeek.values()[currentIndex == 4 ? 0 : ++currentIndex].name();
	}
	
	private static String getHungarianDay(String engDay) {
		switch(engDay) {
			case "MONDAY": return "Hétfõ";
			case "TUESDAY": return "Kedd";
			case "WEDNESDAY": return "Szerda";
			case "THURSDAY": return "Csütörtök";
			default: return "Péntek";
		}
	}
	
	private static String getEnglishDay(String hunDay) {
		switch(hunDay) {
			case "Hétfõ": return "MONDAY";
			case "Kedd": return "TUESDAY";
			case "Szerda": return "WEDNESDAY";
			case "Csütörtök": return "THURSDAY";
			default: return "FRIDAY";
		}
	}
	
	public static final class TableModel extends DefaultTableModel{
		@Override public int getRowCount() { return 6; }
		@Override public int getColumnCount() { return 2; }
		@Override public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == 1; }
	}
	
	public static final class CustomCellRenderer extends DefaultTableCellRenderer{
		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(column == 0 ? Color.DARK_GRAY : Color.BLACK);
			return cell;
		}
	}
}