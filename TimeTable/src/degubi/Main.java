package degubi;

import java.awt.*;
import java.awt.TrayIcon.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;
import javax.imageio.*;
import javax.json.bind.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.plaf.nimbus.*;

public final class Main extends WindowAdapter{
	private static final ArrayList<ClassButton> classButtons = new ArrayList<>();
	public static ClassButton currentClassButton;

	public static final String[] days = {"Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek"};
	public static final JLabel dateLabel = new JLabel();
	public static final JPanel mainPanel = new JPanel(null);
	public static final Image icon = getIcon("tray.png", 0).getImage();
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final Font tableHeaderFont = new Font("SansSerif", Font.PLAIN, 20);
	public static final JMenuItem screenshotItem = newMenuItem("Órarend Fénykép", "screencap.png", Main::createScreenshot);
	public static final Jsonb json = JsonbBuilder.create(new JsonbConfig().withFormatting(Boolean.TRUE));
	
	public static void main(String[] args) throws AWTException, UnsupportedLookAndFeelException, InterruptedException {
		/*{	//Update checking
			var client = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).build();
			var buildStr = client.send(HttpRequest.newBuilder(URI.create("https://drive.google.com/uc?authuser=0&id=1qyyC0HAvmIQmIZxaf479rRGXDA7f0ZnT&export=download")).build(), BodyHandlers.ofString()).body();
			
			if(Integer.parseInt(buildStr) > 200) {
				client.send(HttpRequest.newBuilder(URI.create("https://drive.google.com/uc?authuser=0&id=1qYnJ_gsCxu-wfxD-w7QtEzIb7NZhCz0k&export=download")).build(),
							BodyHandlers.ofFileDownload(Path.of("./"), TRUNCATE_EXISTING, WRITE, CREATE));
				
				Runtime.getRuntime().exec("java -jar TimeTableInstaller.jar");
				System.exit(0);
			}
		}
		*/
		UIManager.setLookAndFeel(new NimbusLookAndFeel());
		
		var frame = new JFrame("Órarend");
		frame.setBounds(0, 0, 950, 713);
		frame.setLocationRelativeTo(null);
		frame.setContentPane(mainPanel);
		
		IntStream.range(0, 5)
				 .forEach(i -> {
					 var currentDay = days[i];
					 var topAdd = new JButton(currentDay);
					 
					 topAdd.setFocusable(false);
					 topAdd.addMouseListener(new CreateClassListener(currentDay));
					 topAdd.setBackground(Color.GRAY);
					 topAdd.setForeground(Color.BLACK);
					 topAdd.setFont(tableHeaderFont);
					 topAdd.setBounds(20 + (i * 180), 80, 150, 40);
					 mainPanel.add(topAdd);
				  });
		
		updateClasses();
		dateLabel.setBounds(325, 5, 300, 40);
		dateLabel.setFont(tableHeaderFont);
		mainPanel.add(dateLabel);
		
		var listeners = new Main();
		frame.addWindowListener(listeners);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon);
		frame.setResizable(false);
		frame.setVisible(true);
		
		//System tray menu
		var sleepMode = new JCheckBoxMenuItem("Alvó Mód", getIcon("sleep.png", 24), false);
		var popMenu = new JPopupMenu();

		popMenu.setPreferredSize(new Dimension(160, 200));
		popMenu.add(newMenuItem("Megnyitás", "open.png", Main::trayOpenGui));
		popMenu.addSeparator();
		popMenu.add(sleepMode);
		popMenu.add(screenshotItem);
		popMenu.add(newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
		popMenu.addSeparator();
		popMenu.add(newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));
		SwingUtilities.updateComponentTreeUI(popMenu);
		
		tray.addMouseListener(new SystemTrayListener(popMenu));
		SystemTray.getSystemTray().add(tray);
		
		Runtime.getRuntime().addShutdownHook(new Thread(Settings::saveSettings));
		
		
		//Time label update
		var displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");
		var timer = Settings.updateInterval - 100;
		
		while(true) {
			if(frame.isVisible()) {
				dateLabel.setText(LocalDateTime.now().format(displayTimeFormat));
			}

			if(++timer == Settings.updateInterval) {
				if(!sleepMode.isSelected() && !frame.isVisible()) {
					var now = LocalTime.now();
					var current = ClassButton.currentClassButton;

					if(Settings.enablePopups && current != null && now.isBefore(current.startTime)) {
						var timeBetween = Duration.between(now, current.startTime);

						if(timeBetween.toMinutes() < Settings.timeBeforeNotification) {
							tray.displayMessage("Órarend", "Figyelem! Következõ óra: " + timeBetween.toHoursPart() + " óra " +  timeBetween.toMinutesPart() + " perc múlva!\nÓra: " + current.className + ' ' + current.startTime + '-' + current.endTime, MessageType.NONE);

							try(var stream = AudioSystem.getAudioInputStream(Main.class.getClassLoader().getResource("assets/beep.wav"));
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
		var fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) + ".png";
		
		try {
			ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 20, window.y + 90, 890, 600)), "PNG", new File(fileName));
			JOptionPane.showMessageDialog(mainPanel.getTopLevelAncestor(), "TimeTable saved as: " + fileName);
		} catch (HeadlessException | AWTException | IOException e1) {}
	}
	
	private static void trayOpenGui(@SuppressWarnings("unused") ActionEvent event) {
		updateClasses();
		var top = (JFrame) mainPanel.getTopLevelAncestor();
		top.setVisible(true);
		top.setExtendedState(JFrame.NORMAL);
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
	
	public static void updateClasses() {
		classButtons.forEach(mainPanel::remove);
		classButtons.clear();
		currentClassButton = null;
		
		String today;
		var now = LocalTime.now();
		
		handleNightMode(mainPanel, now);
		handleNightMode(dateLabel, now);
		
		switch(LocalDateTime.now().getDayOfWeek()) {
			case MONDAY: today = "Hétfõ"; break;
			case TUESDAY: today = "Kedd"; break;
			case WEDNESDAY: today = "Szerda"; break;
			case THURSDAY: today = "Csütörtök"; break;
			case FRIDAY: today = "Péntek"; break;
			default: today = "MenjHaza";
		}

		Settings.classes
				.forEach((day, rawClasses) -> {
					var yPosition = new int[] {20};
					int xPosition;

					switch(day) {   //TODO: Java14-be remélhetõleg ez szebb lehet végre
						case "Hétfõ": xPosition = 20; break;
						case "Kedd": xPosition = 200; break;
						case "Szerda": xPosition = 380; break;
						case "Csütörtök": xPosition = 560; break;
						case "Péntek": xPosition = 740; break;
						default: throw new IllegalStateException("HUH?");
					}
					
					rawClasses.stream()
							  .sorted(Comparator.comparingInt((ClassButton button) -> Settings.indexOf(button.day, days))
											    .thenComparing(button -> button.startTime)
											    .thenComparing(button -> button.className))
							  .forEach(clazz -> {
								  clazz.setBounds(xPosition, yPosition[0] += 110, 150, 100);
								  
								  var isToday = day.equalsIgnoreCase(today);
								  var isBefore = isToday && now.isBefore(clazz.startTime);
								  var isAfter = isToday && (now.isAfter(clazz.startTime) || now.equals(clazz.startTime));
								  var isNext = currentClassButton == null && !clazz.unImportant && isBefore || (isToday && now.equals(clazz.startTime));
									
								  if(isNext) {
									  currentClassButton = clazz;
										
									  var between = Duration.between(now, clazz.startTime);
									  Main.tray.setToolTip("Következõ óra " + between.toHoursPart() + " óra " + between.toMinutesPart() + " perc múlva: " + clazz.className + ' ' + clazz.classType + "\nIdõpont: " + clazz.startTime + '-' + clazz.endTime + "\nTerem: " + clazz.room);
								  }
								  clazz.setBackground(clazz.unImportant ? Settings.unimportantClassColor : isNext ? Settings.currentClassColor : isBefore ? Settings.upcomingClassColor : isAfter ? Settings.pastClassColor : Settings.otherDayClassColor);
								  clazz.setForeground(clazz.unImportant ? Color.LIGHT_GRAY : Color.BLACK);
								  
								  mainPanel.add(clazz);
								  classButtons.add(clazz);
							  });
					});
		
		mainPanel.repaint();
	}
	
	public static ImageIcon getIcon(String path, int scale) {
		var image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/assets/" + path));
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