package degubi;

import degubi.gui.*;
import degubi.listeners.*;
import degubi.tools.*;
import java.awt.*;
import java.awt.TrayIcon.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.*;
import java.net.http.HttpResponse.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.plaf.nimbus.*;

public final class TimeTableMain extends WindowAdapter{
	public static final BrightablePanel mainPanel = new BrightablePanel();
	public static final Image icon = GuiTools.getIcon("tray.png", 0).getImage();
	public static final TrayIcon tray = new TrayIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	public static final ButtonTable dataTable = new ButtonTable(150, 100, 25, 25, true, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
	public static final JLabel dateLabel = new JLabel();
	private static int timer = Settings.updateInterval - 100;
	private static final int BUILD_NUMBER = 200;
	
	public static void main(String[] args) throws AWTException, IOException, UnsupportedLookAndFeelException, InterruptedException {
		{	//Update checking
			var client = HttpClient.newBuilder().followRedirects(Redirect.NORMAL).build();
			var buildStr = client.send(HttpRequest.newBuilder(URI.create("https://drive.google.com/uc?authuser=0&id=1qyyC0HAvmIQmIZxaf479rRGXDA7f0ZnT&export=download")).build(), BodyHandlers.ofString()).body();
			
			if(Integer.parseInt(buildStr) > BUILD_NUMBER) {
				client.send(HttpRequest.newBuilder(URI.create("https://drive.google.com/uc?authuser=0&id=1qYnJ_gsCxu-wfxD-w7QtEzIb7NZhCz0k&export=download")).build(),
							BodyHandlers.ofFileDownload(Path.of("./"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE));
				
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
		frame.addWindowListener(new MainFrameIconifier());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(icon);
			
		tray.addMouseListener(new SystemTrayListener());
		SystemTray.getSystemTray().add(tray);
		
		//Time label update
		var displayTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEEE HH:mm:ss");
		
		while(true) {
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

			Thread.sleep(1000);
		}
	}
}