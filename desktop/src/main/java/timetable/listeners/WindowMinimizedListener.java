package timetable.listeners;

import java.awt.event.*;
import javax.swing.*;

public final class WindowMinimizedListener extends WindowAdapter {
    private final JMenuItem screenshotItem;

    public WindowMinimizedListener(JMenuItem screenshotItem) {
        this.screenshotItem = screenshotItem;
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        screenshotItem.setEnabled(true);
        screenshotItem.setToolTipText(null);
    }

    @Override
    public void windowIconified(WindowEvent e) {
    	if(!System.getProperty("os.name").equals("Linux")) {   // This is bugged on Ubuntu, event fires when opening a window... -_-
	        e.getWindow().setVisible(false);
	        screenshotItem.setEnabled(false);
	        screenshotItem.setToolTipText("Nem lehet fényképet készíteni ha nem látszik az órarend");
    	}
    }
}