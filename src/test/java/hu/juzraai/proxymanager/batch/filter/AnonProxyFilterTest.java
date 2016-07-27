package hu.juzraai.proxymanager.batch.filter;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Zsolt Jurányi
 */
public class AnonProxyFilterTest {

	private ProxyRecord proxyRecordWithAnon(Boolean anon) {
		ProxyTestInfo p = new ProxyTestInfo();
		p.setAnon(anon);
		return new ProxyRecord(null, p);
	}

	@Test
	public void shouldReturnNullForNonAnon() {
		assertNull(new AnonProxyFilter().processRecord(proxyRecordWithAnon(false)));
	}

	@Test
	public void shouldReturnNullForUnknownAnon() {
		assertNull(new AnonProxyFilter().processRecord(proxyRecordWithAnon(null)));
	}

	@Test
	public void shouldReturnRecordForAnon() {
		ProxyRecord r = proxyRecordWithAnon(true);
		assertEquals(r, new AnonProxyFilter().processRecord(r));
	}


}