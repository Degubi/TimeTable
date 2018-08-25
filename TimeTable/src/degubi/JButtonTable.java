package degubi;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

public final class JButtonTable<T extends JButton> extends JComponent {
	private static final Font bigFont = new Font("TimesRoman", Font.PLAIN, 20);
	
	public final List<T> dataList = new ArrayList<>();
	private final String[] columns;
	private final int[] horizontalIndexers;
	private final int cellWidth;
	
	public JButtonTable(int perCellWidth, int x, int y, int height, String... columnNames) {
		horizontalIndexers = new int[columnNames.length];
		cellWidth = perCellWidth;
		columns = columnNames;
		
		Arrays.fill(horizontalIndexers, 20);
		setBounds(x, y, perCellWidth * columnNames.length + perCellWidth, height);
		
		for(int topIndex = 0; topIndex < columnNames.length; ++topIndex) {
			JButton topAdd = new JButton(columnNames[topIndex]);
			topAdd.setBorder(Main.blackBorder);
			topAdd.setFocusable(false);
			topAdd.setBackground(Color.LIGHT_GRAY);
			topAdd.setForeground(Color.BLACK);
			topAdd.setFont(bigFont);
			topAdd.setBounds(getX() + topIndex * cellWidth + (topIndex * 20), getY(), perCellWidth, 60);
			add(topAdd);
			
			@SuppressWarnings("unchecked")
			T inCast = (T) topAdd;
			
			dataList.add(inCast);
		}
	}
	
	private static int indexOf(String columnName, String[] data) {
		for(int k = 0; k < data.length; ++k) {
			if(columnName.equals(data[k])) {
				return k;
			}
		}
		throw new IllegalArgumentException("Unkown Column for Table: " + columnName);
	}
	
	public void tableRemove(T buttonToRemove) {
		remove(buttonToRemove);
	}
	
	public void tableAdd(String columnName, T buttonToAdd) {
		buttonToAdd.setFocusable(false);
		int column = indexOf(columnName, columns);
		buttonToAdd.setBounds(getX() + column * cellWidth + (column * 16), getY() + (horizontalIndexers[column] += 66), cellWidth, 60);
		add(buttonToAdd);
	}
}