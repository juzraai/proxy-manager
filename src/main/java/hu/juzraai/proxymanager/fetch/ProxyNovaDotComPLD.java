package hu.juzraai.proxymanager.fetch;

import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class ProxyNovaDotComPLD extends ProxyListDownloaderTask {

	private static final Logger L = LoggerFactory.getLogger(ProxyNovaDotComPLD.class);

	@Override
	public Set<String> call() throws Exception {
		Set<String> proxies = new HashSet<>();

		L.info("Starting");
		String url = "http://www.proxynova.com/proxy-server-list/";
		Document d = getDocument(url);
		proxies.addAll(parseProxiesFromTable(d, "table#tbl_proxy_list tr", 0, 1));

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}
}
