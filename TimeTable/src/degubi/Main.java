package degubi;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import degubi.gui.ButtonEditorGui;
import degubi.gui.ClassDataButton;

public final class Main extends WindowAdapter implements MouseListener{
	public static final LineBorder blackBorder = new LineBorder(Color.BLACK, 2), redBorder = new LineBorder(Color.RED, 3);
	public static final JFrame frame = new JFrame("TimeTable");
	public static final TrayIcon tray = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("icons/tray.png")));
	public static final Path dataFilePath = Paths.get("classData.txt");
	
	public static long trayClickTimer;
	
	public static void main(String[] args) throws AWTException, IOException {
		DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("yyyy MM dd, EEEE HH:mm:ss");
		Font bigFont = new Font("TimesRoman", Font.PLAIN, 20);
		JLabel label = new JLabel(LocalDateTime.now().format(displayTimeFormat));
		label.setForeground(Color.BLACK);
		label.setBounds(400, 10, 300, 40);
		label.setFont(bigFont);
		
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			if(frame.isVisible()) label.setText(LocalDateTime.now().format(displayTimeFormat));
		}, 0, 1, TimeUnit.SECONDS);

		Main main = new Main();
		frame.addWindowListener(main);
		tray.addMouseListener(main);
		SystemTray.getSystemTray().add(tray);
		
		JButton addClassButton = new JButton("Új Óra Hozzáadása");
		addClassButton.setFocusable(false);
		addClassButton.setBounds(840, 650, 150, 60);
		addClassButton.setBorder(blackBorder);
		addClassButton.setBackground(Color.GRAY);
		addClassButton.setForeground(Color.BLACK);
		addClassButton.addActionListener(e -> ButtonEditorGui.openNewButtonGui());
		
		frame.add(addClassButton);
		frame.add(newDayButton("Hétfõ", 60, bigFont));
		frame.add(newDayButton("Kedd", 230, bigFont));
		frame.add(newDayButton("Szerda", 400, bigFont));
		frame.add(newDayButton("Csütörtök", 570, bigFont));
		frame.add(newDayButton("Péntek", 740, bigFont));
		frame.add(label);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(0, 0, 1024, 768);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		ClassDataButton.reloadDataFile();
	}
	
	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON1) {
			if(System.currentTimeMillis() - trayClickTimer < 250L) {
				frame.setExtendedState(JFrame.NORMAL);
				frame.setVisible(true);
				ClassDataButton.updateAllButtons();
			}
		}
		trayClickTimer = System.currentTimeMillis();
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
		frame.setExtendedState(JFrame.ICONIFIED);
		frame.setVisible(false);
	}
	
	private static JButton newDayButton(String dayName, int xCoord, Font bigFont) {
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