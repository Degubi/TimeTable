package degubi;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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

public final class ClassDataButton extends JButton implements MouseListener{
	private static final Comparator<ClassDataButton> sorter = Comparator.<ClassDataButton>comparingInt(button -> Main.dataTable.indexOf(button.day)).thenComparing(button -> button.startTime);
	private static boolean nextHourFound = false;
	public static ClassDataButton currentClassButton;
	
	public final String day;
	public final LocalTime startTime, endTime;
	public final String className, classType, room;
	public final boolean unImportant;
	
	public ClassDataButton(String line) {
		String[] data = line.split(" ");
		
		day = data[0];
		className = data[1];
		classType = data[2];
		startTime = LocalTime.parse(data[3], DateTimeFormatter.ISO_LOCAL_TIME);
		endTime = LocalTime.parse(data[4], DateTimeFormatter.ISO_LOCAL_TIME);
		room = data[5];
		unImportant = Boolean.parseBoolean(data[6]);
		setText("<html>Óra: " + data[1] + 
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
		boolean isCurrent = !nextHourFound && !unImportant && isBefore || isBetween || (isToday && todayTime.equals(startTime));
		
		if(isCurrent) {
			nextHourFound = true;
			currentClassButton = this;
			Main.tray.setToolTip("Következõ óra: " + className + ' ' + classType + "\nIdõpont: " + startTime + '-' + endTime + "\nTerem: " + room);
		}
		setBackground(unImportant ? Color.LIGHT_GRAY : isCurrent ? calmRed : isBefore ? calmGreen : isAfter ? calmYellow : calmBlue);
	}
	
	private static void rewriteFile() {
		List<String> dataLines = Main.dataTable.tableDataStream().map(ClassDataButton::toString).collect(Collectors.toList());
		reloadData(dataLines);
		
		try {
			Files.write(Main.dataFilePath, dataLines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON3) {
			JDialog editFrame = new JDialog(Main.frame);
			editFrame.addWindowFocusListener(new ButtonFocusHandler(editFrame));
			editFrame.setLayout(null);
			editFrame.setUndecorated(true);
			editFrame.setLocationRelativeTo(null);
			editFrame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y, 32, 96);
			
			editFrame.add(newEditButton(32, "Törlés", ButtonEditorGui.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(Main.frame, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
					Main.dataTable.tableRemove(this);
					rewriteFile();
				}
			}));
			editFrame.add(newEditButton(64, unImportant ? "UnIgnorálás" : "Ignorálás", unImportant ? ButtonEditorGui.unIgnore : ButtonEditorGui.ignoreIcon, e -> {
				addOrReplaceButton(false, this, day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + !unImportant);
			}));
			editFrame.add(newEditButton(0, "Szerkesztés", ButtonEditorGui.editIcon, e -> ButtonEditorGui.showEditorGui(false, this)));
			editFrame.setVisible(true);
		}
	}

	public static void reloadData(List<String> dataLines) {
		Main.dataTable.resetTable();
		
		dataLines.stream()
				 .map(ClassDataButton::new)
				 .sorted(sorter)
				 .forEach(button -> Main.dataTable.tableAdd(button.day, button));
		
		ClassDataButton.updateAllButtons(true);
	}
	
	public static void updateAllButtons(boolean setVisible) {
		if(setVisible) {
			Main.frame.setVisible(true);
		}
		nextHourFound = false;
		
		String today = toHunDay(LocalDateTime.now().getDayOfWeek().name());
		LocalTime now = LocalTime.now();
		
		Main.dataTable.forEachData(button -> button.updateButton(today, now));
		if(!nextHourFound) {
			Main.tray.setToolTip("Nincs mára több óra! :)");
			currentClassButton = null;
		}
		
		Main.frame.getContentPane().setBackground(Main.isDarkMode(now) ? Color.DARK_GRAY : new Color(240, 240, 240));
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
	
	public static void addOrReplaceButton(boolean add, ClassDataButton toRemove, String newDataForButton) {
		if(!add) {
			Main.dataTable.tableRemove(toRemove);
		}
		Main.dataTable.tableAdd(new ClassDataButton(newDataForButton));
		rewriteFile();
	}
	
	
	
	public static final class ButtonFocusHandler extends WindowAdapter{
		private final JDialog frame;
		
		public ButtonFocusHandler(JDialog frame) {
			this.frame = frame;
		}

		@Override
		public void windowLostFocus(WindowEvent event) {
			frame.dispose();
		}
	}
	
	@Override public void mouseClicked(MouseEvent e) {} @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
	
	private static JButton newEditButton(int yPos, String tooltip, ImageIcon icon, ActionListener listener) {
		JButton butt = new JButton(icon);
		butt.setToolTipText(tooltip);
		butt.setBorder(Main.blackThinBorder);
		butt.setBackground(new Color(240, 240, 240));
		butt.setBounds(0, yPos, 32, 32);
		butt.addActionListener(listener);
		return butt;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ClassDataButton) {
			ClassDataButton button = (ClassDataButton) obj;
			return className.equals(button.className) && day.equals(button.day) && classType.equals(button.classType) && startTime.equals(button.startTime) && endTime.equals(button.endTime) && room.equals(button.room);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + unImportant;
	}
	
	
	private static Map<String, List<String>> createRoomData(){
		LinkedHashMap<String, List<String>> map = new LinkedHashMap<>(3);
		map.put("TIK", List.of("Kongresszusi", "Alagsor1"));
		map.put("Irinyi", List.of("217", "218", "222", "225"));
		map.put("Bolyai", List.of("Kerkékjártó", "Farkas"));
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
	
	public static final Color calmRed = new Color(255, 69, 69);
	private static final Color calmGreen = new Color(0, 147, 3);
	private static final Color calmBlue = new Color(84, 113, 142);
	private static final Color calmYellow = new Color(247, 238, 90);
}