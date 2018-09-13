package degubi.listeners;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

public class EditClassButtonListener extends WindowAdapter{
	private final JDialog passFrame;
	
	public EditClassButtonListener(JDialog frame) {
		passFrame = frame;
	}
	
	@Override
	public void windowLostFocus(WindowEvent event) {
		passFrame.dispose();
	}
}