package degubi;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public final class ClassButton extends JButton implements MouseListener{
	public static ClassButton currentClassButton;
	public static final Font importantFont = new Font("SansSerif", Font.BOLD, 12);

	public final String day;
	public final LocalTime startTime, endTime;
	public final String className, classType, room;
	public final boolean unImportant;
	
	public ClassButton(String line) {
		String[] data = line.split(" ", 7);
		
		//UTF-8 BOM char fix
		day = data[0].charAt(0) == '\uFEFF' ? data[0].substring(1, data[0].length()) : data[0];
		className = data[1];
		classType = data[2];
		startTime = LocalTime.parse(data[3], DateTimeFormatter.ISO_LOCAL_TIME);
		endTime = LocalTime.parse(data[4], DateTimeFormatter.ISO_LOCAL_TIME);
		room = data[5];
		unImportant = Boolean.parseBoolean(data[6]);
		setText("<html>�ra: " + className.replace('_', ' ') + 
				"<br>Id�: " + startTime + "-" + endTime + 
				"<br>T�pus: " + data[2] + 
				"<br>�p�let: " + getBuildingForRoom(data[5]) +
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
		boolean isBetween = isToday && todayTime.isAfter(startTime) && todayTime.isBefore(endTime);
		boolean isAfter = isToday && (todayTime.isAfter(endTime) || todayTime.equals(endTime));
		boolean isCurrent = currentClassButton == null && !unImportant && isBefore || isBetween || (isToday && todayTime.equals(startTime));
		
		if(isCurrent) {
			currentClassButton = this;
			Duration between = Duration.between(todayTime, startTime);
			Main.tray.setToolTip("K�vetkez� �ra " + between.toHoursPart() + " �ra " + between.toMinutesPart() + "perc m�lva: " + className + ' ' + classType + "\nId�pont: " + startTime + '-' + endTime + "\nTerem: " + room);
		}
		setBackground(unImportant ? PropertyFile.unimportantClassColor : isCurrent ? PropertyFile.currentClassColor : isBefore ? PropertyFile.upcomingClassColor : isAfter ? PropertyFile.pastClassColor : PropertyFile.otherClassColor);
	}
	
	private static void rewriteFile() {
		var dataLines = Main.dataTable.tableDataStream().map(ClassButton::toString).collect(Collectors.toList());
		reloadData(dataLines, true);
		
		try {
			Files.write(Paths.get("classData.txt"), dataLines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON3) {
			JDialog frame = new JDialog((JFrame)Main.mainPanel.getTopLevelAncestor());
			JPanel panel = new JPanel(null);
			
			frame.add(new JLayer<>(panel, new BrightnessOverlay()));
			frame.addWindowFocusListener(new Main(frame));
			frame.setUndecorated(true);
			frame.setLocationRelativeTo(null);
			frame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y, 32, 96);
			
			panel.add(newEditButton(32, "T�rl�s", PopupGuis.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(Main.mainPanel, "T�nyleg T�rl�d?", "T�rl�s Meger�s�t�s", JOptionPane.YES_NO_OPTION) == 0) {
					Main.dataTable.tableRemove(this);
					rewriteFile();
				}
			}));
			panel.add(newEditButton(64, unImportant ? "UnIgnor�l�s" : "Ignor�l�s", unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> {
				addOrReplaceButton(false, this, day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + !unImportant);
			}));
			panel.add(newEditButton(0, "Szerkeszt�s", PopupGuis.editIcon, e -> PopupGuis.showEditorGui(false, this)));
			frame.setVisible(true);
		}
	}

	public static void reloadData(List<String> dataLines, boolean showFrame) {
		Main.dataTable.resetTable();
		
		dataLines.stream()
				 .map(ClassButton::new)
				 .sorted(Comparator.comparingInt((ClassButton button) -> Main.dataTable.indexOf(button.day)).thenComparing(button -> button.startTime).thenComparing(button -> button.className))
				 .forEach(button -> Main.dataTable.tableAdd(button.day, button));
		
		ClassButton.updateAllButtons(showFrame);
	}
	
	public static void updateAllButtons(boolean setVisible) {
		currentClassButton = null;
		String today;
		LocalTime now = LocalTime.now();

		switch(LocalDateTime.now().getDayOfWeek().name()) {
			case "MONDAY": today = "H�tf�"; break;
			case "TUESDAY": today = "Kedd"; break;
			case "WEDNESDAY": today = "Szerda"; break;
			case "THURSDAY": today = "Cs�t�rt�k"; break;
			case "FRIDAY": today = "P�ntek"; break;
			default: today = "MenjHaza";
		}
		
		Main.dataTable.forEachData(button -> button.updateButton(today, now));
		if(currentClassButton == null) {
			Main.tray.setToolTip("Nincs m�ra t�bb �ra! :)");
		}
		
		Main.label.setForeground(Main.isDarkMode(now) ? Color.WHITE : Color.BLACK);
		Main.handleNightMode(Main.mainPanel);
		Main.mainPanel.repaint();
		
		if(setVisible) {
			Main.mainPanel.getTopLevelAncestor().setVisible(true);
		}
	}
	
	public static void addOrReplaceButton(boolean add, ClassButton toRemove, String newDataForButton) {
		if(!add) {
			Main.dataTable.tableRemove(toRemove);
		}
		Main.dataTable.tableAdd(new ClassButton(newDataForButton));
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
		map.put("Bolyai", List.of("Ker�kj�rt�", "Farkas", "�rp�d"));
		map.put("K�lvil�g", List.of("Teniszp�lya"));
		return map;
	}
	
	public static final Map<String, List<String>> roomData = createRoomData();
	private static final Set<Entry<String, List<String>>> dataCache = roomData.entrySet();

	private static String getBuildingForRoom(String room) {
		return dataCache.stream()
					    .filter(entry -> entry.getValue().stream().anyMatch(checkRoom -> checkRoom.equals(room)))
					    .map(Entry::getKey)
					    .findFirst()
					    .orElse("Ismeretlen �p�let");
	}
}