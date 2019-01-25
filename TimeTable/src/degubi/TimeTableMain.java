package degubi;

import static java.nio.file.StandardOpenOption.*;

import com.google.gson.*;
import java.awt.*;
import java.awt.TrayIcon.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.*;
import java.net.http.HttpResponse.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import javax.imageio.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.plaf.nimbus.*;

public final class TimeTableMain extends WindowAdapter{
	public static final JPanel mainPanel = new JPanel(null);
	public static final Image icon = getIcon("tray.png", 0).getImage();
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final ButtonTable dataTable = new ButtonTable(150, 100, 25, 25, true, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
	public static final JLabel dateLabel = new JLabel();
	public static final JMenuItem screenshotItem = newMenuItem("Órarend Fénykép", "screencap.png", TimeTableMain::createScreenshot);
	
	public static void main(String[] args) throws AWTException, IOException, UnsupportedLookAndFeelException, InterruptedException {
		{	//Update checking
			var client = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).build();
			var buildStr = client.send(HttpRequest.newBuilder(URI.create("https://drive.google.com/uc?authuser=0&id=1qyyC0HAvmIQmIZxaf479rRGXDA7f0ZnT&export=download")).build(), BodyHandlers.ofString()).body();
			
			if(Integer.parseInt(buildStr) > 200) {
				client.send(HttpRequest.newBuilder(URI.create("https://drive.google.com/uc?authuser=0&id=1qYnJ_gsCxu-wfxD-w7QtEzIb7NZhCz0k&export=download")).build(),
							BodyHandlers.ofFileDownload(Path.of("./"), TRUNCATE_EXISTING, WRITE, CREATE));
				
				Runtime.getRuntime().exec("java -jar TimeTableInstaller.jar");
				System.exit(0);
			}
		}
		
		//Main frame & style setup
		UIManager.setLookAndFeel(new NimbusLookAndFeel());
		SwingUtilities.updateComponentTreeUI(dataTable);
		
		var frame = new JFrame("Órarend");
		mainPanel.add(dataTable);
		frame.setBounds(0, 0, 950, 713);
		frame.setLocationRelativeTo(null);
		frame.setContentPane(mainPanel);
		
		dataTable.reloadTable(Settings.classes, !(args.length == 1 && args[0].equals("-window")));
		dateLabel.setBounds(320, 5, 300, 40);
		dateLabel.setFont(ButtonTable.tableHeaderFont);
		frame.setResizable(false);
		mainPanel.add(dateLabel);
		var listeners = new TimeTableMain();
		frame.addWindowListener(listeners);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon);
		
		//System tray menu
		var sleepMode = new JCheckBoxMenuItem("Alvó Mód", getIcon("sleep.png", 24), false);
		var popMenu = new JPopupMenu();

		var friendMenu = new JMenu("Ismerõsök");
		friendMenu.setIcon(getIcon("friends.png", 24));
		reinitFriendsMenu(friendMenu);
		
		popMenu.setPreferredSize(new Dimension(160, 200));
		popMenu.add(newMenuItem("Megnyitás", "open.png", TimeTableMain::trayOpenGui));
		popMenu.addSeparator();
		popMenu.add(friendMenu);
		popMenu.add(sleepMode);
		popMenu.add(screenshotItem);
		popMenu.add(newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
		popMenu.addSeparator();
		popMenu.add(newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));
		SwingUtilities.updateComponentTreeUI(popMenu);
		
		tray.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent event) {
				if(event.getButton() == MouseEvent.BUTTON3) {
					popMenu.setLocation(event.getX() - 160, event.getY());
					popMenu.setInvoker(popMenu);
					popMenu.setVisible(true);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
				if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
					ClassButton.updateAllButtons(true, dataTable);
					((JFrame)mainPanel.getTopLevelAncestor()).setExtendedState(JFrame.NORMAL);
				}
			}
		});
		SystemTray.getSystemTray().add(tray);
		
		//Time label update
		var displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");
		var timer = Settings.updateInterval - 100;
		
		while(true) {
			if(frame.isVisible()) {
				dateLabel.setText(LocalDateTime.now().format(displayTimeFormat));
			}

			if(++timer == Settings.updateInterval) {
				ClassButton.updateAllButtons(false, dataTable);

				if(!sleepMode.isSelected() && !frame.isVisible()) {
					var now = LocalTime.now();
					var current = ClassButton.currentClassButton;

					if(Settings.enablePopups && current != null && now.isBefore(current.startTime)) {
						var timeBetween = Duration.between(now, current.startTime);

						if(timeBetween.toMinutes() < Settings.noteTime) {
							tray.displayMessage("Órarend", "Figyelem! Következõ óra: " + timeBetween.toHoursPart() + " óra " +  timeBetween.toMinutesPart() + " perc múlva!\nÓra: " + current.className + ' ' + current.startTime + '-' + current.endTime, MessageType.NONE);

							try(var stream = AudioSystem.getAudioInputStream(TimeTableMain.class.getClassLoader().getResource("assets/beep.wav"));
								var line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, stream.getFormat(), 8900))){

								line.open();
								((FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-20);
								line.start();
								var buf = new byte[8900];
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

			Thread.sleep(1000);
		}
	}
	
	private static void createScreenshot(@SuppressWarnings("unused") ActionEvent event) {
		var window = mainPanel.getTopLevelAncestor().getLocationOnScreen();
		try {
			ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 50, window.y + 80, 870, 600)), "PNG", new File(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) +".png"));
		} catch (HeadlessException | AWTException | IOException e1) {}
	}
	
	private static void trayOpenGui(@SuppressWarnings("unused") ActionEvent event) {
		ClassButton.updateAllButtons(true, dataTable);
		((JFrame)mainPanel.getTopLevelAncestor()).setExtendedState(JFrame.NORMAL);
	}
	
	
	public static void reinitFriendsMenu(JMenu friendMenu) {
		friendMenu.removeAll();
		var addFriendItem = newMenuItem("Ismerõs Hozzáadása", null, e -> addNewFriend(friendMenu));
		var removeFriendItem = newMenuItem("Ismerõs Eltávolítása", null, e -> removeFriend(friendMenu));
		
		Settings.friends.forEach(friend -> {
			var friendItem = newMenuItem(friend.getAsJsonObject().get("name").getAsString(), null, TimeTableMain::handleFriendTable);
			friendItem.setActionCommand(friend.getAsJsonObject().get("url").getAsString());
			friendMenu.add(friendItem);
		});
		
		if(Settings.friends.size() != 0) {
			friendMenu.addSeparator();
		}
		friendMenu.add(addFriendItem);
		friendMenu.add(removeFriendItem);
	}
	
	private static void addNewFriend(JMenu friendMenu) {
		var friendName = JOptionPane.showInputDialog("Írd be haverod nevét!");
		
		if(friendName != null && !friendName.isEmpty()) {
			var friendURL = JOptionPane.showInputDialog("Írd be haverod URL-jét!");
		
			if(friendURL != null && !friendURL.isEmpty()) {
				var newFriend = Settings.newFriendObject(friendName, friendURL);
				Settings.friends.add(newFriend);
				reinitFriendsMenu(friendMenu);
				Settings.save();
			}
		}
	}
	
	private static void removeFriend(JMenu friendMenu) {
		var friends = Settings.stream(Settings.friends)
							  .map(obj -> obj.get("name").getAsString())
							  .toArray(String[]::new);
		
		if(friends.length == 0) {
			JOptionPane.showMessageDialog(null, "Nincsenek barátaid. :)");
		}else{
			var selection = (String) JOptionPane.showInputDialog(null, "Válaszd ki a \"barátod\"", "Temetés", JOptionPane.OK_CANCEL_OPTION, null, friends, friends[0]);
			
			if(selection != null) {
				Settings.friends.remove(Settings.indexOf(selection, friends));
				Settings.save();
				reinitFriendsMenu(friendMenu);
			}
		}
	}
	
	private static void handleFriendTable(ActionEvent event) {
		var friendTable = new ButtonTable(150, 96, 25, 30, false, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
		
		try(var reader = new URL(event.getActionCommand()).openStream()){
			var data = new JsonParser().parse(new String(reader.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
			
			friendTable.reloadTable(data.get("classes").getAsJsonArray(), false);
			PopupGuis.showNewDialog(false, ((JMenuItem)event.getSource()).getText() + " Órarendje", 930, 700, null, friendTable);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static JMenuItem newMenuItem(String text, String iconPath, ActionListener listener) {
		var item = new JMenuItem(text, iconPath == null ? null : getIcon(iconPath, 24));
		item.addActionListener(listener);
		return item;
	}
	
	public static void handleNightMode(Container container, LocalTime time) {
		var isDarkMode = time.isAfter(Settings.dayTimeEnd) || time.isBefore(Settings.dayTimeStart);
	
		if(container instanceof JLabel || container instanceof JCheckBox) {
			container.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
		}else{
			container.setBackground(isDarkMode ? Settings.nightTimeColor : Settings.dayTimeColor);
		}
	}
	
	public static ImageIcon getIcon(String path, int scale) {
		var image = Toolkit.getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/" + path));
		if(scale == 0 || image.getWidth(null) == scale) {
			return new ImageIcon(image);
		}
		return new ImageIcon(image.getScaledInstance(scale, scale, Image.SCALE_SMOOTH));
	}
	
	@Override
	public void windowDeiconified(WindowEvent e) {
		screenshotItem.setEnabled(true);
		screenshotItem.setToolTipText(null);
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
		mainPanel.getTopLevelAncestor().setVisible(false);
		screenshotItem.setEnabled(false);
		screenshotItem.setToolTipText("Nem lehet fényképet készíteni ha nem látszik az órarend");
	}
}