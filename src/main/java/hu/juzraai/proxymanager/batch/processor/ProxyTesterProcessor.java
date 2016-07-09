package hu.juzraai.proxymanager.batch.processor;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.test.ProxyTester;
import org.easybatch.core.processor.RecordProcessingException;
import org.easybatch.core.processor.RecordProcessor;

import java.io.IOException;

/**
 * @author Zsolt Jurányi
 */
public class ProxyTesterProcessor implements RecordProcessor<ProxyRecord, ProxyRecord> {

	private final ProxyTester tester;

	public ProxyTesterProcessor(ProxyTester tester) {
		this.tester = tester;
	}

	@Override
	public ProxyRecord processRecord(ProxyRecord record) throws RecordProcessingException {
		try {
			tester.test(record.getPayload());
		} catch (IOException e) {
			throw new RecordProcessingException("Failed to test proxy", e);
		}
		return record;
	}
}
