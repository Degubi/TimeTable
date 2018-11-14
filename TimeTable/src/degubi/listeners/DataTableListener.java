package degubi.listeners;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import degubi.gui.ButtonTable;
import degubi.gui.ClassButton;
import degubi.gui.PopupGuis;

public final class DataTableListener extends MouseAdapter{
	private final JTable dataTable;
	
	public DataTableListener(JTable dataTable) {
		this.dataTable = dataTable;
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getClickCount() == 2 && dataTable.getSelectedColumn() == 1 && dataTable.getSelectedRow() == 5) {
			ButtonTable buildingTable = new ButtonTable(120, 40, 20, 20, ClassButton.roomData, (String) dataTable.getValueAt(5, 1));
				
			PopupGuis.showNewDialog(true, "Teremválasztó", 800, 600, frame -> 
				buildingTable.findFirstButton(button -> button.getBackground() == Color.RED)
							 .ifPresent(button -> {
								 dataTable.setValueAt(button.getText(), 5, 1);
								 frame.dispose();
							 })
			, buildingTable);
		}
	}
}