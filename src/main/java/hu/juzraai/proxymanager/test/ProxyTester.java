package hu.juzraai.proxymanager.test;

import hu.juzraai.proxymanager.data.ProxyTestInfo;
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

	private static final int TIMEOUT = 60 * 1000;
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	private static final long EXPECTED_PROXY_WORK_TIME = 10 * 60 * 1000; // 10 mins
	private static final int DEFAULT_TRY_COUNT = 1;

	private final Logger L;

	public ProxyTester(Logger l) {
		L = l;
	}

	protected void configureConnection(Connection c) {
		c.method(GET).timeout(TIMEOUT).userAgent(USER_AGENT);
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
			L.trace("Failed to fetch test page using proxy: {}:{}, error: {}", proxy.ip(), proxy.port(), e.getMessage());
			return null;
		}
	}

	public abstract String getTestPageUrl();

	public int getTries() {
		return DEFAULT_TRY_COUNT;
	}

	protected abstract Boolean parseIfAnon(Response r); // null means not working

	public boolean shouldTestProxy(ProxyTestInfo proxy) { // TODO unit test
		if (null == proxy.getWorking() || null == proxy.getLastChecked() || null == proxy.getSameResultSince()) {
			L.debug("Auto test decision: true for proxy {} because at least one crucial field is null", proxy.getId());
			return true;
		}
		long lc = proxy.getLastChecked();
		long sr = proxy.getSameResultSince();
		long now = new Date().getTime();
		boolean r;
		if (proxy.getWorking()) {
			r = now - lc > EXPECTED_PROXY_WORK_TIME;
			L.debug("Auto test decision: [{}] for working proxy {} because NOW {} - LC {} > EXP {}", r, proxy.getId(), now, lc, EXPECTED_PROXY_WORK_TIME);
		} else {
			r = now - lc > lc - sr;
			L.debug("Auto test decision: [{}] for non-working proxy {} because NOW {} - LC {} > LC {} - SR {}", r, proxy.getId(), now, lc, lc, sr);
		}
		return r;
	}

	public void test(ProxyTestInfo proxy) throws IOException {
		L.info("Testing proxy: {}", proxy.getId());

		int tries = getTries();
		boolean working = false;
		Boolean anon = null;
		while (0 < tries-- && !working) {
			L.trace("Try #{} of proxy: {}", getTries() - tries, proxy.getId());
			Response r = getTestPage(proxy);
			if (null != r) {
				anon = parseIfAnon(r);
				L.trace("Anonimity check for {} proxy: {}", proxy.getId(), anon);
				working = null != anon;
			}
		}

		if (!working) {
			L.debug("Test of proxy {} failed {} times, checking test site w/o proxy", proxy.getId(), getTries());
			Response r = getTestPage(null);
			if (null != r && null != parseIfAnon(r)) { // works w/o proxy
				L.debug("Test page is available so the proxy was wrong.");
			} else {
				String m = "Test page is unavailable or internet connection is broken";
				L.error(m);
				throw new IOException(m);
			}
		}

		Boolean oldResult = proxy.getWorking();
		proxy.setWorking(working);
		proxy.setAnon(Boolean.TRUE == anon);
		proxy.setLastChecked(new Date().getTime());
		if (!proxy.getWorking().equals(oldResult)) {
			proxy.setSameResultSince(proxy.getLastChecked());
		}
		L.trace("Test result: {}", proxy);
	}
}
