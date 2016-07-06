package hu.juzraai.proxymanager.test;

import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Date;

import static org.jsoup.Connection.Method.GET;
import static org.jsoup.Connection.Response;

/**
 * @author Zsolt Jurányi
 */
public class ProxyTester {

	private static final Logger L = LoggerFactory.getLogger(ProxyTester.class);
	private static final int TRIES = 2;
	private static final String TESTER_PAGE_URL = "http://www.proxyserverprivacy.com/adv-free-proxy-detector.shtml";
	private static final int TIMEOUT = 60 * 1000;
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

	protected Response getTestPage(ProxyTestInfo proxy) {
		Connection c = Jsoup.connect(TESTER_PAGE_URL).method(GET).timeout(TIMEOUT).userAgent(USER_AGENT);
		if (null != proxy) {
			c.proxy(proxy.ip(), proxy.port());
		}
		try {
			return c.execute();
		} catch (IOException e) {
			L.trace("Failed to fetch test page using proxy: {}:{}, error: {}", proxy.ip(), proxy.port(), e.getMessage());
			return null;
		}
	}

	protected Boolean parseIfAnon(ProxyTestInfo proxy, Response r) {
		Boolean result = null;
		try {
			Document d = r.parse();
			Element e = d.select("h5").first().nextElementSibling();
			result = "You do not use proxy".equalsIgnoreCase(e.text().trim());
			L.trace("Anonimity check for {} proxy: {}", proxy.getId(), result);
		} catch (Exception e) {
			L.trace("Failed to parse test page when using proxy: " + proxy.getId(), e);
		}
		return result;
	}

	public void test(ProxyTestInfo proxy) {
		L.info("Testing proxy: {}", proxy.getId());
		Response r = null;

		int tries = TRIES;
		boolean working = false;
		while (0 < tries-- && !working) {
			r = getTestPage(proxy);
			if (null != r) {
				Boolean anon = parseIfAnon(proxy, r);
				if (null != r) {
					working = true;
					proxy.setWorking(true);
					proxy.setAnon(anon);
					proxy.setLastChecked(new Date().getTime());
				}
			}
		}

		if (!working) {
			L.debug("Test of proxy {} failed {} times, checking test site w/o proxy", proxy.getId(), TRIES);
			boolean workingWithoutProxy = false;
			r = getTestPage(null);
			if (null != r) {
				workingWithoutProxy = null != parseIfAnon(null, r);
			}
			if (workingWithoutProxy) {
				L.debug("Test page is available so the proxy was wrong.");
				proxy.setWorking(false);
				proxy.setAnon(false);
				proxy.setLastChecked(new Date().getTime());
			} else {
				L.error("Test page is unavailable or internet connection is broken");
			}
		}
	}

}
