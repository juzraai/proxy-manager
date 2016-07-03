package hu.juzraai.proxymanager.fetch;

import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class IdCloakDotComPLD extends ProxyListDownloaderTask {

	private static final Logger L = LoggerFactory.getLogger(IdCloakDotComPLD.class);

	@Override
	public Set<String> call() throws Exception {
		Set<String> proxies = new HashSet<>();

		L.info("Starting");
		String url = "http://www.idcloak.com/proxylist/free-proxy-ip-list.html";
		Document d = getDocument(url);
		proxies.addAll(parseProxiesFromTable(d, "div.proxy_table tr", 7, 6));

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}
}
