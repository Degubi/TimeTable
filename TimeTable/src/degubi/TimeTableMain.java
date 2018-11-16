package degubi;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import degubi.gui.BrightablePanel;
import degubi.gui.ButtonTable;
import degubi.gui.ClassButton;
import degubi.listeners.MainFrameIconifier;
import degubi.listeners.SystemTrayListener;
import degubi.tools.GuiTools;
import degubi.tools.Settings;

public final class TimeTableMain extends WindowAdapter{
	public static final BrightablePanel mainPanel = new BrightablePanel();
	public static final Image icon = GuiTools.getIcon("tray.png", 0).getImage();
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final ButtonTable dataTable = new ButtonTable(150, 100, 25, 25, true, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
	public static final JLabel dateLabel = new JLabel();
	private static int timer = Settings.updateInterval - 100;
	private static final int BUILD_NUMBER = 106;
	
	public static void main(String[] args) throws AWTException, IOException, UnsupportedLookAndFeelException {
		checkForUpdates();
		
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
		frame.addWindowListener(new MainFrameIconifier());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon);
			
		launchTimerThread(frame);
		
		tray.addMouseListener(new SystemTrayListener());
		SystemTray.getSystemTray().add(tray);
	}
	
	private static void checkForUpdates() throws IOException, MalformedURLException {
		try(var urlInput = new URL("https://pastebin.com/raw/NZfLFzYB").openStream()){
			var data = new byte[3];
			urlInput.read(data);
			
			if(Integer.parseInt(new String(data)) > BUILD_NUMBER) {
				try(var urlChannel = Channels.newChannel(new URL("https://drive.google.com/uc?authuser=0&id=1qYnJ_gsCxu-wfxD-w7QtEzIb7NZhCz0k&export=download").openStream()); 
					var fileChannel = FileChannel.open(Path.of("TimeTableInstaller.jar"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE)){
								
					fileChannel.transferFrom(urlChannel, 0, Integer.MAX_VALUE);
				}
					
				Runtime.getRuntime().exec("java -jar TimeTableInstaller.jar");
				System.exit(0);
			}
		}
	}
	
	private static void launchTimerThread(JFrame frame) {	
		DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			if(frame.isVisible()) {
				dateLabel.setText(LocalDateTime.now().format(displayTimeFormat));
			}
			
			if(++timer == Settings.updateInterval) {
				ClassButton.updateAllButtons(false, TimeTableMain.dataTable);

				if(!SystemTrayListener.sleepMode.isSelected() && !frame.isVisible()) {
					var now = LocalTime.now();
					var current = ClassButton.currentClassButton;
		
					if(Settings.enablePopups && current != null && now.isBefore(current.startTime)) {
						var timeBetween = Duration.between(now, current.startTime);
						
						if(timeBetween.toMinutes() < Settings.noteTime) {
							TimeTableMain.tray.displayMessage("Órarend", "Figyelem! Következõ óra: " + timeBetween.toHoursPart() + " óra " +  timeBetween.toMinutesPart() + " perc múlva!\nÓra: " + current.className + ' ' + current.startTime + '-' + current.endTime, MessageType.NONE);

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
					GuiTools.handleNightMode(dateLabel, now);
				}
				timer = 0;
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
}