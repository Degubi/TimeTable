package degubi;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.LayerUI;

public class BrightnessOverlay extends LayerUI<JComponent> implements ChangeListener{
	public static Color meow = new Color(0, 0, 0, 0);
	
	@Override
	public void paint(Graphics graphics, JComponent component) {
		super.paint(graphics, component);
		
		graphics.setColor(meow);
		graphics.fillRect(0, 0, component.getWidth(), component.getHeight());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider comp = (JSlider) e.getSource();
		meow = new Color(0, 0, 0, (16 - comp.getValue()) * 13);
		Main.mainPanel.repaint();
	}
}