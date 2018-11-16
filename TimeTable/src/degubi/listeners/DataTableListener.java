package degubi.listeners;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import degubi.gui.ButtonTable;
import degubi.gui.ClassButton;
import degubi.gui.PopupGuis;
import degubi.tools.GuiTools;

public final class DataTableListener implements GuiTools{
	private final JTable dataTable;
	
	public DataTableListener(JTable dataTable) {
		this.dataTable = dataTable;
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getClickCount() == 2 && dataTable.getSelectedColumn() == 1 && dataTable.getSelectedRow() == 5) {
			var buildingTable = new ButtonTable(120, 40, 20, 20, ClassButton.roomData, (String) dataTable.getValueAt(5, 1));
				
			PopupGuis.showNewDialog(true, "Teremválasztó", 800, 600, frame -> 
				
				buildingTable.dataButtonList.stream()
							 .filter(button -> button.getBackground() == Color.RED)
							 .findFirst()
							 .ifPresent(button -> {
								 dataTable.setValueAt(button.getText(), 5, 1);
								 frame.dispose();
							 })
			, buildingTable);
		}
	}
}