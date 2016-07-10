package hu.juzraai.proxymanager.batch.record;

import hu.juzraai.proxymanager.data.ProxyTestInfo;
import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.Header;

/**
 * A generic record with {@link ProxyTestInfo} payload.
 *
 * @author Zsolt Jurányi
 */
public class ProxyRecord extends GenericRecord<ProxyTestInfo> {

	/**
	 * Creates a new instance.
	 *
	 * @param header  Record header
	 * @param payload Payload
	 */
	public ProxyRecord(Header header, ProxyTestInfo payload) {
		super(header, payload);
	}
}
