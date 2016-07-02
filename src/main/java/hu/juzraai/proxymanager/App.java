package hu.juzraai.proxymanager;

import hu.juzraai.proxymanager.fetch.ProxyListDownloaderEngine;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class App {
	// TODO crawlers
	// http://proxylist.hidemyass.com - obfuscated
	// http://proxy.moo.jp/?u=90&page=1 - need URLDecode

	// TODO Proxy.notWorkingSince

	// TODO easy batch
	// RecordReader: read stdin->ProxyTest / read db->ProxyTest
	// Filter: filter freshly checked
	// Filter: long time not working (notWorkingSince > 24h)
	// Proc: test, update test info in model
	// Writer: write into db

	// TODO site stat
	// query db
	// per site: proxy count, working count, anon count, common count

	// TODO checkfornull, nonnull

	private static final Logger L = LoggerFactory.getLogger(App.class);

	private final boolean readFromStdIn;


	public App(boolean readFromStdIn) {
		this.readFromStdIn = readFromStdIn;
	}

	public static void main(String[] args) {
		boolean readFromStdIn = false;
		for (String arg : args) {
			readFromStdIn = readFromStdIn || "--stdin".equalsIgnoreCase(arg);
		}
		new App(readFromStdIn).start();
	}


	private Set<String> readFromCrawlers() {
		ProxyListDownloaderEngine e = new ProxyListDownloaderEngine();
		return e.fetchProxyList();
	}

	private Set<String> readFromStdIn() {
		// TODO test and store ip:port list from stdin
		return null;
	}

	private void start() {
		Set<String> proxies = new HashSet<>();
		if (readFromStdIn) {
			proxies.addAll(readFromStdIn());
		} else {
			proxies.addAll(readFromCrawlers());
		}
	}
}
