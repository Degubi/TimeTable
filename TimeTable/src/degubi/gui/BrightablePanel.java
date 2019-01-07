package degubi.gui;

import degubi.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public final class BrightablePanel extends JPanel implements ChangeListener{
	private static Color brightnessColor = new Color(0, 0, 0, 0);
	
	public BrightablePanel() {
		super(null);
	}
	
	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		
		graphics.setColor(brightnessColor);
		graphics.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		brightnessColor = new Color(0, 0, 0, (16 - ((JSlider) e.getSource()).getValue()) * 13);
		TimeTableMain.mainPanel.repaint();
	}
}