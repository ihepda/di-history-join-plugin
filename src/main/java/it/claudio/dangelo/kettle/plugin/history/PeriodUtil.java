package it.claudio.dangelo.kettle.plugin.history;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.pentaho.di.core.exception.KettleException;

class PeriodUtil {
	private Date[][] periods;
	private boolean[] writeNewRow;
	private int index = -1;
	
	private PeriodUtil(Date[][] periods, boolean[] writeNewRow) {
		super();
		this.periods = periods;
		this.writeNewRow = writeNewRow;
	}
	
	int getNumberOfRows() {
		return periods.length;
	}
	
	boolean next() {
		index++;
		return index < periods.length;
	}

	Date[] getPeriod() {
		return periods[index];
	}
	
	boolean writeNewRow() {
		return writeNewRow[index];
	}
	
	static PeriodUtil getInstance(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo,
			AddTypes addTypes) throws KettleException {
		PeriodUtil periodUtil = null;
		if(historyDateFrom.after(otherHistoryDateTo) || historyDateTo.before(otherHistoryDateFrom))
			periodUtil = case10(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.after(otherHistoryDateFrom) && historyDateTo.before(otherHistoryDateTo))
			periodUtil = case1(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.before(otherHistoryDateFrom) && historyDateTo.before(otherHistoryDateTo))
			periodUtil = case2(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.before(otherHistoryDateFrom) && historyDateTo.after(otherHistoryDateTo))
			periodUtil = case3(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.after(otherHistoryDateFrom) && historyDateTo.after(otherHistoryDateTo))
			periodUtil = case4(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.equals(otherHistoryDateFrom) && historyDateTo.before(otherHistoryDateTo))
			periodUtil = case5(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.equals(otherHistoryDateFrom) && historyDateTo.after(otherHistoryDateTo))
			periodUtil = case6(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.after(otherHistoryDateFrom) && historyDateTo.equals(otherHistoryDateTo))
			periodUtil = case7(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.before(otherHistoryDateFrom) && historyDateTo.equals(otherHistoryDateTo))
			periodUtil = case8(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else if(historyDateFrom.equals(otherHistoryDateFrom) && historyDateTo.equals(otherHistoryDateTo))
			periodUtil = case9(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
		else 
			throw new KettleException(getErrorMessage(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo));
		return periodUtil;
	}
	
	/**
	 * Case when the dates are out
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @param addTypes 
	 * @return
	 */
	private static PeriodUtil case10(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
//		return new PeriodUtil(new Date[][]{new Date[]{historyDateFrom, historyDateTo}}, new boolean[] {false});
		return null;
		
	}
	
	
	private static String getErrorMessage(Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("historyDateFrom : ").append(historyDateFrom).append('-');
		buffer.append("historyDateTo : ").append(historyDateTo).append('-');
		buffer.append("otherHistoryDateFrom : ").append(otherHistoryDateFrom).append('-');
		buffer.append("otherHistoryDateTo : ").append(otherHistoryDateTo);
		return buffer.toString();
	}
	
	
	/**
	 * Case when historyDateFrom = otherHistoryDateFrom &&  historyDateTo = otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @return
	 */
	private static PeriodUtil case9(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		return case1(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
	}

	/**
	 * Case when historyDateFrom > otherHistoryDateFrom &&  historyDateTo = otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @return
	 */
	private static PeriodUtil case8(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		return case2(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
	}
	

	/**
	 * Case when historyDateFrom > otherHistoryDateFrom &&  historyDateTo = otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @return
	 */
	private static PeriodUtil case7(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		return case1(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
	}
	
	/**
	 * Case when historyDateFrom = otherHistoryDateFrom &&  historyDateTo > otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @return
	 */
	private static PeriodUtil case6(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		return case4(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
	}

	/**
	 * Case when historyDateFrom = otherHistoryDateFrom &&  historyDateTo < otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @return
	 */
	private static PeriodUtil case5(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		return case1(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, addTypes);
	}
	
	
	/**
	 * Case when historyDateFrom > otherHistoryDateFrom &&  historyDateTo > otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @return
	 */
	private static PeriodUtil case4(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		Date[][] row = new Date[2][];
		boolean[] writes = new boolean[2];
		GregorianCalendar calendar = new GregorianCalendar();
		row[0] = new Date[] {historyDateFrom, otherHistoryDateTo};
		writes[0] = true;
		calendar.setTime(otherHistoryDateTo);
		calendar.add(addTypes.getType(), 1);
		row[1] = new Date[] {calendar.getTime(), historyDateTo};
		writes[1] = false;
		return new PeriodUtil(row, writes);
	}

	
	/**
	 * Case when historyDateFrom < otherHistoryDateFrom &&  historyDateTo > otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @return
	 */
	private static PeriodUtil case3(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		Date[][] row = new Date[3][];
		boolean[] writes = new boolean[3];
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(otherHistoryDateFrom);
		calendar.add(addTypes.getType(), -1);
		row[0] = new Date[] {historyDateFrom, calendar.getTime()};
		writes[0] = false;
		row[1] = new Date[] {otherHistoryDateFrom, otherHistoryDateTo};
		writes[1] = true;
		calendar.setTime(otherHistoryDateTo);
		calendar.add(addTypes.getType(), 1);
		row[2] = new Date[] {calendar.getTime(), historyDateTo};
		writes[2] = false;
		return new PeriodUtil(row, writes);
	}

	/**
	 * Case when historyDateFrom < otherHistoryDateFrom &&  historyDateTo < otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @param addTypes 
	 * @return
	 */
	private static PeriodUtil case2(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		Date[][] row = new Date[2][];
		boolean[] writes = new boolean[2];
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(otherHistoryDateFrom);
		calendar.add(addTypes.getType(), -1);
		row[0] = new Date[] {historyDateFrom, calendar.getTime()};
		writes[0] = false;
		row[1] = new Date[] {otherHistoryDateFrom, historyDateTo};
		writes[1] = true;
		return new PeriodUtil(row, writes);
	}

	
	/**
	 * Case when historyDateFrom > otherHistoryDateFrom &&  historyDateTo < otherHistoryDateTo
	 * @param historyDateFrom
	 * @param historyDateTo
	 * @param otherHistoryDateFrom
	 * @param otherHistoryDateTo
	 * @param addTypes 
	 * @return
	 */
	private static PeriodUtil case1(
			Date historyDateFrom,
			Date historyDateTo,
			Date otherHistoryDateFrom,
			Date otherHistoryDateTo, AddTypes addTypes) {
		return new PeriodUtil(new Date[][]{new Date[]{historyDateFrom, historyDateTo}}, new boolean[] {true});
		
	}
	
}
