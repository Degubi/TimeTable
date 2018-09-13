package degubi.listeners;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import degubi.Main;

public class MainFrameIconifier extends WindowAdapter{

	@Override
	public void windowIconified(WindowEvent e) {
		Main.mainPanel.getTopLevelAncestor().setVisible(false);
	}
}