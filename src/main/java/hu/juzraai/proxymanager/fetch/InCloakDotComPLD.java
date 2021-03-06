package hu.juzraai.proxymanager.fetch;

import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.nodes.Document;
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
		proxies.addAll(parseProxiesFromTable(d, "table.proxy__t tr", 0, 1));

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}
}
