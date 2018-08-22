package degubi.data;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalTime;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import degubi.Main;

public final class ButtonEditorGui{
	
	public static void showEditorGui(ClassDataButton dataButton) {
		JDialog frame = new JDialog((JFrame)null, "Editor Gui");
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
		dataTable.setBounds(20, 20, 340, 120);
		
		dataTable.setValueAt("Nap", 0, 0);
		dataTable.setValueAt(dataButton.day, 0, 1);
		dataTable.setValueAt("Óra Neve", 1, 0);
		dataTable.setValueAt(dataButton.className, 1, 1);
		dataTable.setValueAt("Kezdés Idõ", 2, 0);
		dataTable.setValueAt(dataButton.startTime, 2, 1);
		dataTable.setValueAt("Végzés Idõ", 3, 0);
		dataTable.setValueAt(dataButton.endTime, 3, 1);
		dataTable.setValueAt("Óra Típusa", 4, 0);
		dataTable.setValueAt(dataButton.classType, 4, 1);
		dataTable.setValueAt("Terem", 5, 0);
		dataTable.setValueAt(dataButton.room, 5, 1);
		
		JButton saveButton = new JButton("Save");
		saveButton.setBounds(125, 210, 120, 40);
		saveButton.setForeground(Color.BLACK);
		saveButton.setBackground(Color.LIGHT_GRAY);
		saveButton.setBorder(Main.blackBorder);
		saveButton.addActionListener(e -> {
			dataButton.refreshDataFromTable(dataTable);
			frame.dispose();
		});
		
		frame.getContentPane().setBackground(LocalTime.now().isAfter(LocalTime.of(18, 00)) ? Color.DARK_GRAY : new Color(240, 240, 240));
		frame.add(dataTable);
		frame.add(saveButton);
		frame.setVisible(true);
	}
	
	public static void openNewButtonGui() {
		ClassDataButton butt = new ClassDataButton("NAP ÓRANÉV Elõadás/Gyak 08:00 10:00 Terem");
		ClassDataButton.classData.add(butt);
		ButtonEditorGui.showEditorGui(butt);
	}
	
	static final class TableModel extends DefaultTableModel{
		
		@Override
		public int getRowCount() {
			return 6;
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}
	}
	
	static final class CustomCellRenderer extends DefaultTableCellRenderer{
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(column == 0 ? Color.DARK_GRAY : Color.BLACK);
			return cell;
		}
	}
}