package hu.juzraai.proxymanager.batch.writer;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import org.easybatch.core.writer.RecordWriter;
import org.easybatch.core.writer.RecordWritingException;

import javax.annotation.Nonnull;

/**
 * Prints out the proxy from record's payload to standard output in IP:PORT
 * format.
 *
 * @author Zsolt Jurányi
 */
public class StdoutProxyWriter implements RecordWriter<ProxyRecord> {

	@Nonnull
	@Override
	public ProxyRecord processRecord(@Nonnull ProxyRecord record) throws RecordWritingException {
		System.out.println(record.getPayload().getIpPort());
		return record;
	}
}
