package degubi.data;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import degubi.gui.ButtonTable;
import degubi.gui.ClassButton;
import degubi.gui.PopupGuis;
import degubi.tools.IPropertyObjectBuilder;
import degubi.tools.PropertyFile;

public final class Friend {
	public static final FriendBuilder builder = new FriendBuilder();
	
	public final String name, url;
	
	public Friend(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public static void reinitFriendsMenu(JMenu friendMenu) {
		friendMenu.removeAll();
		JMenuItem addFriendItem = new JMenuItem("Ismerõs Hozzáadása");
		addFriendItem.addActionListener(e -> addNewFriend(friendMenu));
		
		PropertyFile.friends.forEach(friend -> {
			JMenuItem friendItem = new JMenuItem(friend.name);
			friendItem.setActionCommand(friend.url);
			friendItem.addActionListener(Friend::handleFriendTable);
			friendMenu.add(friendItem);
		});
		
		if(!PropertyFile.friends.isEmpty()) {
			friendMenu.addSeparator();
		}
		friendMenu.add(addFriendItem);
	}
	
	private static void addNewFriend(JMenu friendMenu) {
		String friendName = JOptionPane.showInputDialog("Írd be haverod nevét!");
		
		if(friendName != null && !friendName.isEmpty()) {
			String friendURL = JOptionPane.showInputDialog("Írd be haverod URL-jét!");
		
			if(friendURL != null && !friendURL.isEmpty()) {
				PropertyFile.friends.add(new Friend(friendName, friendURL));
				reinitFriendsMenu(friendMenu);
				PropertyFile.setObjectList("friends", Friend.builder, PropertyFile.friends);
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
	
	private static final class FriendBuilder implements IPropertyObjectBuilder<Friend>{

		@Override
		public String writeObject(Friend object) {
			return object.name + ' ' + object.url;
		}

		@Override
		public Friend readObject(String dataStr) {
			String[] data = dataStr.split(" ", 2);
			return new Friend(data[0], data[1]);
		}
	}
}