package timetable.listeners;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import timetable.*;

public final class RoomSelectionListener extends MouseAdapter{
    private final JTable dataTable;
    
    public RoomSelectionListener(JTable dataTable) {
        this.dataTable = dataTable;
    }
    
    @Override
    public void mousePressed(MouseEvent event) {
        if(event.getClickCount() == 2 && dataTable.getSelectedColumn() == 1 && dataTable.getSelectedRow() == 5) {
            var topPanel = new JPanel(new GridBagLayout());
            var cons = new GridBagConstraints(0, -1, 1, 1, 1D, 0D, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null, 0, 0);
            var buttonDimension = new Dimension(120, 40);
            var topInsent = new Insets(10, 5, 20, 5);
            var bottomInsent = new Insets(0, 5, 5, 5);
            var roomButtons = new ArrayList<JButton>(ClassButton.roomData.values().size());
            var currentRoom = dataTable.getValueAt(5, 1).toString();

            ClassButton.roomData.forEach((building, rooms) -> {
                cons.insets = topInsent;
                topPanel.add(Components.newButton(building, Color.BLACK, Color.GRAY, buttonDimension), cons);
                cons.insets = bottomInsent;
                
                Arrays.stream(rooms).forEach(room -> {
                    var isCurrentRoom = currentRoom != null && room.equals(currentRoom);
                    var roomButton = Components.newButton(room, isCurrentRoom ? Color.BLACK : Color.GRAY, isCurrentRoom ? Color.RED : Color.LIGHT_GRAY, buttonDimension);
                    
                    roomButton.addActionListener(e -> handleRoomButtonPress(roomButtons, roomButton));
                    roomButtons.add(roomButton);
                    topPanel.add(roomButton, cons);
                });
                ++cons.gridx;
            });
            
            var bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            var saveButton = Components.newButton("Save", Color.BLACK, Color.GRAY, new Dimension(120, 40));
            saveButton.addActionListener(e -> roomButtons.stream()
                                                         .filter(button -> button.getBackground() == Color.RED)
                                                         .findFirst()
                                                         .ifPresent(button -> {
                                                             dataTable.setValueAt(button.getText(), 5, 1);
                                                             ((JDialog)topPanel.getTopLevelAncestor()).dispose();
                                                         }));
            bottomPanel.add(saveButton);

            var frame = new JDialog((JFrame)Main.mainPanel.getTopLevelAncestor(), "Teremválasztó", true);
            frame.setIconImage(Components.trayIcon);
            frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            frame.setBounds(0, 0, 700, 600);
            frame.setLocationRelativeTo(Main.mainPanel);
            frame.setMinimumSize(new Dimension(550, 500));
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(bottomPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
        }
    }

    private static void handleRoomButtonPress(ArrayList<JButton> roomButtons, JButton currentButton) {
        roomButtons.forEach(roomButts -> {
            roomButts.setForeground(Color.GRAY);
            roomButts.setBackground(Color.LIGHT_GRAY);
        });
        
        currentButton.setForeground(Color.BLACK);
        currentButton.setBackground(Color.RED);
    }
}