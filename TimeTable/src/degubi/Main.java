package degubi;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public final class Main extends WindowAdapter implements MouseListener{
	public static final LineBorder blackThinBorder = new LineBorder(Color.BLACK, 1), blackBorder = new LineBorder(Color.BLACK, 2), redBorder = new LineBorder(ClassDataButton.calmRed, 3);
	public static final JFrame frame = new JFrame("TimeTable");
	private static final Image icon = Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/tray.png"));
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final Path dataFilePath = Paths.get("classData.txt");
	public static final JButtonTable<ClassDataButton> dataTable = new JButtonTable<>(150, 96, 30, 30, true, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
	
	public static void main(String[] args) throws AWTException, IOException, LineUnavailableException, UnsupportedAudioFileException {
		frame.setLayout(null);
		frame.add(dataTable);
		frame.setBounds(0, 0, 1024, 768);
		frame.setLocationRelativeTo(null);
		
		if(!Files.exists(dataFilePath)) Files.write(Main.dataFilePath, "Hétfõ Óra Elõadás 08:00 10:00 Terem false".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		
		ClassDataButton.reloadData(Files.readAllLines(dataFilePath));
		
		DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("yyyy MM dd, EEEE HH:mm:ss");
		JLabel label = new JLabel(LocalDateTime.now().format(displayTimeFormat));
		label.setForeground(isDarkMode(LocalTime.now()) ? Color.WHITE : Color.BLACK);
		label.setBounds(360, 5, 300, 40);
		label.setFont(JButtonTable.tableHeaderFont);
		
		Main main = new Main();
		frame.setResizable(false);
		frame.add(label);
		frame.addWindowListener(main);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon);
		
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			if(frame.isVisible()) label.setText(LocalDateTime.now().format(displayTimeFormat));
		}, 0, 1, TimeUnit.SECONDS);

		@SuppressWarnings("resource")
		Clip beepBoop = AudioSystem.getClip();
		
		try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Main.class.getClassLoader().getResource("assets/beep.wav"))){
			beepBoop.open(audioInputStream);
			((FloatControl) beepBoop.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-20);
		}
		
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			if(!frame.isVisible()) {
				LocalTime now = LocalTime.now();
				ClassDataButton current = ClassDataButton.currentClassButton;
	
				if(current != null && now.isBefore(current.startTime)) {
					long timeBetween = ChronoUnit.MINUTES.between(now, current.startTime);
					if(timeBetween < 60) {
						beepBoop.setMicrosecondPosition(0);
						beepBoop.start();
						Main.tray.displayMessage("Órarend", "Figyelem! Következõ óra " + timeBetween + " perc múlva!\nÓra: " + current.className + ' ' + current.startTime + '-' + current.endTime, MessageType.INFO);
					}
				}
				label.setForeground(isDarkMode(now) ? Color.WHITE : Color.BLACK);
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
	
	@Override public void mouseReleased(MouseEvent e) {} @Override public void mouseClicked(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}

	private static final LocalTime afterSeven = LocalTime.of(19, 0), beforeSeven = LocalTime.of(7, 0);
	public static boolean isDarkMode(LocalTime now) {
		return now.isAfter(afterSeven) || now.isBefore(beforeSeven);
	}
}