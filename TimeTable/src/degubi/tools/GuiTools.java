package degubi.tools;

import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalTime;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import degubi.TimeTableMain;

public interface GuiTools extends MouseListener{
    @Override default void mouseClicked(MouseEvent e) {};
    @Override default void mouseReleased(MouseEvent e) {}
    @Override default void mouseEntered(MouseEvent e) {}
    @Override default void mouseExited(MouseEvent e) {}
    
    public static JMenuItem newMenuItem(String text, String iconPath, ActionListener listener) {
		var item = new JMenuItem(text, iconPath == null ? null : getIcon(iconPath, 24));
		item.addActionListener(listener);
		return item;
	}
    
    public static JComboBox<String> newComboBox(String selectedItem, int y, String... data){
		var endTimeBox = new JComboBox<>(data);
		endTimeBox.setBounds(100, y, 75, 30);
		endTimeBox.setSelectedItem(selectedItem);
		return endTimeBox;
	}
    
    public static JButton newColorButton(int x, int y, Consumer<JButton> listener, Color startColor) {
		var butt = new JButton();
		butt.setBackground(startColor);
		butt.setBounds(x, y, 48, 48);
		butt.setFocusable(false);
		butt.setBorder(null);
		butt.addActionListener(e -> listener.accept(butt));
		return butt;
	}
    
    public static JButton newEditButton(int yPos, ImageIcon icon, ActionListener listener) {
		var butt = new JButton(icon);
		butt.setBackground(Color.LIGHT_GRAY);
		butt.setBounds(0, yPos, 32, 32);
		butt.addActionListener(listener);
		return butt;
	}
    
    public static void handleNightMode(Container container, LocalTime time) {
		var isDarkMode = time.isAfter(Settings.dayTimeEnd) || time.isBefore(Settings.dayTimeStart);
	
		if(container instanceof JLabel || container instanceof JCheckBox) {
			container.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
		}else{
			container.setBackground(isDarkMode ? Settings.nightTimeColor : Settings.dayTimeColor);
		}
	}
    
    public static ImageIcon getIcon(String path, int scale) {
		var image = Toolkit.getDefaultToolkit().getImage(TimeTableMain.class.getResource("/assets/" + path));
		if(scale == 0 || image.getWidth(null) == scale) {
			return new ImageIcon(image);
		}
		return new ImageIcon(image.getScaledInstance(scale, scale, Image.SCALE_SMOOTH));
	}
}