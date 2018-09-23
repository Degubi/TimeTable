package degubi;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
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
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

import com.github.lgooddatepicker.components.CalendarPanel;

import degubi.listeners.CalendarListeners;
import degubi.listeners.FriendButtonListener;
import degubi.listeners.MainFrameIconifier;
import degubi.listeners.SystemTrayListener;

public final class Main extends WindowAdapter{
	public static final String VERSION = "1.0.0";
	public static final LineBorder blackBorder = new LineBorder(Color.BLACK, 2, true);
	public static final JPanel mainPanel = new JPanel(null);
	public static final Image icon = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/assets/tray.png"));
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final ButtonTable<ClassButton> dataTable = new ButtonTable<>(150, 96, 25, 30, true, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
	public static final JLabel dateLabel = new JLabel();
	private static int timer = PropertyFile.updateInterval - 100;

	public static void main(String[] args) throws AWTException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		if(args.length > 0) {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			SwingUtilities.updateComponentTreeUI(dataTable);
			
			JFrame frame = new JFrame("Órarend");
			mainPanel.add(dataTable);
			frame.setBounds(0, 0, 950, 713);
			frame.setLocationRelativeTo(null);
			BrightnessOverlay overlay = new BrightnessOverlay();
			
			frame.add(new JLayer<>(mainPanel, overlay));
			Path dataFilePath = Paths.get("classData.txt");
			if(!Files.exists(dataFilePath)) {
				ExcelParser.showExcelFileBrowser(dataFilePath);
			}
			
			ClassButton.reloadData(Files.readAllLines(dataFilePath), dataTable, args[0].equals("-full"));
			
			dateLabel.setBounds(320, 5, 300, 40);
			dateLabel.setFont(ButtonTable.tableHeaderFont);
			frame.setResizable(false);
			mainPanel.add(dateLabel);
			frame.addWindowListener(new MainFrameIconifier());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setIconImage(icon);
			
			launchUpdaterThread(frame);
			
			tray.addMouseListener(new SystemTrayListener(initTrayMenu(overlay)));
			SystemTray.getSystemTray().add(tray);
		}else{
			JOptionPane.showMessageDialog(null, "Nincs indítási flag! ('-full' vagy '-windows')");
		}
	}

	@SuppressWarnings("boxing")
	private static JPopupMenu initTrayMenu(BrightnessOverlay overlay) {
		JSlider brightnessSlider = new JSlider(0, 16, 16);
		brightnessSlider.addChangeListener(overlay);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setMaximumSize(new Dimension(155, 40));
		
		JMenu calendarMenu = new JMenu("Naptár");
		calendarMenu.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/assets/calendar.png")).getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
		
		CalendarListeners listeners = new CalendarListeners();
		CalendarPanel calendar = new CalendarPanel(listeners, listeners);
		calendarMenu.add(calendar);
		
		JMenu friendMenu = new JMenu("Ismerõsök");
		friendMenu.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/assets/friends.png")).getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
		reinitFriendsMenu(friendMenu);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>(2);
		labelTable.put(0, new JLabel("Sötét"));
		labelTable.put(16, new JLabel("Világos"));
		brightnessSlider.setLabelTable(labelTable);
		
		JPopupMenu popMenu = new JPopupMenu();
		popMenu.setPreferredSize(new Dimension(160, 240));
		popMenu.add(newMenuItem("Megnyitás", "open.png", Main::trayOpenGui));
		popMenu.addSeparator();
		popMenu.add(friendMenu);
		popMenu.add(newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
		popMenu.add(calendarMenu);
		popMenu.add(newMenuItem("Órarend Fénykép", "screencap.png", Main::createScreenshot));
		popMenu.addSeparator();
		popMenu.add(brightnessSlider);
		popMenu.addSeparator();
		popMenu.add(newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));
		return popMenu;
	}
	
	private static void reinitFriendsMenu(JMenu friendMenu) {
		friendMenu.removeAll();
		JMenuItem addFriendItem = new JMenuItem("Ismerõs Hozzáadása");
		addFriendItem.addActionListener(e -> addNewFriend(friendMenu));
		
		PropertyFile.friendsMap.forEach((name, url) -> {
			JMenuItem friendItem = new JMenuItem(name);
			friendItem.setActionCommand(url);
			friendItem.addActionListener(new FriendButtonListener());
			friendMenu.add(friendItem);
		});
		
		if(!PropertyFile.friendsMap.isEmpty()) {
			friendMenu.addSeparator();
		}
		friendMenu.add(addFriendItem);
	}
	
	private static void addNewFriend(JMenu friendMenu) {
		String friendName = JOptionPane.showInputDialog("Írd be haverod nevét!");
		String friendURL = JOptionPane.showInputDialog("Írd be haverod URL-jét!");
		
		PropertyFile.friendsMap.put(friendName, friendURL);
		
		reinitFriendsMenu(friendMenu);
		
		PropertyFile.setMap("friends", PropertyFile.friendsMap);
	}
	
	private static JMenuItem newMenuItem(String text, String iconPath, ActionListener listener) {
		JMenuItem item = new JMenuItem(text, iconPath != null ? new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/assets/" + iconPath)).getScaledInstance(24, 24, Image.SCALE_SMOOTH)) : null);
		item.addActionListener(listener);
		return item;
	}
	
	private static void createScreenshot(@SuppressWarnings("unused") ActionEvent event) {
		if(mainPanel.getTopLevelAncestor().isVisible()) {
			var window = mainPanel.getTopLevelAncestor().getLocationOnScreen();
			try {
				ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 50, window.y + 80, 870, 600)), "PNG", new File(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) +".png"));
			} catch (HeadlessException | AWTException | IOException e1) {}
		}
	}
	
	private static void launchUpdaterThread(JFrame frame) {
		DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			if(frame.isVisible()) {
				dateLabel.setText(LocalDateTime.now().format(displayTimeFormat));
			}
			
			if(++timer == PropertyFile.updateInterval) {
				ClassButton.updateAllButtons(false, Main.dataTable);

				if(!frame.isVisible()) {
					LocalTime now = LocalTime.now();
					ClassButton current = ClassButton.currentClassButton;
		
					if(PropertyFile.enablePopups && current != null && now.isBefore(current.startTime)) {
						var timeBetween = Duration.between(now, current.startTime);
						
						if(timeBetween.toMinutes() < PropertyFile.noteTime) {
							Main.tray.displayMessage("Órarend", "Figyelem! Következõ óra: " + timeBetween.toHoursPart() + " óra " +  timeBetween.toMinutesPart() + " perc múlva!\nÓra: " + current.className + ' ' + current.startTime + '-' + current.endTime, MessageType.NONE);

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
					dateLabel.setForeground(isDarkMode(now) ? Color.WHITE : Color.BLACK);
				}
				timer = 0;
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
	
	private static void trayOpenGui(@SuppressWarnings("unused") ActionEvent event) {
		ClassButton.updateAllButtons(true, Main.dataTable);
		((JFrame)Main.mainPanel.getTopLevelAncestor()).setExtendedState(JFrame.NORMAL);
	}
	
	public static boolean isDarkMode(LocalTime now) {
		return now.isAfter(PropertyFile.dayTimeEnd) || now.isBefore(PropertyFile.dayTimeStart);
	}
	
	public static void handleNightMode(Container container) {
		container.setBackground(isDarkMode(LocalTime.now()) ? PropertyFile.nightTimeColor : PropertyFile.dayTimeColor);
	}
}