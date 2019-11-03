package timetable;

import java.awt.event.*;
import javax.swing.*;

public final class SystemTrayListener extends MouseAdapter{
	private final JPopupMenu popMenu;
	
	public SystemTrayListener(JPopupMenu popMenu) {
		this.popMenu = popMenu;
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON3) {
			popMenu.setLocation(event.getX() - 160, event.getY());
			popMenu.setInvoker(popMenu);
			popMenu.setVisible(true);
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
			Main.updateClasses();
			var top = (JFrame) Main.mainPanel.getTopLevelAncestor();
			top.setVisible(true);
			top.setExtendedState(JFrame.NORMAL);
		}
	}
}