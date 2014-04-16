package it.claudio.dangelo.kettle.plugin.history;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;


public class PeriodUtilTest {
	@Test
	public void case1() throws Exception{
		Date historyDateFrom = new GregorianCalendar(2001, 1, 1).getTime();
		Date historyDateTo = new GregorianCalendar(2005, 12, 31).getTime();
		Date otherHistoryDateFrom = new GregorianCalendar(2000, 5, 1).getTime();
		Date otherHistoryDateTo = new GregorianCalendar(2007, 11, 11).getTime();
		PeriodUtil instance = PeriodUtil.getInstance(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, AddTypes.Day);
		Assert.assertEquals(1, instance.getNumberOfRows());
		Assert.assertTrue(instance.next());
		Assert.assertEquals(historyDateFrom, instance.getPeriod()[0]);
		Assert.assertEquals(historyDateTo, instance.getPeriod()[1]);
		Assert.assertEquals(true, instance.writeNewRow());
		Assert.assertFalse(instance.next());
	}
	@Test
	public void case2() throws KettleException {
		Date historyDateFrom = new GregorianCalendar(2001, 1, 1).getTime();
		Date historyDateTo = new GregorianCalendar(2005, 12, 31).getTime();
		GregorianCalendar gregorianCalendar = new GregorianCalendar(2002, 5, 1);
		Date otherHistoryDateFrom = gregorianCalendar.getTime();
		Date otherHistoryDateTo = new GregorianCalendar(2007, 11, 11).getTime();
		PeriodUtil instance = PeriodUtil.getInstance(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, AddTypes.Day);
		Assert.assertEquals(2, instance.getNumberOfRows());
		Assert.assertTrue(instance.next());
		Assert.assertEquals(historyDateFrom, instance.getPeriod()[0]);
		gregorianCalendar.add(Calendar.DAY_OF_MONTH, -1);
		Assert.assertEquals(gregorianCalendar.getTime(), instance.getPeriod()[1]);
		Assert.assertFalse(instance.writeNewRow());
		Assert.assertTrue(instance.next());
		Assert.assertEquals(otherHistoryDateFrom, instance.getPeriod()[0]);
		Assert.assertEquals(historyDateTo, instance.getPeriod()[1]);
		Assert.assertTrue(instance.writeNewRow());
		Assert.assertFalse(instance.next());
	}
	@Test
	public void case9() throws KettleException {
		Date historyDateFrom = new GregorianCalendar(2001, 1, 1).getTime();
		Date historyDateTo = new GregorianCalendar(2005, 12, 31).getTime();
		GregorianCalendar gregorianCalendar = new GregorianCalendar(2001, 1, 1);
		Date otherHistoryDateFrom = gregorianCalendar.getTime();
		Date otherHistoryDateTo = new GregorianCalendar(2005, 12, 31).getTime();
		PeriodUtil instance = PeriodUtil.getInstance(historyDateFrom, historyDateTo, otherHistoryDateFrom, otherHistoryDateTo, AddTypes.Day);
		Assert.assertEquals(1, instance.getNumberOfRows());
		Assert.assertTrue(instance.next());
		Assert.assertEquals(historyDateFrom, instance.getPeriod()[0]);
		Assert.assertEquals(historyDateTo, instance.getPeriod()[1]);
		Assert.assertTrue(instance.writeNewRow());
		Assert.assertFalse(instance.next());
	}
}
