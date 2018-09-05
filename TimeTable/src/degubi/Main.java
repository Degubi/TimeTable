package degubi;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public final class Main extends WindowAdapter implements MouseListener{
	public static final LineBorder blackBorder = new LineBorder(Color.BLACK, 3), redBorder = new LineBorder(Color.RED, 3);
	public static final JFrame frame = new JFrame("TimeTable");
	private static final Image icon = Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("assets/tray.png"));
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final ButtonTable<ClassButton> dataTable = new ButtonTable<>(150, 96, 25, 30, true, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
	public static final PropertyFile settingsFile = new PropertyFile("settings.prop");
	public static final JLabel label = new JLabel();
	
	private final JDialog passFrame;
	
	public Main(JDialog frame) {
		passFrame = frame;
	}
	
	public static void main(String[] args) throws AWTException, IOException {
		if(args.length > 0) {
			frame.setLayout(null);
			frame.add(dataTable);
			frame.setBounds(0, 0, 950, 713);
			frame.setLocationRelativeTo(null);
			
			Path dataFilePath = Paths.get("classData.txt");
			if(!Files.exists(dataFilePath)) Files.write(dataFilePath, "Hétfõ Óra Elõadás 08:00 10:00 Terem false".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			
			ClassButton.reloadData(Files.readAllLines(dataFilePath), args[0].contains("full"));
			
			DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");
			CheckboxMenuItem sleepMode = new CheckboxMenuItem("Alvó Mód", false);
			Main main = new Main(null);
			label.setBounds(310, 5, 300, 40);
			label.setFont(ButtonTable.tableHeaderFont);
			frame.setResizable(false);
			frame.add(label);
			frame.addWindowListener(main);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setIconImage(icon);
			
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
				if(frame.isVisible()) {
					label.setText(LocalDateTime.now().format(displayTimeFormat));
				}
				
				if(++timer == 600) {
					ClassButton.updateAllButtons(false);

					if(!frame.isVisible()) {
						LocalTime now = LocalTime.now();
						ClassButton current = ClassButton.currentClassButton;
			
						if(enablePopups && !sleepMode.getState() && current != null && now.isBefore(current.startTime)) {
							var timeBetween = Duration.between(now, current.startTime);
							
							if(timeBetween.toHoursPart() == 0) {
								Main.tray.displayMessage("Órarend", "Figyelem! Következõ óra: " + timeBetween.toMinutesPart() + " perc múlva!\nÓra: " + current.className + ' ' + current.startTime + '-' + current.endTime, MessageType.NONE);

								try(AudioInputStream stream = AudioSystem.getAudioInputStream(Main.class.getClassLoader().getResource("assets/beep.wav"));
									SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, stream.getFormat(), 8900))){
									
									line.open();
									((FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-20);
									line.start();
									byte[] buf = new byte[8900];
									stream.read(buf);
									line.write(buf, 0, 8900);
									line.drain();
								}catch(IOException | UnsupportedAudioFileException | LineUnavailableException e1) {}
							}
						}
						label.setForeground(isDarkMode(now) ? Color.WHITE : Color.BLACK);
					}
					timer = 0;
				}
			}, 0, 1, TimeUnit.SECONDS);
			
			PopupMenu popMenu = new PopupMenu();
			popMenu.add(newMenuItem("Megnyitás", Main::trayOpenGui));
			popMenu.addSeparator();
			popMenu.add(newMenuItem("Beállítások", PopupGuis::showSettingsGui));
			popMenu.add(newMenuItem("Órarend Fénykép", Main::createScreenshot));
			popMenu.add(sleepMode);
			popMenu.addSeparator();
			popMenu.add(newMenuItem("Bezárás", e -> System.exit(0)));
			
			SystemTray.getSystemTray().add(tray);
			tray.addMouseListener(main);
			tray.setPopupMenu(popMenu);
		}else{
			throw new RuntimeException("Can't find startup flag.! (full or window)");
		}
	}
	
	private static int timer = 0;
	
	@Override
	public void windowIconified(WindowEvent e) {
		if(passFrame == null) frame.setVisible(false);
	}
	
	@Override
	public void windowLostFocus(WindowEvent event) {
		if(passFrame != null) passFrame.dispose();
	}
	
	public static LocalTime dayTimeStart = LocalTime.parse(settingsFile.get("dayTimeStart", "07:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static LocalTime dayTimeEnd = LocalTime.parse(settingsFile.get("dayTimeEnd", "19:00"), DateTimeFormatter.ISO_LOCAL_TIME);
	public static Color dayTimeColor = settingsFile.getColor("dayTimeColor", 240, 240, 240);
	public static Color nightTimeColor = settingsFile.getColor("nightTimeColor", 64, 64, 64);
	public static boolean enablePopups = settingsFile.getBoolean("enablePopups", true);

	private static MenuItem newMenuItem(String text, ActionListener listener) {
		MenuItem item = new MenuItem(text);
		item.addActionListener(listener);
		return item;
	}
	
	private static void createScreenshot(@SuppressWarnings("unused") ActionEvent event) {
		if(frame.isVisible()) {
			var window = frame.getLocationOnScreen();
			try {
				ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 50, window.y + 80, 870, 600)), "PNG", new File(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) +".png"));
			} catch (HeadlessException | AWTException | IOException e1) {}
		}
	}
	
	private static void trayOpenGui(@SuppressWarnings("unused") ActionEvent event) {
		frame.setExtendedState(JFrame.NORMAL);
		ClassButton.updateAllButtons(true);
	}
	
	public static boolean isDarkMode(LocalTime now) {
		return now.isAfter(dayTimeEnd) || now.isBefore(dayTimeStart);
	}
	
	public static void handleNightMode(Container container) {
		container.setBackground(isDarkMode(LocalTime.now()) ? nightTimeColor : dayTimeColor);
	}


	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
			ClassButton.updateAllButtons(true);
			frame.setExtendedState(JFrame.NORMAL);
		}
	}
	
	@Override public void mouseClicked(MouseEvent e) {} @Override public void mouseReleased(MouseEvent e) {} @Override public void mouseEntered(MouseEvent e) {} @Override public void mouseExited(MouseEvent e) {}
}