package hu.juzraai.proxymanager.batch.filter;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Zsolt Jurányi
 */
public class WorkingProxyFilterTest {

	private ProxyRecord proxyRecordWithWorking(Boolean working) {
		ProxyTestInfo p = new ProxyTestInfo();
		p.setWorking(working);
		return new ProxyRecord(null, p);
	}

	@Test
	public void shouldReturNullForNonWorking() {
		assertNull(new WorkingProxyFilter().processRecord(proxyRecordWithWorking(false)));
	}

	@Test
	public void shouldReturNullForUnknownWorking() {
		assertNull(new WorkingProxyFilter().processRecord(proxyRecordWithWorking(null)));
	}

	@Test
	public void shouldReturnRecordForWorking() {
		ProxyRecord r = proxyRecordWithWorking(true);
		assertEquals(r, new WorkingProxyFilter().processRecord(r));
	}
}