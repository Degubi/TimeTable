package degubi.listeners;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import degubi.TimeTableMain;
import degubi.data.Friend;
import degubi.data.Note;
import degubi.gui.ClassButton;
import degubi.gui.PopupGuis;
import degubi.tools.NIO;

public class SystemTrayListener extends MouseAdapter {
	public static final JMenuItem screenshotItem = SystemTrayListener.newMenuItem("Órarend Fénykép", "screencap.png", NIO::createScreenshot);
	public static final JCheckBoxMenuItem sleepMode = new JCheckBoxMenuItem("Alvó Mód", NIO.getIcon("sleep.png", 24), false);

	private final JPopupMenu popMenu = initTrayMenu();
	
	@SuppressWarnings("boxing")
	private static JPopupMenu initTrayMenu() {
		JSlider brightnessSlider = new JSlider(0, 16, 16);
		brightnessSlider.addChangeListener(TimeTableMain.mainPanel);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setMaximumSize(new Dimension(155, 40));
		
		JMenu friendMenu = new JMenu("Ismerõsök");
		friendMenu.setIcon(NIO.getIcon("friends.png", 24));
		Friend.reinitFriendsMenu(friendMenu);
		
		var labelTable = new Hashtable<Integer, JLabel>(2);
		labelTable.put(0, new JLabel("Sötét"));
		labelTable.put(16, new JLabel("Világos"));
		brightnessSlider.setLabelTable(labelTable);
		
		JMenu notesMenu = new JMenu("Jegyzetek");
		notesMenu.setIcon(NIO.getIcon("notes.png", 24));
		JPanel panel = new JPanel(null);
		Note.initNotes(panel);
		notesMenu.getPopupMenu().add(new JScrollPane(panel));
		notesMenu.getPopupMenu().setPreferredSize(new Dimension(300, 300));
		
		JPopupMenu popMenu = new JPopupMenu();
		popMenu.setPreferredSize(new Dimension(160, 240));
		popMenu.add(newMenuItem("Megnyitás", "open.png", SystemTrayListener::trayOpenGui));
		popMenu.addSeparator();
		popMenu.add(friendMenu);
		popMenu.add(notesMenu);
		popMenu.add(sleepMode);
		popMenu.add(screenshotItem);
		popMenu.add(newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
		popMenu.addSeparator();
		popMenu.add(brightnessSlider);
		popMenu.addSeparator();
		popMenu.add(newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));
		
		SwingUtilities.updateComponentTreeUI(popMenu);
		return popMenu;
	}
	
	private static void trayOpenGui(@SuppressWarnings("unused") ActionEvent event) {
		ClassButton.updateAllButtons(true, TimeTableMain.dataTable);
		((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor()).setExtendedState(JFrame.NORMAL);
	}
	
	private static JMenuItem newMenuItem(String text, String iconPath, ActionListener listener) {
		JMenuItem item = new JMenuItem(text, NIO.getIcon(iconPath, 24));
		item.addActionListener(listener);
		return item;
	}
	
	
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
			ClassButton.updateAllButtons(true, TimeTableMain.dataTable);
			((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor()).setExtendedState(JFrame.NORMAL);
		}
	}
}