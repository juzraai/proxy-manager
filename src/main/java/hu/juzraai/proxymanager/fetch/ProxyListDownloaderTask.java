package hu.juzraai.proxymanager.fetch;

import hu.juzraai.toolbox.log.LoggerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class ProxyListDownloaderTask implements Callable<Set<String>> {

	private static final Logger L = LoggerFactory.getLogger(ProxyListDownloaderTask.class);
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

	protected Document getDocument(String url) throws IOException {
		L.trace("Downloading URL: {}", url);
		return Jsoup.connect(url).timeout(10 * 1000).userAgent(USER_AGENT).get();
	}

}
