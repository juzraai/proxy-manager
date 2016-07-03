package hu.juzraai.proxymanager.fetch;

import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.jsoup.Connection.Method;
import static org.jsoup.Connection.Response;

public abstract class ProxyListDownloaderTask implements Callable<Set<String>> {

	private static final Logger L = LoggerFactory.getLogger(ProxyListDownloaderTask.class);
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

	private final Map<String, String> cookies = new HashMap<>();

	protected Document getDocument(String url) throws IOException {
		L.trace("Downloading URL: {}", url);
		Response r = Jsoup.connect(url)
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.header("Accept-Encoding", "gzip, deflate, sdch")
				.header("Accept-Language", "hu-HU,hu;q=0.8,en-US;q=0.6,en;q=0.4")
				.cookies(cookies)
				.userAgent(USER_AGENT)
				.method(Method.GET)
				.timeout(10 * 1000)
				.execute();
		cookies.putAll(r.cookies());
		return r.parse();
	}

	protected Set<String> parseProxiesFromTable(Document d, String trSelector, int ipTdIndex, int portTdIndex) {
		Set<String> proxies = new HashSet<>();
		for (Element tr : d.select(trSelector)) {
			Elements tds = tr.select("td");
			if (Math.max(ipTdIndex, portTdIndex) < tds.size()) {
				String ip = tds.get(ipTdIndex).text();
				String port = tds.get(portTdIndex).text();
				String proxy = String.format("%s:%s", ip, port);
				if (ProxyValidator.isValidIpPort(proxy)) {
					proxies.add(proxy);
				}
			}
		}
		return proxies;
	}

}
