package hu.juzraai.proxymanager.fetch;

import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class FreeProxyListsDotComPLD extends ProxyListDownloaderTask {

	private static final Logger L = LoggerFactory.getLogger(FreeProxyListsDotComPLD.class);

	@Override
	public Set<String> call() throws Exception {
		Set<String> proxies = new HashSet<>();

		L.info("Starting");
		Document d = getDocument("http://www.freeproxylists.com/anon.php");
		String listId = d.select("a[href^=anon]").first().attr("href").replaceAll(".*\\/", "");
		String url = "http://www.freeproxylists.com/load_anon_" + listId;
		d = getDocument(url);
		String html = d.select("quote").html().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		d = Jsoup.parse(html);
		for (Element tr : d.select("tr")) {
			Elements tds = tr.select("td");
			if (2 <= tds.size()) {
				String ip = tds.get(0).ownText();
				String port = tds.get(1).ownText();
				String proxy = ip + ":" + port;
				if (ProxyValidator.isValidIpPort(proxy)) {
					proxies.add(proxy);
				}
			}
		}

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}

}
