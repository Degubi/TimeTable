package degubi.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.gson.JsonObject;

import degubi.TimeTableMain;
import degubi.listeners.EditClassButtonListener;
import degubi.tools.GuiTools;
import degubi.tools.Settings;

public final class ClassButton extends JButton implements GuiTools{
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
			var panel = new BrightablePanel();
			
			frame.setContentPane(panel);
			frame.addWindowFocusListener(new EditClassButtonListener(frame));
			frame.setUndecorated(true);
			frame.setLocationRelativeTo(null);
			frame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y, 32, 96);
			
			panel.add(GuiTools.newEditButton(32, PopupGuis.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(TimeTableMain.mainPanel, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
					TimeTableMain.dataTable.deleteClass(Settings.classes, classObject);
				}
			}));
			panel.add(GuiTools.newEditButton(64, unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> {
				TimeTableMain.dataTable.editClass(Settings.classes, classObject, Settings.newClassObject(day, className, classType, startTime.toString(), endTime.toString(), room, !unImportant));
				frame.dispose();
			}));
			panel.add(GuiTools.newEditButton(0, PopupGuis.editIcon, e -> PopupGuis.showEditorGui(classObject, this)));
			frame.setVisible(true);
		}
	}

	public static void updateAllButtons(boolean setVisible, ButtonTable dataTable) {
		if(dataTable == TimeTableMain.dataTable) {
			currentClassButton = null;
		}
		String today;
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
		
		GuiTools.handleNightMode(TimeTableMain.dateLabel, now);
		GuiTools.handleNightMode(TimeTableMain.mainPanel, now);
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
	
	
	private static Map<String, List<String>> createRoomData(){
		var map = new LinkedHashMap<String, List<String>>(4);
		map.put("TIK", List.of("Kongresszusi", "Alagsor"));
		map.put("Irinyi", List.of("214", "217", "218", "222", "224", "225"));
		map.put("Bolyai", List.of("Kerékjártó", "Farkas", "Árpád"));
		map.put("Külvilág", List.of("Teniszpálya"));
		return map;
	}
	
	public static final Map<String, List<String>> roomData = createRoomData();
	private static final Set<Entry<String, List<String>>> dataCache = roomData.entrySet();

	private static String getBuildingForRoom(String room) {
		return dataCache.stream()
					    .filter(entry -> entry.getValue().stream().anyMatch(checkRoom -> checkRoom.equals(room)))
					    .map(Entry::getKey)
					    .findFirst()
					    .orElse("Ismeretlen");
	}
}