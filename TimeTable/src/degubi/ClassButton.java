package degubi;

import com.google.gson.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.Map.*;
import javax.swing.*;

public final class ClassButton extends JButton implements MouseListener{
	public static ClassButton currentClassButton;
	public static final Font importantFont = new Font("SansSerif", Font.BOLD, 12);

	private final JsonObject classObject;
	public final String day;
	public final LocalTime startTime, endTime;
	public final String className, classType, room;
	public final boolean unImportant, interactive;
	
	public ClassButton(JsonObject object, ButtonTable table) {
		classObject = object;
		day = object.get("day").getAsString();
		className = object.get("className").getAsString();
		classType = object.get("classType").getAsString();
		startTime = LocalTime.parse(object.get("startTime").getAsString(), DateTimeFormatter.ISO_LOCAL_TIME);
		endTime = LocalTime.parse(object.get("endTime").getAsString(), DateTimeFormatter.ISO_LOCAL_TIME);
		room = object.get("room").getAsString();
		unImportant = object.get("unImportant").getAsBoolean();
		interactive = table == TimeTableMain.dataTable;
		
		setText("<html>Óra: " + className.replace('_', ' ') + 
				"<br>Idõ: " + startTime + "-" + endTime + 
				"<br>Típus: " + classType + 
				"<br>Épület: " + getBuildingForRoom(room) +
				"<br>Terem: " + room);
		
		if(classType.charAt(0) == 'G') {
			setFont(importantFont);
		}
		setForeground(unImportant ? Color.GRAY : Color.BLACK);
		setFocusable(false);
		addMouseListener(this);
	}
	
	private void updateButton(String today, LocalTime todayTime) {
		var isToday = day.equals(today);
		var isBefore = isToday && todayTime.isBefore(startTime);
		var isAfter = isToday && (todayTime.isAfter(startTime) || todayTime.equals(startTime));
		var isNext = currentClassButton == null && !unImportant && isBefore || (isToday && todayTime.equals(startTime));
		
		if(interactive && isNext) {
			currentClassButton = this;
			
			var between = Duration.between(todayTime, startTime);
			TimeTableMain.tray.setToolTip("Következõ óra " + between.toHoursPart() + " óra " + between.toMinutesPart() + " perc múlva: " + className + ' ' + classType + "\nIdõpont: " + startTime + '-' + endTime + "\nTerem: " + room);
		}
		setBackground(unImportant ? Settings.unimportantClassColor : isNext ? Settings.currentClassColor : isBefore ? Settings.upcomingClassColor : isAfter ? Settings.pastClassColor : Settings.otherDayClassColor);
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if(interactive && event.getButton() == MouseEvent.BUTTON3) {
			var frame = new JDialog((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor());
			var panel = new JPanel(null);
			
			frame.setContentPane(panel);
			frame.addWindowFocusListener(new EditClassButtonListener(frame));
			frame.setUndecorated(true);
			frame.setLocationRelativeTo(null);
			frame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y, 32, 96);
			
			panel.add(newEditButton(32, PopupGuis.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(TimeTableMain.mainPanel, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
					TimeTableMain.dataTable.deleteClass(Settings.classes, classObject);
				}
			}));
			panel.add(newEditButton(64, unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> {
				TimeTableMain.dataTable.editClass(Settings.classes, classObject, Settings.classObjectFromData(day, className, classType, startTime.toString(), endTime.toString(), room, !unImportant));
				frame.dispose();
			}));
			panel.add(newEditButton(0, PopupGuis.editIcon, e -> PopupGuis.showEditorGui(classObject, this)));
			frame.setVisible(true);
		}
	}

	public static void updateAllButtons(boolean setVisible, ButtonTable dataTable) {
		if(dataTable == TimeTableMain.dataTable) {
			currentClassButton = null;
		}
		String today;   //TODO Java 12 ezen tud majd szépíteni
		var now = LocalTime.now();
		
		switch(LocalDateTime.now().getDayOfWeek()) {
			case MONDAY: today = "Hétfõ"; break;
			case TUESDAY: today = "Kedd"; break;
			case WEDNESDAY: today = "Szerda"; break;
			case THURSDAY: today = "Csütörtök"; break;
			case FRIDAY: today = "Péntek"; break;
			default: today = "MenjHaza";
		}
		
		dataTable.dataButtonList.forEach(button -> ((ClassButton)button).updateButton(today, now));
		if(currentClassButton == null) {
			TimeTableMain.tray.setToolTip("Nincs mára több óra! :)");
		}
		
		TimeTableMain.handleNightMode(TimeTableMain.dateLabel, now);
		TimeTableMain.handleNightMode(TimeTableMain.mainPanel, now);
		TimeTableMain.mainPanel.repaint();
		
		if(setVisible) {
			TimeTableMain.mainPanel.getTopLevelAncestor().setVisible(true);
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