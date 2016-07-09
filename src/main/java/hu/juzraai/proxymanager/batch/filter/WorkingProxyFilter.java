package hu.juzraai.proxymanager.batch.filter;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.filter.RecordFilter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * Tests whether proxy in {@link ProxyRecord} is considered working or not by
 * previous test.
 *
 * @author Zsolt Jurányi
 */
public class WorkingProxyFilter implements RecordFilter<ProxyRecord> {

	private static final Logger L = LoggerFactory.getLogger(WorkingProxyFilter.class);

	/**
	 * Tests payload {@link ProxyTestInfo} if it has <code>true</code> value in
	 * its <code>working</code> field.
	 *
	 * @param record The {@link ProxyRecord} to be tested
	 * @return The record itself it's considered working or <code>null</code>
	 * otherwise
	 */
	@Override
	public ProxyRecord processRecord(@Nonnull ProxyRecord record) {
		boolean working = Boolean.TRUE == record.getPayload().getWorking();
		if (!working) {
			L.trace("Proxy filtered out: {}", record.getPayload().getId());
		}
		return working ? record : null;
	}
}
