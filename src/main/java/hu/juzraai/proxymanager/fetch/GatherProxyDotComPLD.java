package hu.juzraai.proxymanager.fetch;

import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zsolt on 2016. 07. 03..
 */
public class GatherProxyDotComPLD extends ProxyListDownloaderTask {

	private static final Logger L = LoggerFactory.getLogger(GatherProxyDotComPLD.class);
	private static final Pattern PATTERN = Pattern.compile("\"PROXY_IP\":\"(?<ip>\\d{1,3}(\\.\\d{1,3}){3})\",.*\"PROXY_PORT\":\"(?<port>[0-9A-F]{2,5})\"");

	@Override
	public Set<String> call() throws Exception {
		Set<String> proxies = new HashSet<>();

		L.info("Starting");
		String url = "http://gatherproxy.com/proxylist/anonymity/?t=Elite";
		Document d = getDocument(url);

		for (Element script : d.getElementsByTag("script")) {
			for (DataNode node : script.dataNodes()) {
				String data = node.getWholeData().trim();
				if (data.contains("PROXY_IP")) { // TODO parse JSON?
					for (String line : data.split("\n")) {
						Matcher m = PATTERN.matcher(line);
						if (m.find()) {
							String portInHex = m.group("port");
							Integer port = Integer.parseInt(portInHex, 16);
							String proxy = String.format("%s:%d", m.group("ip"), port);
							proxies.add(proxy);
						}
					}
				}
			}
		}

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}
}
