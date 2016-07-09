package hu.juzraai.proxymanager.test;

import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Date;

import static org.jsoup.Connection.Method.GET;

/**
 * @author Zsolt Jurányi
 */
public abstract class ProxyTester {

	private static final Logger L = LoggerFactory.getLogger(ProxyTester.class);
	private static final int TIMEOUT = 60 * 1000;
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	private static int DEFAULT_RETRY_COUNT = 2;

	protected void configureConnection(Connection c) {
		c.method(GET).timeout(TIMEOUT).userAgent(USER_AGENT);
	}

	public int getRetries() {
		return DEFAULT_RETRY_COUNT;
	}

	protected Response getTestPage(ProxyTestInfo proxy) {
		Connection c = Jsoup.connect(getTestPageUrl());
		if (null != proxy) {
			c.proxy(proxy.ip(), proxy.port());
		}
		configureConnection(c);
		try {
			return c.execute();
		} catch (IOException e) {
			L.trace("{} - Failed to fetch test page using proxy: {}:{}, error: {}", this.getClass().getSimpleName(), proxy.ip(), proxy.port(), e.getMessage());
			return null;
		}
	}

	public abstract String getTestPageUrl();

	protected abstract Boolean parseIfAnon(Response r); // null means not working

	public void test(ProxyTestInfo proxy) throws IOException {
		L.info("Testing proxy: {}", proxy.getId());
		Response r = null;

		int tries = getRetries();
		boolean working = false;
		while (0 < tries-- && !working) {
			L.trace("{} - Try #{} of proxy: {}", this.getClass().getSimpleName(), getRetries() - tries, proxy.getId());
			r = getTestPage(proxy);
			if (null != r) {
				Boolean anon = parseIfAnon(r);
				L.trace("{} - Anonimity check for {} proxy: {}", this.getClass().getSimpleName(), proxy.getId(), anon);
				if (null != anon) {
					working = true;
					proxy.setWorking(true);
					proxy.setAnon(anon);
					proxy.setLastChecked(new Date().getTime());
				}
			}
		}

		if (!working) {
			L.debug("{} - Test of proxy {} failed {} times, checking test site w/o proxy", this.getClass().getSimpleName(), proxy.getId(), getRetries());
			boolean workingWithoutProxy = false;
			r = getTestPage(null);
			if (null != r) {
				workingWithoutProxy = null != parseIfAnon(r);
			}
			if (workingWithoutProxy) {
				L.debug("{} - Test page is available so the proxy was wrong.", this.getClass().getSimpleName());
				proxy.setWorking(false);
				proxy.setAnon(false);
				proxy.setLastChecked(new Date().getTime());
			} else {
				String m = "Test page is unavailable or internet connection is broken";
				L.error("{} - {}", this.getClass().getSimpleName(), m);
				throw new IOException(m);
			}
		}
	}
}
