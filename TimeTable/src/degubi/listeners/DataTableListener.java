package degubi.listeners;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import degubi.PopupGuis;

public class DataTableListener extends MouseAdapter{
	private final JTable dataTable;
	
	public DataTableListener(JTable dataTable) {
		this.dataTable = dataTable;
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getClickCount() == 2 && dataTable.getSelectedColumn() == 1 && dataTable.getSelectedRow() == 5) {
			PopupGuis.showRoomFinder(dataTable);
		}
	}
}