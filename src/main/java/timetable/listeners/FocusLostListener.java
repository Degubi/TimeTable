package timetable.listeners;

import java.awt.event.*;
import javax.swing.*;

public final class FocusLostListener extends WindowAdapter {
    private final JDialog frame;

    public FocusLostListener(JDialog frame) {
        this.frame = frame;
    }
    
    @Override
    public void windowLostFocus(WindowEvent event) {
        frame.dispose();
    }
}