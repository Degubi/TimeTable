package timetable.listeners;

import java.awt.event.*;
import javax.swing.*;
import timetable.*;

@SuppressWarnings("boxing")
public final class ClassEditorTableKeyListener extends AbstractAction{
    private final JTable dataTable;
    private final char key;

    public ClassEditorTableKeyListener(char key, JTable dataTable) {
        this.key = key;
        this.dataTable = dataTable;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        var row = dataTable.getSelectedRow();

        if(row == 1) {
            if(key == 'R') {
                dataTable.setValueAt(getNextOrPrevColumn(true, Main.days, dataTable.getValueAt(1, 1).toString()), 1, 1);
            }else if(key == 'L') {
                dataTable.setValueAt(getNextOrPrevColumn(false, Main.days, dataTable.getValueAt(1, 1).toString()), 1, 1);
            }
        }else if(row == 2) {
            if(key == 'R' || key == 'L') {
                var current = dataTable.getValueAt(2, 1).toString().charAt(0);
                var next = current == 'E' ? "Gyakorlat" : current == 'G' ? "Szabvál" : "Előadás";

                dataTable.setValueAt(next, 2, 1);
            }
        }else if(row == 3 || row == 4) {
            var split = dataTable.getValueAt(row, 1).toString().split(":");

            if(key == 'D') {
                var hours = Integer.parseInt(split[0]);
                dataTable.setValueAt((hours == 0 ? "23" : String.format("%02d", --hours)) + ':' + split[1], row, 1);
            }else if(key == 'L') {
                var minutes = Integer.parseInt(split[1]);
                dataTable.setValueAt(split[0] + ':' + (minutes == 0 ? "45" : String.format("%02d", minutes -= 15)), row, 1);
            }else if(key == 'R') {
                var minutes = Integer.parseInt(split[1]);
                dataTable.setValueAt(split[0] + ':' + (minutes == 45 ? "00" : String.format("%02d", minutes += 15)), row, 1);
            }else if(key == 'U'){
                var hours = Integer.parseInt(split[0]);
                dataTable.setValueAt((hours == 23 ? "00" : String.format("%02d", ++hours)) + ':' + split[1], row, 1);
            }
        }
    }

    private static String getNextOrPrevColumn(boolean isNext, String[] columns, String day) {
        int currentIndex = Settings.indexOf(day, columns);
        return isNext ? columns[currentIndex == columns.length - 1 ? 0 : ++currentIndex] : columns[currentIndex == 0 ? columns.length - 1 : --currentIndex];
    }
}