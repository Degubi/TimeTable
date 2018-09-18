package degubi.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.JMenuItem;

import degubi.ButtonTable;
import degubi.ClassButton;
import degubi.PopupGuis;

public final class FriendButtonListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent event) {
		JMenuItem clickedButton = (JMenuItem) event.getSource();
		
		ButtonTable<ClassButton> friendTable = new ButtonTable<>(150, 96, 25, 30, false, "Hétfõ", "Kedd", "Szerda", "Csütörtök", "Péntek");
		byte[] data = new byte[1000];
		int readCount = 0;
		try(var reader = new URL(clickedButton.getActionCommand()).openStream()){
			readCount = reader.read(data, 0, data.length);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ClassButton.reloadData(List.of(new String(data, 0, readCount, StandardCharsets.UTF_8).split("\n")), friendTable, false);
		PopupGuis.showNewDialog(false, ((JMenuItem)event.getSource()).getText() + " Órarendje", 930, 700, null, friendTable);
	}
}