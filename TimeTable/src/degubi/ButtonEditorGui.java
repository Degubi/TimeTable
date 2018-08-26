package degubi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class ButtonEditorGui extends AbstractAction{
	public static final ImageIcon editIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/edit.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon deleteIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/delete.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon ignoreIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/ignore.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	public static final ImageIcon unIgnore = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/unignore.png")).getScaledInstance(32, 32, Image.SCALE_SMOOTH));

	private final JTable dataTable;
	private final char key;
	
	public ButtonEditorGui(char key, JTable dataTable) {
		this.key = key;
		this.dataTable = dataTable;
	}
	
	public static void showRoomFinder(JTable dataTable) {
		JDialog frame = new JDialog(Main.frame, "Temerválasztó", true);
		JButtonTable<JButton> buildingTable = new JButtonTable<>(120, 40, 20, 20, 600, ClassDataButton.roomData, (String) dataTable.getValueAt(5, 1));
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setBounds(0, 0, 800, 600);
		frame.setLocationRelativeTo(null);
		frame.add(buildingTable);
		
		frame.add(Main.newButton("Mentés", 300, 500, 120, 40, e -> {
			buildingTable.findFirstButton(button -> button.getBackground() == Color.RED).ifPresent(button -> {
				dataTable.setValueAt(button.getText(), 5, 1);
				frame.dispose();
			});
		}));
		
		frame.setVisible(true);
	}
	
	public static void showEditorGui(boolean isNew, ClassDataButton dataButton) {
		JDialog frame = new JDialog(Main.frame, "Editor Gui", true);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		frame.setBounds(0, 0, 400, 300);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		JTable dataTable = new JTable(new TableModel());
		dataTable.addMouseListener(new TableClickListener(dataTable));
		dataTable.setBackground(Color.LIGHT_GRAY);
		dataTable.setRowHeight(20);
		CustomCellRenderer cellRenderer = new CustomCellRenderer();
		
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
		dataTable.getActionMap().put("DOWN", new ButtonEditorGui('D', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
		dataTable.getActionMap().put("LEFT", new ButtonEditorGui('L', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
		dataTable.getActionMap().put("RIGHT", new ButtonEditorGui('R', dataTable));
		dataTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
		dataTable.getActionMap().put("UP", new ButtonEditorGui('U', dataTable));
		
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
		
		frame.getContentPane().setBackground(LocalTime.now().isAfter(LocalTime.of(19, 00)) ? Color.DARK_GRAY : new Color(240, 240, 240));
		frame.add(dataTable);
		frame.add(Main.newButton("Mentés", 125, 210, 120, 40, e -> {
			if(dataTable.getCellEditor() != null) dataTable.getCellEditor().stopCellEditing();
			
			String newData = dataTable.getValueAt(1, 1).toString() + ' ' + dataTable.getValueAt(0, 1) + ' ' + dataTable.getValueAt(2, 1) + ' ' + dataTable.getValueAt(3, 1) + ' ' + dataTable.getValueAt(4, 1) + ' ' + dataTable.getValueAt(5, 1) + ' ' + dataButton.unImportant;
			ClassDataButton.addOrReplaceButton(isNew, dataButton, newData);
			frame.dispose();
		}));
		
		frame.setVisible(true);
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
	
	
	public static final class TableClickListener extends MouseAdapter{
		private final JTable table;
		
		public TableClickListener(JTable table) {
			this.table = table;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if(event.getClickCount() == 2 && table.getSelectedColumn() == 1 && table.getSelectedRow() == 5) {
				showRoomFinder(table);
			}
		}
	}
	
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