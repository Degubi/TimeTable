package timetable.listeners;

import java.awt.event.*;
import java.time.*;
import timetable.*;

public final class CreateClassListener extends MouseAdapter {
    private final String dayStr;

    public CreateClassListener(String dayStr) {
        this.dayStr = dayStr;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
            PopupGuis.showEditorForNewClass(new ClassButton(dayStr, "Óra", "Előadás", LocalTime.of(8, 0), LocalTime.of(10, 0), "Terem", false));
        }
    }
}