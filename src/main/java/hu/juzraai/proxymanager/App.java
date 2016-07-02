package hu.juzraai.proxymanager;

import hu.juzraai.proxymanager.fetch.ProxyListDownloaderEngine;
import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class App {
	// TODO cli
	// --stdout: print out working proxies
	// --anon: print out only anon proxies

	// TODO log should be redirected to file, at least when using --stdout

	// TODO crawlers
	// http://www.idcloak.com/proxylist/free-proxy-ip-list.html
	// http://www.proxylist.ro/anonymous-proxy-list-filtered-by-privacy-free-proxy-servers.html
	// http://www.proxynova.com/proxy-server-list/
	// https://www.torvpn.com/en/proxy-list
	// http://gatherproxy.com/proxylist/anonymity/?t=Elite
	// http://proxylist.hidemyass.com - obfuscated

	// TODO Proxy.notWorkingSince

	// TODO easy batch
	// RecordReader: read stdin->ProxyTest / read db->ProxyTest
	// Filter: filter freshly checked
	// Filter: long time not working (notWorkingSince > 24h)
	// Proc: test, update test info in model
	// Writer: write into db

	// TODO site stat
	// query db, generate ProxyListInfo

	// TODO crawling: do not fetch a proxy site more than one in a minute! (ProxyListInfo.lastFetched)

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
		Set<String> proxies = new HashSet<>();

		L.info("Reading proxies from stdin (press Ctrl+D to stop)");
		try (Scanner s = new Scanner(System.in)) {
			while (s.hasNextLine()) {
				String proxy = s.nextLine();
				if (ProxyValidator.isValidIpPort(proxy)) {
					proxies.add(proxy);
				}
			}
		}

		L.info("Read {} unique proxies from stdin", proxies.size());
		return proxies;
	}

	private void start() {
		Set<String> proxies = new HashSet<>();
		if (readFromStdIn) {
			proxies.addAll(readFromStdIn());
		} else {
			proxies.addAll(readFromCrawlers());
		}
		// TODO grab+update/create proxy test info

		// TODO run easy batch: test ALL proxies in db
	}
}
