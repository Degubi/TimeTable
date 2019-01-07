package degubi.listeners;

import com.google.gson.*;
import degubi.*;
import degubi.gui.*;
import degubi.tools.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

public class SystemTrayListener implements GuiTools {
	public static final JMenuItem screenshotItem = GuiTools.newMenuItem("Órarend Fénykép", "screencap.png", SystemTrayListener::createScreenshot);
	public static final JCheckBoxMenuItem sleepMode = new JCheckBoxMenuItem("Alvó Mód", GuiTools.getIcon("sleep.png", 24), false);

	private final JPopupMenu popMenu = initTrayMenu();
	
	private static void createScreenshot(@SuppressWarnings("unused") ActionEvent event) { // NO_UCD (unused code)
		var window = TimeTableMain.mainPanel.getTopLevelAncestor().getLocationOnScreen();
		try {
			ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.x + 50, window.y + 80, 870, 600)), "PNG", new File(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_kk_HH_ss")) +".png"));
		} catch (HeadlessException | AWTException | IOException e1) {}
	}
	
	@SuppressWarnings("boxing")
	private static JPopupMenu initTrayMenu() {
		var brightnessSlider = new JSlider(0, 16, 16);
		brightnessSlider.addChangeListener(TimeTableMain.mainPanel);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setMaximumSize(new Dimension(155, 40));
		
		var friendMenu = new JMenu("Ismerõsök");
		friendMenu.setIcon(GuiTools.getIcon("friends.png", 24));
		reinitFriendsMenu(friendMenu);
		
		var labelTable = new Hashtable<Integer, JLabel>(2);
		labelTable.put(0, new JLabel("Sötét"));
		labelTable.put(16, new JLabel("Világos"));
		brightnessSlider.setLabelTable(labelTable);
		
		var notesMenu = new JMenu("Jegyzetek");
		notesMenu.setIcon(GuiTools.getIcon("notes.png", 24));
		var panel = new JPanel(null);
		NoteButton.initNotes(panel);
		notesMenu.getPopupMenu().add(new JScrollPane(panel));
		notesMenu.getPopupMenu().setPreferredSize(new Dimension(300, 300));
		
		var popMenu = new JPopupMenu();
		popMenu.setPreferredSize(new Dimension(160, 240));
		popMenu.add(GuiTools.newMenuItem("Megnyitás", "open.png", SystemTrayListener::trayOpenGui));
		popMenu.addSeparator();
		popMenu.add(friendMenu);
		popMenu.add(notesMenu);
		popMenu.add(sleepMode);
		popMenu.add(screenshotItem);
		popMenu.add(GuiTools.newMenuItem("Beállítások", "settings.png", PopupGuis::showSettingsGui));
		popMenu.addSeparator();
		popMenu.add(brightnessSlider);
		popMenu.addSeparator();
		popMenu.add(GuiTools.newMenuItem("Bezárás", "exit.png", e -> System.exit(0)));
		
		SwingUtilities.updateComponentTreeUI(popMenu);
		return popMenu;
	}
	
	private static void trayOpenGui(@SuppressWarnings("unused") ActionEvent event) {
		ClassButton.updateAllButtons(true, TimeTableMain.dataTable);
		((JFrame)TimeTableMain.mainPanel.getTopLevelAncestor()).setExtendedState(JFrame.NORMAL);
	}
	
	
	public static void reinitFriendsMenu(JMenu friendMenu) {
		friendMenu.removeAll();
		var addFriendItem = GuiTools.newMenuItem("Ismerõs Hozzáadása", null, e -> addNewFriend(friendMenu));
		var removeFriendItem = GuiTools.newMenuItem("Ismerõs Eltávolítása", null, e -> removeFriend(friendMenu));
		
		Settings.friends.forEach(friend -> {
			var friendItem = GuiTools.newMenuItem(friend.getAsJsonObject().get("name").getAsString(), null, SystemTrayListener::handleFriendTable);
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