package degubi;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComponent;

public final class JButtonTable<T extends JButton> extends JComponent {
	public static final Font tableHeaderFont = new Font("TimesRoman", Font.BOLD, 20);
	
	private final List<T> dataButtonList = new ArrayList<>();
	private final String[] columns;
	private final int[] horizontalIndexers;
	private final int cellWidth, cellHeight;
	
	public JButtonTable(int cellWidth, int cellHeight, int x, int y, boolean addListener, String... columnNames) {
		horizontalIndexers = new int[columnNames.length];
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		columns = columnNames;
		
		Arrays.fill(horizontalIndexers, 0);
		setBounds(x, y, cellWidth * columnNames.length + cellWidth, 600);
		
		for(int topIndex = 0; topIndex < columnNames.length; ++topIndex) {
			JButton topAdd = new JButton(columnNames[topIndex]);
			topAdd.setBorder(Main.blackBorder);
			topAdd.setFocusable(false);
			if(addListener) topAdd.addMouseListener(new CreateClassListener(columnNames[topIndex]));
			topAdd.setBackground(Color.LIGHT_GRAY);
			topAdd.setForeground(Color.BLACK);
			topAdd.setFont(tableHeaderFont);
			topAdd.setBounds(getX() + topIndex * cellWidth + (topIndex * 20), getY(), cellWidth, 40);
			add(topAdd);
		}
	}
	
	@SuppressWarnings("unchecked")
	public JButtonTable(int perCellWidth, int perCellHeight, int x, int y, Map<String, List<String>> bigData, String currentRoom) {
		this(perCellWidth, perCellHeight, x, y, false, bigData.keySet().toArray(new String[0]));
		
		bigData.forEach((columnName, rows) -> rows.forEach(row -> {
			T butt = (T) new JButton(row);
			boolean isCurrentRoom = row.equals(currentRoom);
			
			butt.setBorder(Main.blackBorder);
			butt.setForeground(isCurrentRoom ? Color.BLACK : Color.DARK_GRAY);
			butt.setBackground(isCurrentRoom ? Color.RED : Color.GRAY);
			tableAdd(columnName, butt);
			
			butt.addActionListener(e -> {
				dataButtonList.forEach(checkButton -> {
					checkButton.setForeground(Color.DARK_GRAY);
					checkButton.setBackground(Color.GRAY);
				});
				
				butt.setForeground(Color.BLACK);
				butt.setBackground(Color.RED);
			});
		}));
	}
	
	public int indexOf(String column) {
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
	
	public Optional<T> findFirstButton(Predicate<T> predicate){ return dataButtonList.stream().filter(predicate).findFirst(); }
	public Stream<T> tableDataStream(){ return dataButtonList.stream(); }
	public void tableAdd(T button) { dataButtonList.add(button); }
	public void forEachData(Consumer<T> button) { dataButtonList.forEach(button); }
	
	public void tableAdd(String columnName, T buttonToAdd) {
		buttonToAdd.setFocusable(false);
		int column = indexOf(columnName);
		buttonToAdd.setBounds(getX() + column * cellWidth + (column * 20), getY() + 50 + horizontalIndexers[column], cellWidth, cellHeight);
		horizontalIndexers[column] += cellHeight + 6;
		add(buttonToAdd);
		dataButtonList.add(buttonToAdd);
	}
	
	public static final class CreateClassListener extends MouseAdapter{
		private final String dayStr;
		
		public CreateClassListener(String dayStr) {
			this.dayStr = dayStr;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
				ButtonEditorGui.showEditorGui(true, new ClassDataButton(dayStr + " �ra El�ad�s 08:00 10:00 Terem false"));
			}
		}
	}
}