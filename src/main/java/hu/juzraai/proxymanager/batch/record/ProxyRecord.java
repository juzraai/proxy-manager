package hu.juzraai.proxymanager.batch.record;

import hu.juzraai.proxymanager.data.ProxyTestInfo;
import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.Header;

/**
 * @author Zsolt Jurányi
 */
public class ProxyRecord extends GenericRecord<ProxyTestInfo> {

	public ProxyRecord(Header header, ProxyTestInfo payload) {
		super(header, payload);
	}
}
