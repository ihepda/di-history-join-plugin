package it.claudio.dangelo.kettle.plugin.history;

import java.util.Calendar;

public enum AddTypes {
	Day(Calendar.DAY_OF_MONTH),
	Hour(Calendar.HOUR_OF_DAY),
	Year(Calendar.YEAR),
	Month(Calendar.MONTH),
	Minute(Calendar.MINUTE),
	Second(Calendar.SECOND);
	
	private int type;
	
	private AddTypes(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
}
