package degubi;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JComponent;

public final class JButtonTable<T extends JButton> extends JComponent {
	public static final Font tableHeaderFont = new Font("TimesRoman", Font.BOLD, 20);
	
	private final List<T> dataButtonList = new ArrayList<>();
	private final String[] columns;
	private final int[] horizontalIndexers;
	private final int cellWidth, cellHeight;
	
	public JButtonTable(int perCellWidth, int perCellHeight, int x, int y, int height, String... columnNames) {
		horizontalIndexers = new int[columnNames.length];
		cellWidth = perCellWidth;
		cellHeight = perCellHeight;
		columns = columnNames;
		
		Arrays.fill(horizontalIndexers, 0);
		setBounds(x, y, perCellWidth * columnNames.length + perCellWidth, height);
		
		for(int topIndex = 0; topIndex < columnNames.length; ++topIndex) {
			JButton topAdd = new JButton(columnNames[topIndex]);
			topAdd.setBorder(Main.blackBorder);
			topAdd.setFocusable(false);
			topAdd.setBackground(Color.GRAY);
			topAdd.setForeground(Color.BLACK);
			topAdd.setFont(tableHeaderFont);
			topAdd.setBounds(getX() + topIndex * cellWidth + (topIndex * 20), getY(), perCellWidth, 40);
			add(topAdd);
		}
	}
	
	@SuppressWarnings("unchecked")
	public JButtonTable(int perCellWidth, int perCellHeight, int x, int y, int height, Map<String, List<String>> bigData, String currentRoom) {
		this(perCellWidth, perCellHeight, x, y, height, bigData.keySet().toArray(new String[0]));
		
		bigData.forEach((columnName, rows) -> rows.forEach(row -> {
			T butt = (T) new JButton(row);
			boolean isCurrentRoom = row.equals(currentRoom);
			
			butt.setBorder(Main.blackBorder);
			butt.setForeground(isCurrentRoom ? Color.BLACK : Color.GRAY);
			butt.setBackground(isCurrentRoom ? Color.RED : Color.LIGHT_GRAY);
			tableAdd(columnName, butt);
			
			butt.addActionListener(e -> {
				dataButtonList.forEach(checkButton -> {
					checkButton.setForeground(Color.GRAY);
					checkButton.setBackground(Color.LIGHT_GRAY);
				});
				
				butt.setForeground(Color.BLACK);
				butt.setBackground(Color.RED);
			});
		}));
	}
	
	private int indexOf(String column) {
		for(int k = 0; k < columns.length; ++k) {
			if(column.equals(columns[k])) {
				return k;
			}
		}
		throw new IllegalArgumentException("Unkown Column for Table: " + column);
	}
	
	public void tableRemove(T buttonToRemove) {
		remove(buttonToRemove);
		dataButtonList.remove(buttonToRemove);
	}
	
	public void resetTable() {
		dataButtonList.forEach(this::remove);
		dataButtonList.clear();
		Arrays.fill(horizontalIndexers, 0);
	}
	
	public Optional<T> findFirstButton(Predicate<T> predicate){
		return dataButtonList.stream().filter(predicate).findFirst();
	}
	
	public void tableAdd(String columnName, T buttonToAdd) {
		buttonToAdd.setFocusable(false);
		int column = indexOf(columnName);
		buttonToAdd.setBounds(getX() + column * cellWidth + (column * 20), getY() + (horizontalIndexers[column] += (cellHeight + 6)), cellWidth, cellHeight);
		add(buttonToAdd);
		dataButtonList.add(buttonToAdd);
	}
}