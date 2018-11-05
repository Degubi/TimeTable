package degubi.listeners;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import degubi.TimeTableMain;
import degubi.gui.ButtonTable;
import degubi.gui.ClassButton;
import degubi.gui.NoteButton;
import degubi.gui.PopupGuis;
import degubi.tools.NIO;
import degubi.tools.Settings;

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
		reinitFriendsMenu(friendMenu);
		
		var labelTable = new Hashtable<Integer, JLabel>(2);
		labelTable.put(0, new JLabel("Sötét"));
		labelTable.put(16, new JLabel("Világos"));
		brightnessSlider.setLabelTable(labelTable);
		
		var notesMenu = new JMenu("Jegyzetek");
		notesMenu.setIcon(NIO.getIcon("notes.png", 24));
		var panel = new JPanel(null);
		NoteButton.initNotes(panel);
		notesMenu.getPopupMenu().add(new JScrollPane(panel));
		notesMenu.getPopupMenu().setPreferredSize(new Dimension(300, 300));
		
		var popMenu = new JPopupMenu();
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
	
	
	public static void reinitFriendsMenu(JMenu friendMenu) {
		friendMenu.removeAll();
		JMenuItem addFriendItem = new JMenuItem("Ismerõs Hozzáadása");
		addFriendItem.addActionListener(e -> addNewFriend(friendMenu));
		
		Settings.friends.forEach(friend -> {
			JMenuItem friendItem = new JMenuItem(friend.getAsJsonObject().get("name").getAsString());
			friendItem.setActionCommand(friend.getAsJsonObject().get("url").getAsString());
			friendItem.addActionListener(SystemTrayListener::handleFriendTable);
			friendMenu.add(friendItem);
		});
		
		if(Settings.friends.size() != 0) {
			friendMenu.addSeparator();
		}
		friendMenu.add(addFriendItem);
	}
	
	private static void addNewFriend(JMenu friendMenu) {
		String friendName = JOptionPane.showInputDialog("Írd be haverod nevét!");
		
		if(friendName != null && !friendName.isEmpty()) {
			String friendURL = JOptionPane.showInputDialog("Írd be haverod URL-jét!");
		
			if(friendURL != null && !friendURL.isEmpty()) {
				var newFriend = Settings.newFriendObject(friendName, friendURL);
				Settings.friends.add(newFriend);
				reinitFriendsMenu(friendMenu);
				Settings.save();
			}
		}
	}
	
	private static void handleFriendTable(ActionEvent event) {
		var friendTable = new ButtonTable(150, 96, 25, 30, false, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
		var data = new byte[1000];
		
		int readCount = 0;
		try(var reader = new URL(event.getActionCommand()).openStream()){
			readCount = reader.read(data, 0, data.length);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ClassButton.reloadData(List.of(new String(data, 0, readCount, StandardCharsets.UTF_8).split("\n")), friendTable, false);
		PopupGuis.showNewDialog(false, ((JMenuItem)event.getSource()).getText() + " Órarendje", 930, 700, null, friendTable);
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