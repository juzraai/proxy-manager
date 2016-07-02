package hu.juzraai.proxymanager.fetch;

import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class IpAdressDotComPLD extends ProxyListDownloaderTask {

	private static final Logger L = LoggerFactory.getLogger(IpAdressDotComPLD.class);

	@Override
	public Set<String> call() throws Exception {
		Set<String> proxies = new HashSet<>();

		L.info("Starting");
		Document d = getDocument("http://www.ip-adress.com/proxy_list/?k=time&d=desc");
		for (Element tr : d.select("table.proxylist tr")) {
			Elements tds = tr.select("td");
			if (!tds.isEmpty()) {
				String proxy = tds.get(0).ownText();
				if (ProxyValidator.isValidIpPort(proxy)) {
					proxies.add(proxy);
				}
			}
		}

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}

}
