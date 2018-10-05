package degubi.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.LayerUI;

import degubi.TimeTableMain;

public class BrightnessOverlay extends LayerUI<JComponent> implements ChangeListener{
	private static Color brightnessColor = new Color(0, 0, 0, 0);
	
	@Override
	public void paint(Graphics graphics, JComponent component) {
		super.paint(graphics, component);
		
		graphics.setColor(brightnessColor);
		graphics.fillRect(0, 0, component.getWidth(), component.getHeight());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		brightnessColor = new Color(0, 0, 0, (16 - ((JSlider) e.getSource()).getValue()) * 13);
		TimeTableMain.mainPanel.repaint();
	}
}