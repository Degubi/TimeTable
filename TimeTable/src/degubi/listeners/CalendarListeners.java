package degubi.listeners;

import java.awt.Color;
import java.time.LocalDate;

import javax.swing.JOptionPane;

import com.github.lgooddatepicker.optionalusertools.CalendarListener;
import com.github.lgooddatepicker.optionalusertools.DateHighlightPolicy;
import com.github.lgooddatepicker.zinternaltools.CalendarSelectionEvent;
import com.github.lgooddatepicker.zinternaltools.HighlightInformation;
import com.github.lgooddatepicker.zinternaltools.YearMonthChangeEvent;

import degubi.PropertyFile;

public final class CalendarListeners implements DateHighlightPolicy, CalendarListener{
	private static long calendarClickCounter = 0;
	
	@Override 
	public HighlightInformation getHighlightInformationOrNull(LocalDate date) {
		String dateStr = date.toString();
		
		return PropertyFile.calendarMap.containsKey(dateStr) ? new HighlightInformation(Color.ORANGE, Color.BLACK, PropertyFile.calendarMap.get(dateStr)) : null;
	}

	@Override
	public void selectedDateChanged(CalendarSelectionEvent event) {
		if(System.currentTimeMillis() - calendarClickCounter < 300L) {
			String message = JOptionPane.showInputDialog("Írd be az eseményt!");
			
			if(message != null) {
				PropertyFile.calendarMap.put(event.getNewDate().toString(), message);
				PropertyFile.setMap("calendar", PropertyFile.calendarMap);
			}
		}
		calendarClickCounter = System.currentTimeMillis();
	}

	@Override
	public void yearMonthChanged(YearMonthChangeEvent event) {}
}