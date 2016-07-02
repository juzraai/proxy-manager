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
 * @author Zsolt Jurányi
 */
public class HideMyIpDotComPLD extends ProxyListDownloaderTask {

	private static final Logger L = LoggerFactory.getLogger(HideMyIpDotComPLD.class);
	private static final Pattern PATTERN = Pattern.compile("\"i\":\"(?<ip>\\d{1,3}(\\.\\d{1,3}){3})\",\"p\":\"(?<port>\\d{2,5})\"");

	@Override
	public Set<String> call() throws Exception {
		Set<String> proxies = new HashSet<>();

		L.info("Starting");
		Document d = getDocument("https://www.hide-my-ip.com/proxylist.shtml");
		boolean found = false;
		for (Element script : d.getElementsByTag("script")) {
			for (DataNode node : script.dataNodes()) {
				String data = node.getWholeData().trim();
				if (data.contains("proxylist")) { // TODO parse JSON?
					found = true;
					for (String line : data.split("\n")) {
						Matcher m = PATTERN.matcher(line);
						if (m.find()) {
							String proxy = m.group("ip") + ":" + m.group("port");
							proxies.add(proxy);
						}
						if (100 <= proxies.size()) { // fresh ones are at the beginning of 4K list
							break;
						}
					}
				}
				break;
			}
			if (found) {
				break;
			}
		}

		L.info("Found {} proxies", proxies.size());
		return proxies;
	}
}
