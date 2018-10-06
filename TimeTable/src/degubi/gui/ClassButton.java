package degubi.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import degubi.TimeTableMain;
import degubi.listeners.EditClassButtonListener;
import degubi.tools.NIO;
import degubi.tools.PropertyFile;

public final class ClassButton extends JButton implements MouseListener{
	public static ClassButton currentClassButton;
	public static final Font importantFont = new Font("SansSerif", Font.BOLD, 12);

	public final String day;
	public final LocalTime startTime, endTime;
	public final String className, classType, room;
	public final boolean unImportant, interactive;
	
	public ClassButton(String line, ButtonTable<ClassButton> table) {
		String[] data = line.split(" ", 7);
		
		//UTF-8 BOM char fix
		day = data[0].charAt(0) == '\uFEFF' ? data[0].substring(1, data[0].length()) : data[0];
		className = data[1];
		classType = data[2];
		startTime = LocalTime.parse(data[3], DateTimeFormatter.ISO_LOCAL_TIME);
		endTime = LocalTime.parse(data[4], DateTimeFormatter.ISO_LOCAL_TIME);
		room = data[5];
		unImportant = Boolean.parseBoolean(data[6].trim());
		interactive = table == TimeTableMain.dataTable;
		setText("<html>Óra: " + className.replace('_', ' ') + 
				"<br>Idõ: " + startTime + "-" + endTime + 
				"<br>Típus: " + data[2] + 
				"<br>Épület: " + getBuildingForRoom(data[5]) +
				"<br>Terem: " + data[5]);
		
		if(data[2].charAt(0) == 'G') {
			setFont(importantFont);
		}
		setForeground(unImportant ? Color.GRAY : Color.BLACK);
		setFocusable(false);
		addMouseListener(this);
	}
	
	private void updateButton(String today, LocalTime todayTime) {
		boolean isToday = day.equals(today);
		boolean isBefore = isToday && todayTime.isBefore(startTime);
		boolean isAfter = isToday && (todayTime.isAfter(startTime) || todayTime.equals(startTime));
		boolean isNext = currentClassButton == null && !unImportant && isBefore || (isToday && todayTime.equals(startTime));
		
		if(interactive && isNext) {
			currentClassButton = this;
			
			Duration between = Duration.between(todayTime, startTime);
			TimeTableMain.tray.setToolTip("Következõ óra " + between.toHoursPart() + " óra " + between.toMinutesPart() + " perc múlva: " + className + ' ' + classType + "\nIdõpont: " + startTime + '-' + endTime + "\nTerem: " + room);
		}
		setBackground(unImportant ? PropertyFile.unimportantClassColor : isNext ? PropertyFile.currentClassColor : isBefore ? PropertyFile.upcomingClassColor : isAfter ? PropertyFile.pastClassColor : PropertyFile.otherClassColor);
	}
	
	private static void rewriteFile() {
		var dataLines = TimeTableMain.dataTable.tableDataStream().map(ClassButton::toString).collect(Collectors.toList());
		reloadData(dataLines, TimeTableMain.dataTable, true);
		
		NIO.writeAllLines("classData.txt", dataLines);
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if(interactive && event.getButton() == MouseEvent.BUTTON3) {
			JDialog frame = new JDialog((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor());
			BrightablePanel panel = new BrightablePanel();
			
			frame.setContentPane(panel);
			frame.addWindowFocusListener(new EditClassButtonListener(frame));
			frame.setUndecorated(true);
			frame.setLocationRelativeTo(null);
			frame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y, 32, 96);
			
			panel.add(newEditButton(32, "Törlés", PopupGuis.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(TimeTableMain.mainPanel, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
					TimeTableMain.dataTable.tableRemove(this);
					rewriteFile();
				}
			}));
			panel.add(newEditButton(64, unImportant ? "UnIgnorálás" : "Ignorálás", unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> {
				addOrReplaceButton(false, this, day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + !unImportant);
			}));
			panel.add(newEditButton(0, "Szerkesztés", PopupGuis.editIcon, e -> PopupGuis.showEditorGui(false, this)));
			frame.setVisible(true);
		}
	}

	public static void reloadData(List<String> dataLines, ButtonTable<ClassButton> dataTable, boolean showFrame) {
		dataTable.resetTable();
		
		dataLines.stream()
				 .map(line -> new ClassButton(line, dataTable))
				 .sorted(Comparator.comparingInt((ClassButton button) -> dataTable.indexOf(button.day)).thenComparing(button -> button.startTime).thenComparing(button -> button.className))
				 .forEach(button -> dataTable.tableAdd(button.day, button));
		
		ClassButton.updateAllButtons(showFrame, dataTable);
	}
	
	public static void updateAllButtons(boolean setVisible, ButtonTable<ClassButton> dataTable) {
		if(dataTable == TimeTableMain.dataTable) {
			currentClassButton = null;
		}
		String today;
		LocalTime now = LocalTime.now();

		switch(LocalDateTime.now().getDayOfWeek().name()) {
			case "MONDAY": today = "Hétfõ"; break;
			case "TUESDAY": today = "Kedd"; break;
			case "WEDNESDAY": today = "Szerda"; break;
			case "THURSDAY": today = "Csütörtök"; break;
			case "FRIDAY": today = "Péntek"; break;
			default: today = "MenjHaza";
		}
		
		dataTable.forEachData(button -> button.updateButton(today, now));
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
	
	public static void addOrReplaceButton(boolean add, ClassButton toRemove, String newDataForButton) {
		if(!add) {
			TimeTableMain.dataTable.tableRemove(toRemove);
		}
		TimeTableMain.dataTable.tableAddInternal(new ClassButton(newDataForButton, TimeTableMain.dataTable));
		rewriteFile();
	}
	
	@Override public void mouseClicked(MouseEvent e) {} @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
	
	private static JButton newEditButton(int yPos, String tooltip, ImageIcon icon, ActionListener listener) {
		JButton butt = new JButton(icon);
		butt.setToolTipText(tooltip);
		butt.setFocusable(false);
		butt.setBackground(Color.LIGHT_GRAY);
		butt.setBounds(0, yPos, 32, 32);
		butt.addActionListener(listener);
		return butt;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof ClassButton) {
			ClassButton button = (ClassButton) obj;
			return className.equals(button.className) && day.equals(button.day) && classType.equals(button.classType) && startTime.equals(button.startTime) && endTime.equals(button.endTime) && room.equals(button.room);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + unImportant;
	}
	
	
	private static Map<String, List<String>> createRoomData(){
		LinkedHashMap<String, List<String>> map = new LinkedHashMap<>(4);
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