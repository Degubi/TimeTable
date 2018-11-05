package degubi.listeners;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import degubi.TimeTableMain;

public final class MainFrameIconifier extends WindowAdapter{

	@Override
	public void windowDeiconified(WindowEvent e) {
		SystemTrayListener.screenshotItem.setEnabled(true);
		SystemTrayListener.screenshotItem.setToolTipText(null);
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
		TimeTableMain.mainPanel.getTopLevelAncestor().setVisible(false);
		SystemTrayListener.screenshotItem.setEnabled(false);
		SystemTrayListener.screenshotItem.setToolTipText("Nem lehet fényképet készíteni ha nem látszik az órarend");
	}
}