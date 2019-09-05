package timetable;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.Map.*;
import javax.json.*;
import javax.swing.*;

public final class ClassButton extends JButton implements MouseListener{
	public static ClassButton currentClassButton;
	public static final Font importantFont = new Font("SansSerif", Font.BOLD, 12);

	public final String day;
	public final LocalTime startTime, endTime;
	public final String className, classType, room;
	public boolean unImportant;
	
	public ClassButton(String day, String className, String classType, LocalTime startTime, LocalTime endTime, String room, boolean unImportant) {
		this.day = day;
		this.className = className;
		this.classType = classType;
		this.startTime = startTime;
		this.endTime = endTime;
		this.room = room;
		this.unImportant = unImportant;
		
		setText("<html>Óra: " + className.replace('_', ' ') + 
				"<br>Idõ: " + startTime + "-" + endTime + 
				"<br>Típus: " + classType + 
				"<br>Épület: " + getBuildingForRoom(room) +
				"<br>Terem: " + room);
		
		if(classType.charAt(0) == 'G') {
			setFont(importantFont);
		}
		setFocusable(false);
		addMouseListener(this);
	}
	
	public ClassButton(JsonObject object) {
		this(object.getString("day"), object.getString("className"), object.getString("classType"), 
				LocalTime.parse(object.getString("startTime"), DateTimeFormatter.ISO_LOCAL_TIME),
				LocalTime.parse(object.getString("endTime"), DateTimeFormatter.ISO_LOCAL_TIME),
				object.getString("room"), object.getBoolean("unImportant"));
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON3) {
			var frame = new JDialog((JFrame)Main.mainPanel.getTopLevelAncestor());
			var panel = new JPanel(null);
			
			frame.setContentPane(panel);
			frame.addWindowFocusListener(new EditClassButtonListener(frame));
			frame.setUndecorated(true);
			frame.setLocationRelativeTo(null);
			frame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y, 32, 96);
			
			panel.add(newEditButton(32, PopupGuis.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(Main.mainPanel, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
					Settings.classes.get(day)
						    .removeIf(this::equals);
					
					Main.updateClasses();
				}
			}));
			panel.add(newEditButton(64, unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> {
				Settings.classes.get(day).stream()
						.filter(this::equals)
						.findFirst()
						.ifPresent(butt -> {
							butt.unImportant = !butt.unImportant;
							Main.updateClasses();
							frame.dispose();
						});
			}));
			panel.add(newEditButton(0, PopupGuis.editIcon, e -> PopupGuis.showEditorGui(day, false, this)));
			
			frame.setVisible(true);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof ClassButton) {
			var button = (ClassButton) obj;
			return className.equals(button.className) && day.equals(button.day) && classType.equals(button.classType) && startTime.equals(button.startTime) && endTime.equals(button.endTime) && room.equals(button.room);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + unImportant;
	}
	
	public static final Map<String, String[]> roomData = Map.of(
					"TIK", new String[] {"Kongresszusi", "Alagsor"},
					"Irinyi", new String[] {"214", "217", "218", "219", "222", "224", "225"},
					"Bolyai", new String[] {"Kerékjártó", "Farkas", "Kiss Árpád"},
					"Külvilág", new String[] {"Teniszpálya"});

	private static String getBuildingForRoom(String room) {
		return roomData.entrySet().stream()
					   .filter(entry -> Arrays.stream(entry.getValue()).anyMatch(checkRoom -> checkRoom.equals(room)))
					   .map(Entry::getKey)
					   .findFirst()
					   .orElse("Ismeretlen");
	}
	
	private static JButton newEditButton(int yPos, ImageIcon icon, ActionListener listener) {
		var butt = new JButton(icon);
		butt.setBackground(Color.LIGHT_GRAY);
		butt.setBounds(0, yPos, 32, 32);
		butt.addActionListener(listener);
		return butt;
	}

	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	
	private static final class EditClassButtonListener extends WindowAdapter{
		private final JDialog passFrame;
		
		public EditClassButtonListener(JDialog frame) {
			passFrame = frame;
		}
		
		@Override
		public void windowLostFocus(WindowEvent event) {
			passFrame.dispose();
		}
	}
}