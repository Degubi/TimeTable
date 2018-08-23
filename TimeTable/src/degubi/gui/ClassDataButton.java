package degubi.gui;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import degubi.Main;

public final class ClassDataButton extends JButton implements MouseListener{
	private static final Comparator<ClassDataButton> sorter = Comparator.<ClassDataButton>comparingInt(button -> DayOfWeek.valueOf(button.day).ordinal()).thenComparing(button -> button.startTime);
	private static final int[] dayIndexers = {40, 40, 40, 40, 40};
	private static boolean nextHourFound = false;
	public static final List<ClassDataButton> classData = new ArrayList<>();
	
	private String fullData;
	
	public final String day;
	public final LocalTime startTime, endTime;
	public final String className, classType, room;
	public final boolean unImportant;
	
	public ClassDataButton(String line) {
		String[] data = line.split(" ");
		
		fullData = line;
		day = data[0];
		className = data[1];
		classType = data[2];
		startTime = LocalTime.parse(data[3], DateTimeFormatter.ISO_LOCAL_TIME);
		endTime = LocalTime.parse(data[4], DateTimeFormatter.ISO_LOCAL_TIME);
		room = data[5];
		unImportant = Boolean.parseBoolean(data[6]);
		setText("<html>Óra: " + className + 
				"<br>Idõ: " + startTime + "-" + endTime + 
				"<br>Típus: " + data[2] + 
				"<br>Épület: " + (room.contains("21") || room.contains("22") ? "Irinyi" : room.contains("ressz") || room.contains("sor") ? "TIK" : "Bolyai") + 
				"<br>Terem: " + data[5]);
		
		setForeground(unImportant ? Color.GRAY : Color.BLACK);
		setFocusable(false);
		addMouseListener(this);
		setBorder(unImportant ? null : data[2].contains("ad") ? Main.blackBorder : Main.redBorder);
	}
	
	public void updateButton(LocalDateTime today, LocalTime todayTime) {
		boolean isToday = day.equals(today.getDayOfWeek().name());
		boolean isBefore = isToday && todayTime.isBefore(startTime);
		boolean isBetween = isToday && todayTime.isAfter(startTime) && todayTime.isBefore(endTime);
		boolean isAfter = isToday && (todayTime.isAfter(endTime) || todayTime.equals(endTime));
		boolean isCurrent = !nextHourFound && isBefore || isBetween || (isToday && todayTime.equals(startTime));
		
		if(isCurrent) {
			nextHourFound = true;
			Main.tray.setToolTip("Következõ óra: " + className + ' ' + classType + "\nIdõpont: " + startTime + '-' + endTime + "\nTerem: " + room);
		}
		setBackground(unImportant ? Color.LIGHT_GRAY : isCurrent ? Color.RED : isBefore ? Color.GREEN : isAfter ? Color.YELLOW : Color.GRAY);
	}
	
	public void refreshDataFromTable(JTable table) {
		String newData = table.getValueAt(0, 1) + " " + table.getValueAt(1, 1) + " " + table.getValueAt(4, 1) + " " + table.getValueAt(2, 1) + " " + table.getValueAt(3, 1) + " " + table.getValueAt(5, 1) + " " + unImportant;
		
		if(isDataValid(newData)) {
			fullData = newData;
			rewriteFile();
		}else{
			JOptionPane.showMessageDialog(null, "Input data fucked up!");
		}
	}
	
	private static void rewriteFile() {
		try {
			Files.write(Main.dataFilePath, ClassDataButton.classData.stream().map(button -> button.fullData).collect(Collectors.toList()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			reloadDataFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON3) {
			JDialog editFrame = new JDialog(Main.frame, "Meow");
			editFrame.addWindowFocusListener(new ButtonFocusListener(editFrame));
			editFrame.setLayout(null);
			editFrame.setUndecorated(true);
			editFrame.setLocationRelativeTo(null);
			editFrame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y - 4, 32, 96);
			
			editFrame.add(newEditButton(32, "Törlés", ButtonEditorGui.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(Main.frame, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
					classData.remove(this);
					Main.frame.remove(this);
					rewriteFile();
				}
			}));
			editFrame.add(newEditButton(64, unImportant ? "UnIgnorálás" : "Ignorálás", unImportant ? ButtonEditorGui.unIgnore : ButtonEditorGui.ignoreIcon, e -> {
				fullData = day + ' ' + className + ' ' + classType + ' ' + startTime + ' ' + endTime + ' ' + room + ' ' + !unImportant;
				rewriteFile();
			}));
			editFrame.add(newEditButton(0, "Szerkesztés", ButtonEditorGui.editIcon, e -> ButtonEditorGui.showEditorGui(this)));
			editFrame.setVisible(true);
		}
	}

	public static void reloadDataFile() throws IOException {
		if(!ClassDataButton.classData.isEmpty()) {
			ClassDataButton.classData.forEach(Main.frame::remove);
			Arrays.fill(ClassDataButton.dayIndexers, 40);
			Main.frame.revalidate();
			Main.frame.repaint();
			ClassDataButton.classData.clear();
		}
		
		if(!Files.exists(Main.dataFilePath)) {
			Files.write(Main.dataFilePath, "MONDAY Dimat Elõadás 18:00 20:00 Kongresszusi false".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		}
		
		try(Stream<String> lines = Files.lines(Main.dataFilePath, StandardCharsets.UTF_8)){
			lines.map(ClassDataButton::new)
				 .sorted(ClassDataButton.sorter)
				 .forEach(button -> {
					 button.setBounds(60 + DayOfWeek.valueOf(button.day).ordinal() * 170, dayIndexers[DayOfWeek.valueOf(button.day).ordinal()] += 95, 150, 90);
					 Main.frame.add(button);
					 classData.add(button);
				 });
			ClassDataButton.updateAllButtons();
		}
	}
	
	public static void updateAllButtons() {
		Main.frame.setVisible(true);
		nextHourFound = false;
		
		LocalDateTime today = LocalDateTime.now();
		LocalTime todayTime = today.toLocalTime();
		
		classData.forEach(button -> button.updateButton(today, todayTime));
		if(!nextHourFound) Main.tray.setToolTip("Nincs mára több óra! :)");
		
		Main.frame.getContentPane().setBackground(todayTime.isAfter(LocalTime.of(18, 00)) ? Color.DARK_GRAY : new Color(240, 240, 240));
	}
	
	public static final class ButtonFocusListener implements WindowFocusListener{
		private final JDialog frame;
		
		public ButtonFocusListener(JDialog frame) {
			this.frame = frame;
		}
		
		@Override public void windowLostFocus(WindowEvent e) { frame.dispose(); }
		@Override public void windowGainedFocus(WindowEvent e) {}
	}
	
	private static JButton newEditButton(int yPos, String tooltip, ImageIcon icon, ActionListener listener) {
		JButton butt = new JButton(icon);
		butt.setToolTipText(tooltip);
		butt.setBounds(0, yPos, 32, 32);
		butt.addActionListener(listener);
		return butt;
	}
	
	private static boolean isDataValid(String line) {
		try {
			String[] data = line.split(" ");
			DayOfWeek.valueOf(data[0]);
			LocalTime.parse(data[3], DateTimeFormatter.ISO_LOCAL_TIME);
			LocalTime.parse(data[4], DateTimeFormatter.ISO_LOCAL_TIME);
			
			return true;
		}catch (Exception e) {
			return false;
		}
	}
	
	@Override public void mouseClicked(MouseEvent e) {} @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
}