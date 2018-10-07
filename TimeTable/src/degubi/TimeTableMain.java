package degubi;

import static java.awt.Toolkit.getDefaultToolkit;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import degubi.gui.BrightablePanel;
import degubi.gui.ButtonTable;
import degubi.gui.ClassButton;
import degubi.gui.PopupGuis;
import degubi.listeners.MainFrameIconifier;
import degubi.listeners.SystemTrayListener;
import degubi.tools.NIO;
import degubi.tools.PropertyFile;

public final class TimeTableMain extends WindowAdapter{
	public static final int BUILD_NUMBER = 101;
	public static int INSTALLER_BUILD;
	public static final BrightablePanel mainPanel = new BrightablePanel();
	public static final Image icon = getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/tray.png"));
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final ButtonTable dataTable = new ButtonTable(150, 96, 25, 30, true, "H�tf�", "Kedd", "Szerda", "Cs�t�rt�k", "P�ntek");
	public static final JLabel dateLabel = new JLabel();
	private static int timer = PropertyFile.updateInterval - 100;
	private static final JCheckBoxMenuItem sleepMode = new JCheckBoxMenuItem("Alv� M�d", new ImageIcon(getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/sleep.png")).getScaledInstance(24, 24, Image.SCALE_SMOOTH)), false);
	public static final JMenuItem screenshotItem = newMenuItem("�rarend F�nyk�p", "screencap.png", TimeTableMain::createScreenshot);
	
	public static void main(String[] args) throws AWTException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		if(args.length == 0 || !args[0].equals("-noupdate")) checkForUpdates(args);
		
		UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		SwingUtilities.updateComponentTreeUI(dataTable);
			
		JFrame frame = new JFrame("�rarend");
		mainPanel.add(dataTable);
		frame.setBounds(0, 0, 950, 713);
		frame.setLocationRelativeTo(null);
		frame.setContentPane(mainPanel);
		
		Path dataFilePath = Paths.get("classData.txt");
		NIO.checkFileOr(dataFilePath, NIO::showExcelFileBrowser);
		ClassButton.reloadData(Files.readAllLines(dataFilePath), dataTable, !(args.length == 1 && args[0].equals("-window")));
			
		dateLabel.setBounds(320, 5, 300, 40);
		dateLabel.setFont(ButtonTable.tableHeaderFont);
		frame.setResizable(false);
		mainPanel.add(dateLabel);
		frame.addWindowListener(new MainFrameIconifier());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon);
			
		launchTimerThread(frame);
			
		tray.addMouseListener(new SystemTrayListener(initTrayMenu(mainPanel)));
		SystemTray.getSystemTray().add(tray);
	}

	private static void checkForUpdates(String[] args) throws IOException, MalformedURLException {
		int[] buildNumbers = new int[2];  //0: Main Jar Version, 1: Installer Jar Version
		boolean updateUpdater = false;

		try(var urlInput = new URL("https://pastebin.com/raw/BMxNE6ws").openStream()){
			byte[] data = new byte[7];
			urlInput.read(data);
			
			buildNumbers[0] = (data[0] - 48) * 100 + (data[1] - 48) * 10 + (data[2] - 48);
			buildNumbers[1] = (data[4] - 48) * 100 + (data[5] - 48) * 10 + (data[6] - 48);
		} catch (IOException e) {}
		
		try(var jarFile = new JarFile("TimeTableInstaller.jar")){
			var entry = jarFile.getEntry("version.txt");
			
			if(entry != null) {
				try(var entryInput = jarFile.getInputStream(entry)){
					byte[] buildData = new byte[3];
					entryInput.read(buildData);
					
					int localBuild = (buildData[0] - 48) * 100 + (buildData[1] - 48) * 10 + (buildData[2] - 48);
					INSTALLER_BUILD = localBuild;
					
					if(buildNumbers[1] > localBuild) {
						updateUpdater = true;
					}
				}
			}else{
				updateUpdater = true;
			}
		}
		
		if(updateUpdater) {
			try(var urlChannel = Channels.newChannel(new URL("https://drive.google.com/uc?authuser=0&id=1qYnJ_gsCxu-wfxD-w7QtEzIb7NZhCz0k&export=download").openStream()); 
				var fileChannel = FileChannel.open(Paths.get("TimeTableInstaller.jar"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE)){
					
				fileChannel.transferFrom(urlChannel, 0, Integer.MAX_VALUE);
			}
		}
		
		if(buildNumbers[0] > BUILD_NUMBER) {
			Runtime.getRuntime().exec("java -jar TimeTableInstaller.jar" + (args.length == 1 ? " " + args[0] : ""));
			System.exit(0);
		}
	}

	@SuppressWarnings("boxing")
	private static JPopupMenu initTrayMenu(BrightablePanel overlay) {
		JSlider brightnessSlider = new JSlider(0, 16, 16);
		brightnessSlider.addChangeListener(overlay);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setMaximumSize(new Dimension(155, 40));
		
		JMenu friendMenu = new JMenu("Ismer�s�k");
		friendMenu.setIcon(new ImageIcon(getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/friends.png")).getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
		reinitFriendsMenu(friendMenu);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>(2);
		labelTable.put(0, new JLabel("S�t�t"));
		labelTable.put(16, new JLabel("Vil�gos"));
		brightnessSlider.setLabelTable(labelTable);
		
		JPopupMenu popMenu = new JPopupMenu();
		popMenu.setPreferredSize(new Dimension(160, 240));
		popMenu.add(newMenuItem("Megnyit�s", "open.png", TimeTableMain::trayOpenGui));
		popMenu.addSeparator();
		popMenu.add(friendMenu);
		popMenu.add(sleepMode);
		popMenu.add(screenshotItem);
		popMenu.add(newMenuItem("Be�ll�t�sok", "settings.png", PopupGuis::showSettingsGui));
		popMenu.addSeparator();
		popMenu.add(brightnessSlider);
		popMenu.addSeparator();
		popMenu.add(newMenuItem("Bez�r�s", "exit.png", e -> System.exit(0)));
		
		SwingUtilities.updateComponentTreeUI(popMenu);
		return popMenu;
	}
	
	private static void reinitFriendsMenu(JMenu friendMenu) {
		friendMenu.removeAll();
		JMenuItem addFriendItem = new JMenuItem("Ismer�s Hozz�ad�sa");
		addFriendItem.addActionListener(e -> addNewFriend(friendMenu));
		
		PropertyFile.friendsMap.forEach((name, url) -> {
			JMenuItem friendItem = new JMenuItem(name);
			friendItem.setActionCommand(url);
			friendItem.addActionListener(TimeTableMain::handleFriendTable);
			friendMenu.add(friendItem);
		});
		
		if(!PropertyFile.friendsMap.isEmpty()) {
			friendMenu.addSeparator();
		}
		friendMenu.add(addFriendItem);
	}
	
	private static void handleFriendTable(ActionEvent event) {
		var friendTable = new ButtonTable(150, 96, 25, 30, false, "H�tf�", "Kedd", "Szerda", "Cs�t�rt�k", "P�ntek");
		var data = new byte[1000];
		
		int readCount = 0;
		try(var reader = new URL(event.getActionCommand()).openStream()){
			readCount = reader.read(data, 0, data.length);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ClassButton.reloadData(List.of(new String(data, 0, readCount, StandardCharsets.UTF_8).split("\n")), friendTable, false);
		PopupGuis.showNewDialog(false, ((JMenuItem)event.getSource()).getText() + " �rarendje", 930, 700, null, friendTable);
	}
	
	private static void addNewFriend(JMenu friendMenu) {
		String friendName = JOptionPane.showInputDialog("�rd be haverod nev�t!");
		
		if(friendName != null && !friendName.isEmpty()) {
			String friendURL = JOptionPane.showInputDialog("�rd be haverod URL-j�t!");
		
			if(friendURL != null && !friendURL.isEmpty()) {
				PropertyFile.friendsMap.put(friendName, friendURL);
				reinitFriendsMenu(friendMenu);
				PropertyFile.setMap("friends", PropertyFile.friendsMap);
			}
		}
	}
	
	private static JMenuItem newMenuItem(String text, String iconPath, ActionListener listener) {
		JMenuItem item = new JMenuItem(text, iconPath != null ? new ImageIcon(getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/" + iconPath)).getScaledInstance(24, 24, Image.SCALE_SMOOTH)) : null);
		item.setForeground(Color.BLACK);
		item.addActionListener(listener);
		return item;
	}
	
	private static void createScreenshot(@SuppressWarnings("unused") ActionEvent event) {
		var window = mainPanel.getTopLevelAncestor().getLocationOnScreen();
		try {
			ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 50, window.y + 80, 870, 600)), "PNG", new File(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) +".png"));
		} catch (HeadlessException | AWTException | IOException e1) {}
	}
	
	private static void launchTimerThread(JFrame frame) {
		DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			if(frame.isVisible()) {
				dateLabel.setText(LocalDateTime.now().format(displayTimeFormat));
			}
			
			if(++timer == PropertyFile.updateInterval) {
				ClassButton.updateAllButtons(false, TimeTableMain.dataTable);

				if(!sleepMode.isSelected() && !frame.isVisible()) {
					LocalTime now = LocalTime.now();
					ClassButton current = ClassButton.currentClassButton;
		
					if(PropertyFile.enablePopups && current != null && now.isBefore(current.startTime)) {
						var timeBetween = Duration.between(now, current.startTime);
						
						if(timeBetween.toMinutes() < PropertyFile.noteTime) {
							TimeTableMain.tray.displayMessage("�rarend", "Figyelem! K�vetkez� �ra: " + timeBetween.toHoursPart() + " �ra " +  timeBetween.toMinutesPart() + " perc m�lva!\n�ra: " + current.className + ' ' + current.startTime + '-' + current.endTime, MessageType.NONE);

							try(var stream = AudioSystem.getAudioInputStream(TimeTableMain.class.getClassLoader().getResource("assets/beep.wav"));
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
					handleNightMode(dateLabel, now);
				}
				timer = 0;
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
	
	private static void trayOpenGui(@SuppressWarnings("unused") ActionEvent event) {
		ClassButton.updateAllButtons(true, TimeTableMain.dataTable);
		((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor()).setExtendedState(JFrame.NORMAL);
	}
	
	public static void handleNightMode(Container container, LocalTime time) {
		boolean isDarkMode = time.isAfter(PropertyFile.dayTimeEnd) || time.isBefore(PropertyFile.dayTimeStart);
	
		if(container instanceof JLabel || container instanceof JCheckBox) {
			container.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
		}else{
			container.setBackground(isDarkMode ? PropertyFile.nightTimeColor : PropertyFile.dayTimeColor);
		}
	}
}