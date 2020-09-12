package timetable.listeners;

import java.awt.event.*;

public final class WindowClosedListener extends WindowAdapter {

    private final Runnable saveAction;

    public WindowClosedListener(Runnable saveAction) {
        this.saveAction = saveAction;
    }

    @Override
    public void windowClosed(WindowEvent event) {
        saveAction.run();
    }
}