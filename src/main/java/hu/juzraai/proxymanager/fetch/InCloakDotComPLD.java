package hu.juzraai.proxymanager.fetch;

import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class InCloakDotComPLD extends ProxyListDownloaderTask {

	private static final Logger L = LoggerFactory.getLogger(InCloakDotComPLD.class);

	@Override
	public Set<String> call() throws Exception {
		Set<String> proxies = new HashSet<>();

		L.info("Starting");
		String url = "https://incloak.com/proxy-list/";
		Document d = getDocument(url);
		for (Element tr : d.select("table.proxy__t tr")) {
			Elements tds = tr.select("td");
			if (2 <= tds.size()) {
				String ip = tds.get(0).ownText();
				String port = tds.get(1).ownText();
				String proxy = ip + ":" + port;
				if (proxy.matches("\\d{1,3}(\\.\\d{1,3}){3}:\\d{2,5}")) {
					proxies.add(proxy);
				}
			}
		}

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}
}
