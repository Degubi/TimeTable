package degubi.listeners;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import degubi.TimeTableMain;

public class MainFrameIconifier extends WindowAdapter{

	@Override
	public void windowDeiconified(WindowEvent e) {
		TimeTableMain.screenshotItem.setEnabled(true);
		TimeTableMain.screenshotItem.setToolTipText(null);
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
		TimeTableMain.mainPanel.getTopLevelAncestor().setVisible(false);
		TimeTableMain.screenshotItem.setEnabled(false);
		TimeTableMain.screenshotItem.setToolTipText("Nem lehet fényképet készíteni ha nem látszik az órarend");
	}
}