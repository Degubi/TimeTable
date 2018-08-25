package degubi;

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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public final class ClassDataButton extends JButton implements MouseListener{
	private static final Comparator<ClassDataButton> sorter = Comparator.<ClassDataButton>comparingInt(button -> DayOfWeek.valueOf(button.day).ordinal()).thenComparing(button -> button.startTime);
	private static final int[] dayIndexers = {40, 40, 40, 40, 40};
	private static boolean nextHourFound = false;
	public static final List<ClassDataButton> classData = new ArrayList<>();
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
	
	private void updateButton(LocalDateTime today, LocalTime todayTime) {
		boolean isToday = day.equals(today.getDayOfWeek().name());
		boolean isBefore = isToday && todayTime.isBefore(startTime);
		boolean isBetween = isToday && todayTime.isAfter(startTime) && todayTime.isBefore(endTime);
		boolean isAfter = isToday && (todayTime.isAfter(endTime) || todayTime.equals(endTime));
		boolean isCurrent = !nextHourFound && !unImportant && isBefore || isBetween || (isToday && todayTime.equals(startTime));
		
		if(isCurrent) {
			nextHourFound = true;
			currentClassButton = this;
			Main.tray.setToolTip("Következõ óra: " + className + ' ' + classType + "\nIdõpont: " + startTime + '-' + endTime + "\nTerem: " + room);
		}
		setBackground(unImportant ? Color.LIGHT_GRAY : isCurrent ? Color.RED : isBefore ? Color.GREEN : isAfter ? Color.YELLOW : Color.GRAY);
	}
	
	private static void rewriteFile() {
		List<String> dataLines = classData.stream().map(ClassDataButton::toString).collect(Collectors.toList());
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
			editFrame.setBounds(getLocationOnScreen().x + 118, getLocationOnScreen().y - 4, 32, 96);
			
			editFrame.add(newEditButton(32, "Törlés", ButtonEditorGui.deleteIcon, e -> {
				if(JOptionPane.showConfirmDialog(Main.frame, "Tényleg Törlöd?", "Törlés Megerõsítés", JOptionPane.YES_NO_OPTION) == 0) {
					classData.remove(this);
					Main.frame.remove(this);
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
		if(!ClassDataButton.classData.isEmpty()) {
			ClassDataButton.classData.forEach(Main.frame::remove);
			Arrays.fill(ClassDataButton.dayIndexers, 40);
			Main.frame.revalidate();
			Main.frame.repaint();
			ClassDataButton.classData.clear();
		}
		
		if(!Files.exists(Main.dataFilePath)) {
			try {
				Files.write(Main.dataFilePath, "MONDAY Dimat Elõadás 18:00 20:00 Kongresszusi false".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			} catch (IOException e) {}
		}
		
		dataLines.stream()
				 .map(ClassDataButton::new)
				 .sorted(sorter)
				 .forEach(button -> {
					 button.setBounds(60 + DayOfWeek.valueOf(button.day).ordinal() * 170, dayIndexers[DayOfWeek.valueOf(button.day).ordinal()] += 95, 150, 90);
					 Main.frame.add(button);
					 classData.add(button);
				 });
		
		ClassDataButton.updateAllButtons(true);
	}
	
	public static void updateAllButtons(boolean setVisible) {
		if(setVisible) {
			Main.frame.setVisible(true);
		}
		nextHourFound = false;
		
		LocalDateTime today = LocalDateTime.now();
		LocalTime todayTime = today.toLocalTime();
		
		classData.forEach(button -> button.updateButton(today, todayTime));
		if(!nextHourFound) {
			Main.tray.setToolTip("Nincs mára több óra! :)");
		}
		
		Main.frame.getContentPane().setBackground(todayTime.isAfter(LocalTime.of(18, 00)) ? Color.DARK_GRAY : new Color(240, 240, 240));
	}
	
	public static void addOrReplaceButton(boolean add, ClassDataButton toRemove, String newDataForButton) {
		if(!add) {
			classData.remove(toRemove);
			Main.frame.remove(toRemove);
		}
		classData.add(new ClassDataButton(newDataForButton));
		rewriteFile();
	}
	
	
	
	public static final class ButtonFocusHandler implements WindowFocusListener{
		private final JDialog frame;
		
		public ButtonFocusHandler(JDialog frame) {
			this.frame = frame;
		}

		@Override
		public void windowGainedFocus(WindowEvent event) {}

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
}