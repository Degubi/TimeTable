package timetable.listeners;

import java.awt.event.*;
import javax.swing.*;
import timetable.*;

public final class SystemTrayListener extends MouseAdapter {
    private final JPopupMenu popMenu;
    private final JFrame stupidPopupMenuHelperFrame = new JFrame();

    public SystemTrayListener(JPopupMenu popMenu) {
        this.popMenu = popMenu;

        stupidPopupMenuHelperFrame.setType(JFrame.Type.UTILITY);
        stupidPopupMenuHelperFrame.setUndecorated(true);
        stupidPopupMenuHelperFrame.setOpacity(0F);
        stupidPopupMenuHelperFrame.addFocusListener(new FocusAdapter(){
            @Override
            public void focusLost(FocusEvent e) {
                if(popMenu.isVisible()) {
                    popMenu.setVisible(false);
                }
            }
        });
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if(event.getButton() == MouseEvent.BUTTON3) {
            stupidPopupMenuHelperFrame.setVisible(true);
            popMenu.setLocation(event.getX() - 160, event.getY());
            popMenu.setInvoker(stupidPopupMenuHelperFrame);
            popMenu.setVisible(true);
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
            openFromTray(null);
        }
    }

    public static void openFromTray(@SuppressWarnings("unused") ActionEvent event) {
        Main.updateClassesGui();
        var top = (JFrame) Main.classesPanel.getTopLevelAncestor();
        top.setVisible(true);
        top.setExtendedState(JFrame.NORMAL);
    }
}