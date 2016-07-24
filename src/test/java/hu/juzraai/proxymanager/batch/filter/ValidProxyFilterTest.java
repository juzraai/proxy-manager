package hu.juzraai.proxymanager.batch.filter;

import hu.juzraai.proxymanager.util.ProxyValidatorTest;
import org.easybatch.core.record.StringRecord;

/**
 * @author Zsolt Jurányi
 */
public class ValidProxyFilterTest extends ProxyValidatorTest {

	private final ValidProxyFilter f = new ValidProxyFilter();

	@Override
	protected boolean isValid(String proxy) {
		return null != f.processRecord(new StringRecord(null, proxy));
	}
}