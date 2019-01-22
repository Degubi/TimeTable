package degubi;

import com.google.gson.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public final class ButtonTable extends JComponent{
	public static final Font tableHeaderFont = new Font("SansSerif", Font.PLAIN, 20);
	
	public final List<JButton> dataButtonList = new ArrayList<>();
	private final String[] columns;
	private final int[] horizontalIndexers;
	private final int cellWidth, cellHeight;
	
	public ButtonTable(int cellWidth, int cellHeight, int x, int y, boolean addListener, String... columnNames) {
		horizontalIndexers = columnNames.length == 0 ? new int[1] : new int[columnNames.length];
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		columns = columnNames;
		
		setBounds(x, y, cellWidth * horizontalIndexers.length + cellWidth, 600);
		
		for(int topIndex = 0; topIndex < columnNames.length; ++topIndex) {
			var topAdd = new JButton(columnNames[topIndex]);
			topAdd.setFocusable(false);
			if(addListener) topAdd.addMouseListener(new CreateClassListener(columnNames[topIndex]));
			topAdd.setBackground(Color.GRAY);
			topAdd.setForeground(Color.BLACK);
			topAdd.setFont(tableHeaderFont);
			topAdd.setBounds(getX() + topIndex * cellWidth + (topIndex * 20), getY(), cellWidth, 40);
			add(topAdd);
		}
	}
	
	public ButtonTable(int perCellWidth, int perCellHeight, int x, int y, Map<String, List<String>> bigData, String currentRoom) {
		this(perCellWidth, perCellHeight, x, y, false, bigData.keySet().toArray(new String[0]));
		
		bigData.forEach((columnName, rows) -> rows.forEach(row -> {
			var butt = new JButton(row);
			var isCurrentRoom = row.equals(currentRoom);
			
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
	
	private void resetTable() {
		dataButtonList.forEach(this::remove);
		dataButtonList.clear();
		Arrays.fill(horizontalIndexers, 0);
	}
	
	public String getNextOrPrevColumn(boolean isNext, String day) {
		int currentIndex = Settings.indexOf(day, columns);
		return isNext ? columns[currentIndex == columns.length - 1 ? 0 : ++currentIndex] : columns[currentIndex == 0 ? columns.length - 1 : --currentIndex];
	}
	
	public void tableAdd(String columnName, JButton buttonToAdd) {
		buttonToAdd.setFocusable(false);
		int column = Settings.indexOf(columnName, columns);
		buttonToAdd.setBounds(getX() + column * cellWidth + (column * 20), getY() + 50 + horizontalIndexers[column], cellWidth, cellHeight);
		horizontalIndexers[column] += cellHeight + 6;
		add(buttonToAdd);
		dataButtonList.add(buttonToAdd);
	}
	
	public void addNewClass(JsonArray array, JsonObject newClass) {
		array.add(newClass);
		reloadTable(array, false);
		Settings.save();
	}
	
	public void editClass(JsonArray array, JsonObject oldClass, JsonObject newClass) {
		array.remove(oldClass);
		addNewClass(array, newClass);
	}
	
	public void deleteClass(JsonArray array, JsonObject classObject) {
		array.remove(classObject);
		reloadTable(array, false);
		Settings.save();
	}
	
	public void reloadTable(JsonArray data, boolean showFrame) {
		resetTable();
		
		Settings.stream(data)
				 .map(object -> new ClassButton(object, this))
				 .sorted(Comparator.comparingInt((ClassButton button) -> Settings.indexOf(button.day, columns)).thenComparing(button -> button.startTime).thenComparing(button -> button.className))
				 .forEach(button -> tableAdd(button.day, button));
		
		ClassButton.updateAllButtons(showFrame, this);
	}
	
	private static final class CreateClassListener extends MouseAdapter{
		private final String dayStr;
		
		public CreateClassListener(String dayStr) {
			this.dayStr = dayStr;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
				var newDay = Settings.classObjectFromData(dayStr, "Óra", "Elõadás", "08:00", "10:00", "Terem", false);
				PopupGuis.showEditorGui(null, new ClassButton(newDay, TimeTableMain.dataTable));
			}
		}
	}
}