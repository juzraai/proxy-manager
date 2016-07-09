package hu.juzraai.proxymanager.batch.filter;

import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.filter.RecordFilter;
import org.easybatch.core.record.StringRecord;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * Tests if raw proxy in {@link StringRecord} has valid IP:PORT format.
 *
 * @author Zsolt Jurányi
 */
public class ValidProxyFilter implements RecordFilter<StringRecord> {

	private static final Logger L = LoggerFactory.getLogger(ValidProxyFilter.class);

	/**
	 * Tests payload String if it has a valid IP:PORT format.
	 *
	 * @param record The {@link StringRecord} to be tested
	 * @return The record itself if it's valid or <code>null</code> otherwise
	 */
	@Override
	public StringRecord processRecord(@Nonnull StringRecord record) {
		boolean valid = ProxyValidator.isValidIpPort(record.getPayload());
		if (!valid) {
			L.trace("Proxy is invalid: {}", record.getPayload());
		}
		return valid ? record : null;
	}
}
