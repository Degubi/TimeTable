package degubi;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public final class Main extends WindowAdapter implements MouseListener{
	public static final LineBorder blackThinBorder = new LineBorder(Color.BLACK, 1), blackBorder = new LineBorder(Color.BLACK, 2), redBorder = new LineBorder(Color.RED, 3);
	public static final JFrame frame = new JFrame("TimeTable");
	public static final Image icon = Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/tray.png"));
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final Path dataFilePath = Paths.get("classData.txt");
	public static final Font bigFont = new Font("TimesRoman", Font.PLAIN, 20);
	private static final Clip beepBoop = getBeepSound();
	
	public static void main(String[] args) throws AWTException, IOException {
		frame.setLayout(null);
		frame.add(newDayButton("Hétfõ", 60, bigFont));
		frame.add(newDayButton("Kedd", 230, bigFont));
		frame.add(newDayButton("Szerda", 400, bigFont));
		frame.add(newDayButton("Csütörtök", 570, bigFont));
		frame.add(newDayButton("Péntek", 740, bigFont));
		frame.setBounds(0, 0, 1024, 768);
		frame.setLocationRelativeTo(null);
		
		if(!Files.exists(dataFilePath)) Files.write(Main.dataFilePath, "MONDAY Dimat Elõadás 18:00 20:00 Kongresszusi false".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		
		ClassDataButton.reloadData(Files.readAllLines(dataFilePath));
		
		DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("yyyy MM dd, EEEE HH:mm:ss");
		JLabel label = new JLabel(LocalDateTime.now().format(displayTimeFormat));
		label.setForeground(Color.BLACK);
		label.setBounds(400, 10, 300, 40);
		label.setFont(bigFont);
		
		Main main = new Main();
		frame.add(newButton("Új Óra Hozzáadása", 840, 650, 150, 60, e -> ButtonEditorGui.showEditorGui(true, new ClassDataButton("MONDAY ÓRANÉV Elõadás 08:00 10:00 Terem false"))));
		frame.setResizable(false);
		frame.add(label);
		frame.addWindowListener(main);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon);
		
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			if(frame.isVisible()) {
				label.setText(LocalDateTime.now().format(displayTimeFormat));
			}
		}, 0, 1, TimeUnit.SECONDS);

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			if(!frame.isVisible()) {
				LocalTime now = LocalTime.now();
				ClassDataButton current = ClassDataButton.currentClassButton;
	
				if(current != null && now.isBefore(current.startTime)) {
					long timeBetween = ChronoUnit.MINUTES.between(LocalTime.now(), current.startTime);
					if(timeBetween < 60) {
						beepBoop.setMicrosecondPosition(0);
						beepBoop.start();
						Main.tray.displayMessage("Órarend", "Figyelem! Következõ óra " + timeBetween + " perc múlva!\nÓra: " + current.className + ' ' + current.startTime + '-' + current.endTime, MessageType.INFO);
					}
				}
				ClassDataButton.updateAllButtons(false);
			}
		}, 10, 10, TimeUnit.MINUTES);
		
		SystemTray.getSystemTray().add(tray);
		PopupMenu popMenu = new PopupMenu();
		MenuItem exitItem = new MenuItem("Bezárás");
		exitItem.addActionListener(e -> System.exit(0));
		MenuItem openItem = new MenuItem("Megnyitás");
		openItem.addActionListener(e -> ClassDataButton.updateAllButtons(true));
		popMenu.add(openItem);
		popMenu.add(exitItem);
		
		tray.addMouseListener(main);
		tray.setPopupMenu(popMenu);
	}
	
	private static Clip getBeepSound() {
		try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Main.class.getClassLoader().getResource("assets/beep.wav"))){
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-20);
			
			return clip;
		} catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
			return null;
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
			frame.setExtendedState(JFrame.NORMAL);
			frame.setVisible(true);
			ClassDataButton.updateAllButtons(true);
		}
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
		frame.setVisible(false);
	}
	
	public static JButton newButton(String text, int x, int y, int width, int height, ActionListener listener) {
		JButton toReturn = new JButton(text);
		toReturn.setFocusable(false);
		toReturn.setBounds(x, y, width, height);
		toReturn.setBorder(blackBorder);
		toReturn.setBackground(Color.GRAY);
		toReturn.setForeground(Color.BLACK);
		toReturn.addActionListener(listener);
		return toReturn;
	}
	
	public static JButton newDayButton(String dayName, int xCoord, Font bigFont) {
		JButton dayButton = new JButton(dayName);
		dayButton.setBounds(xCoord, 60, 150, 60);
		dayButton.setFont(bigFont);
		dayButton.setForeground(Color.BLACK);
		dayButton.setFocusable(false);
		dayButton.setBackground(Color.LIGHT_GRAY);
		dayButton.setBorder(blackBorder);
		return dayButton;
	}
	
	@Override public void mouseReleased(MouseEvent e) {} @Override public void mouseClicked(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
}