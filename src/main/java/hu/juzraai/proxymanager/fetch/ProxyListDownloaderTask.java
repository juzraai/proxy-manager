package hu.juzraai.proxymanager.fetch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class ProxyListDownloaderTask implements Callable<Set<String>> {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

	protected Document getDocument(String url) throws IOException {
		return Jsoup.connect(url).timeout(10 * 1000).userAgent(USER_AGENT).get();
	}

}
