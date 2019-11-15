package timetable.listeners;

import static timetable.Main.*;

import java.awt.event.*;
import javax.swing.*;

public final class WindowMinimizedListener extends WindowAdapter{
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
        classesPanel.getTopLevelAncestor().setVisible(false);
        screenshotItem.setEnabled(false);
        screenshotItem.setToolTipText("Nem lehet fényképet készíteni ha nem látszik az órarend");
    }
}