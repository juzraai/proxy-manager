package hu.juzraai.proxymanager.fetch;

import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

		// get list of proxy lists
		Document d = getDocument("http://www.freeproxylists.com/anon.php");

		// get link of freshest anon list
		String listId = d.select("a[href^=anon]").first().attr("href").replaceAll(".*\\/", "");

		// fetch
		d = getDocument("http://www.freeproxylists.com/load_anon_" + listId);

		// decode table
		String html = d.select("quote").html().replaceAll("&lt;", "<").replaceAll("&gt;", ">");

		// parse
		d = Jsoup.parse(html);
		proxies.addAll(parseProxiesFromTable(d, "tr", 0, 1));

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}

}
