package hu.juzraai.proxymanager.batch.writer;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
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

	/**
	 * Prints out the <code>ipPort</code> field of the {@link ProxyTestInfo}
	 * payload of the given record.
	 *
	 * @param record {@link ProxyRecord} to read IP:PORT from
	 * @return The input record
	 * @throws RecordWritingException
	 */
	@Nonnull
	@Override
	public ProxyRecord processRecord(@Nonnull ProxyRecord record) throws RecordWritingException {
		System.out.println(record.getPayload().getIpPort());
		return record;
	}
}
