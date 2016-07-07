package hu.juzraai.proxymanager.test;

import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;

import static org.jsoup.Connection.Response;

/**
 * @author Zsolt Jurányi
 */
public class ProxyServerPrivacyDotComProxyTester extends ProxyTester {

	private static final Logger L = LoggerFactory.getLogger(ProxyServerPrivacyDotComProxyTester.class);

	@Override
	public String getTestPageUrl() {
		return "http://www.proxyserverprivacy.com/adv-free-proxy-detector.shtml";
	}

	@Override
	protected Boolean parseIfAnon(Response r) {
		Boolean result = null;
		try {
			Document d = r.parse();
			Element e = d.select("h5").first().nextElementSibling();
			result = "You do not use proxy".equalsIgnoreCase(e.text().trim());
		} catch (Exception e) {
			L.trace("Failed to parse test page", e);
		}
		return result;
	}

}
