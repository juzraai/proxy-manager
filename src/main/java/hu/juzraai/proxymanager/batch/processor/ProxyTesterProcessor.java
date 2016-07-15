package hu.juzraai.proxymanager.batch.processor;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.proxymanager.test.ProxyTester;
import org.easybatch.core.processor.RecordProcessingException;
import org.easybatch.core.processor.RecordProcessor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Calls <code>test</code> method of the wrapped {@link ProxyTester} object
 * which should test the proxy and save results in the {@link ProxyTestInfo}
 * object which is the payload of the record.
 *
 * @author Zsolt Jurányi
 */
public class ProxyTesterProcessor implements RecordProcessor<ProxyRecord, ProxyRecord> {

	private final ProxyTester tester;
	private final boolean auto;

	/**
	 * Creates a new instance.
	 *
	 * @param tester {@link ProxyTester} to be used to test proxies
	 * @param auto
	 */
	public ProxyTesterProcessor(@Nonnull ProxyTester tester, boolean auto) {
		this.tester = tester;
		this.auto = auto;
	}

	/**
	 * Calls <code>test</code> method of the wrapped {@link ProxyTester} object
	 * and passes the {@link ProxyTestInfo} payload which will be updated with
	 * the test result by the tester.
	 *
	 * @param record {@link ProxyRecord} to be tested
	 * @return The input record with its {@link ProxyTestInfo} payload updated
	 * @throws RecordProcessingException
	 */
	@Nonnull
	@Override
	public ProxyRecord processRecord(@Nonnull ProxyRecord record) throws RecordProcessingException {
		if (!auto || tester.shouldTestProxy(record.getPayload())) { // TODO javadoc auto test
			try {
				tester.test(record.getPayload()); // logging done inside
			} catch (IOException e) {
				throw new RecordProcessingException("Failed to test proxy", e);
			}
		}
		return record;
	}
}
