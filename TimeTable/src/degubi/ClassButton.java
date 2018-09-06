package degubi;

import java.awt.Color;
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
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

public final class ClassButton extends JButton implements MouseListener{
	public static ClassButton currentClassButton;
	
	public final String day;
	public final LocalTime startTime, endTime;
	public final String className, classType, room;
	public final boolean unImportant;
	
	public ClassButton(String line) {
		String[] data = line.split(" ");
		
		//UTF-8 BOM char fix
		day = data[0].charAt(0) == '\uFEFF' ? data[0].substring(1, data[0].length()) : data[0];
		className = data[1].replace('_', ' ');
		classType = data[2];
		startTime = LocalTime.parse(data[3], DateTimeFormatter.ISO_LOCAL_TIME);
		endTime = LocalTime.parse(data[4], DateTimeFormatter.ISO_LOCAL_TIME);
		room = data[5];
		unImportant = Boolean.parseBoolean(data[6]);
		setText("<html>Óra: " + className + 
				"<br>Idõ: " + startTime + "-" + endTime + 
				"<br>Típus: " + data[2] + 
				"<br>Épület: " + getBuildingForRoom(data[5]) +
				"<br>Terem: " + data[5]);
		
		setForeground(unImportant ? Color.GRAY : Color.BLACK);
		setFocusable(false);
		addMouseListener(this);
		setBorder(unImportant ? null : data[2].contains("ad") ? Main.blackBorder : Main.redBorder);
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
			Main.tray.setToolTip("Következõ óra " + between.toHoursPart() + " óra " + between.toMinutesPart() + "perc múlva: " + className + ' ' + classType + "\nIdõpont: " + startTime + '-' + endTime + "\nTerem: " + room);
		}
		setBackground(unImportant ? unimportantClassColor : isCurrent ? currentClassColor : isBefore ? upcomingClassColor : isAfter ? pastClassColor : otherClassColor);
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
			JDialog editFrame = new JDialog(Main.frame);
			editFrame.addWindowFocusListener(new Main(editFrame));
			editFrame.setLayout(null);
			editFrame.setUndecorated(true);
			editFrame.setLocationRelativeTo(null);
			editFrame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y, 32, 96);
			
			editFrame.add(newEditButton(32, "Törlés", PopupGuis.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(Main.frame, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
					Main.dataTable.tableRemove(this);
					rewriteFile();
				}
			}));
			editFrame.add(newEditButton(64, unImportant ? "UnIgnorálás" : "Ignorálás", unImportant ? PopupGuis.unIgnore : PopupGuis.ignoreIcon, e -> {
				addOrReplaceButton(false, this, day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + !unImportant);
			}));
			editFrame.add(newEditButton(0, "Szerkesztés", PopupGuis.editIcon, e -> PopupGuis.showEditorGui(false, this)));
			editFrame.setVisible(true);
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
		if(setVisible) {
			Main.frame.setVisible(true);
		}
		currentClassButton = null;
		
		String today = toHunDay(LocalDateTime.now().getDayOfWeek().name());
		LocalTime now = LocalTime.now();
		
		Main.dataTable.forEachData(button -> button.updateButton(today, now));
		if(currentClassButton == null) {
			Main.tray.setToolTip("Nincs mára több óra! :)");
		}
		
		Main.label.setForeground(Main.isDarkMode(LocalTime.now()) ? Color.WHITE : Color.BLACK);
		Main.handleNightMode(Main.frame.getContentPane());
		Main.frame.repaint();
	}
	
	private static String toHunDay(String day) {
		switch(day) {
			case "MONDAY": return "Hétfõ";
			case "TUESDAY": return "Kedd";
			case "WEDNESDAY": return "Szerda";
			case "THURSDAY": return "Csütörtök";
			case "FRIDAY": return "Péntek";
			default: return "MenjHaza";
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
		butt.setBorder(new LineBorder(Color.BLACK, 1));
		butt.setBackground(new Color(240, 240, 240));
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
					    .orElse("Ismeretlen Épület");
	}
	
	public static Color currentClassColor = Main.settingsFile.getColor("currentClassColor", 255, 69, 69);
	public static Color upcomingClassColor = Main.settingsFile.getColor("upcomingClassColor", 0, 147, 3);
	public static Color otherClassColor = Main.settingsFile.getColor("otherClassColor", 84, 113, 142);
	public static Color pastClassColor = Main.settingsFile.getColor("pastClassColor", 247, 238, 90);
	public static Color unimportantClassColor = Main.settingsFile.getColor("unimportantClassColor", 192, 192, 192);
}