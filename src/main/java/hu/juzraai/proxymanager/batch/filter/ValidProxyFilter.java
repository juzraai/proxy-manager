package hu.juzraai.proxymanager.batch.filter;

import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.filter.RecordFilter;
import org.easybatch.core.record.StringRecord;
import org.slf4j.Logger;

/**
 * @author Zsolt Jurányi
 */
public class ValidProxyFilter implements RecordFilter<StringRecord> {

	private static final Logger L = LoggerFactory.getLogger(ValidProxyFilter.class);

	@Override
	public StringRecord processRecord(StringRecord record) {
		boolean valid = ProxyValidator.isValidIpPort(record.getPayload());
		L.trace("Raw proxy '{}' is {}", record.getPayload(), valid ? "valid" : "invalid");
		return valid ? record : null;
	}
}
