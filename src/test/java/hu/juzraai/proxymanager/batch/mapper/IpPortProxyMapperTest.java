package hu.juzraai.proxymanager.batch.mapper;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import org.easybatch.core.mapper.RecordMappingException;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.StringRecord;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Zsolt Jurányi
 */
public class IpPortProxyMapperTest {

	@Test
	public void shouldCopyAllFields() throws RecordMappingException {
		Header h = new Header(0L, "s", new Date());
		StringRecord sr = new StringRecord(h, "1.2.3.4:5");
		ProxyRecord pr = new IpPortProxyMapper().processRecord(sr);
		assertNotNull(pr);
		assertNotNull(pr.getPayload());
		assertEquals(sr.getPayload(), pr.getPayload().getIpPort());
		assertEquals(sr.getHeader(), pr.getHeader());
	}

}