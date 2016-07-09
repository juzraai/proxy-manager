package hu.juzraai.proxymanager.batch.writer;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import org.easybatch.core.writer.RecordWriter;
import org.easybatch.core.writer.RecordWritingException;

/**
 * @author Zsolt Jurányi
 */
public class StdoutProxyWriter implements RecordWriter<ProxyRecord> {

	@Override
	public ProxyRecord processRecord(ProxyRecord record) throws RecordWritingException {
		System.out.println(record.getPayload().getIpPort());
		return record;
	}
}
