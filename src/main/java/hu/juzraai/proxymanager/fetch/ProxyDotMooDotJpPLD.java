package hu.juzraai.proxymanager.fetch;

import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class ProxyDotMooDotJpPLD extends ProxyListDownloaderTask {

	private static final Logger L = LoggerFactory.getLogger(ProxyDotMooDotJpPLD.class);

	@Override
	public Set<String> call() throws Exception {
		Set<String> proxies = new HashSet<>();

		L.info("Starting");
		getDocument("http://proxy.moo.jp/"); // need cookies > it needs Accept-Language
		int page = 1;
		boolean hasNextPage = true;
		do {
			Document d = getDocument("http://proxy.moo.jp/?u=90&page=" + page);
			for (Element tr : d.select("table.DataGrid tr")) {
				Elements tds = tr.select("td");
				if (2 <= tds.size()) {
					String ip = decodeIP(tds.get(0));
					String port = tds.get(1).text();
					String proxy = ip + ":" + port;
					if (ProxyValidator.isValidIpPort(proxy)) {
						proxies.add(proxy);
					}
				}
			}

			hasNextPage = !d.select("a[href*=page]:contains(Next)").isEmpty();
			page++;
		} while (hasNextPage);

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}

	private String decodeIP(Element td) {
		for (Element script : td.getElementsByTag("script")) {
			for (DataNode node : script.dataNodes()) {
				String data = node.getWholeData().trim();
				if (data.matches("IPDecode\\(\".*\"\\)")) {
					data = data.replaceAll(".*\\(\"", "").replaceAll("\".*", "");
					try {
						return URLDecoder.decode(data, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						return "";
					}
				}
			}
		}
		return "";
	}
}
