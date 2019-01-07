package degubi.listeners;

import java.awt.event.*;
import javax.swing.*;

public final class EditClassButtonListener extends WindowAdapter{
	private final JDialog passFrame;
	
	public EditClassButtonListener(JDialog frame) {
		passFrame = frame;
	}
	
	@Override
	public void windowLostFocus(WindowEvent event) {
		passFrame.dispose();
	}
}