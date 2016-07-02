package hu.juzraai.proxymanager;

import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.fetch.ProxyListDownloaderEngine;
import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.apache.log4j.Level;
import org.slf4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class App {

	// TODO db - file-based SQLite
	// TODO SQLite filename can be modified via CLI arg?

	// TODO cli
	// --quick: skip download & test
	// --stdout: print out working proxies - or should it be automatic?
	// --anon: print out only anon proxies

	// TODO log should be redirected to file, at least when using --stdout
	// org.apache.log4j.Logger.getRootLogger().removeAppender("stdout");

	// TODO crawlers
	// http://www.idcloak.com/proxylist/free-proxy-ip-list.html
	// http://www.proxylist.ro/anonymous-proxy-list-filtered-by-privacy-free-proxy-servers.html
	// http://www.proxynova.com/proxy-server-list/
	// https://www.torvpn.com/en/proxy-list
	// http://gatherproxy.com/proxylist/anonymity/?t=Elite
	// http://proxylist.hidemyass.com - obfuscated

	// TODO Proxy.notWorkingSince

	// TODO easy batch (why? why not :D)
	// RecordReader: read stdin->ProxyTest / read db->ProxyTest
	// Filter: filter freshly checked
	// Filter: long time not working (notWorkingSince > 24h)
	// Proc: test, update test info in model
	// Writer: write into db

	// TODO site stat
	// query db, generate ProxyListInfo
	// we can query new proxies from last crawl: firstFetched = lastFetched = MAX(lastFetched)

	// TODO crawling: do not fetch a proxy site more than one in a minute! (ProxyListInfo.lastFetched)

	// TODO checkfornull, nonnull

	private static final Logger L = LoggerFactory.getLogger(App.class);

	private final boolean readFromStdIn;
	private final ProxyDatabase db;

	public App(boolean readFromStdIn, ProxyDatabase db) {
		this.readFromStdIn = readFromStdIn;
		this.db = db;
	}

	public static void main(String[] args) throws SQLException {
		org.apache.log4j.Logger.getLogger("hu.juzraai.toolbox").setLevel(Level.WARN);
		org.apache.log4j.Logger.getLogger("com.j256.ormlite").setLevel(Level.WARN);
		boolean readFromStdIn = false;
		File dbFile = null;
		for (String arg : args) {
			readFromStdIn = readFromStdIn || "--stdin".equalsIgnoreCase(arg);
			// TODO override dbFile
		}
		ProxyDatabase db = ProxyDatabase.build(dbFile);
		new App(readFromStdIn, db).start();
	}


	private Set<String> readFromCrawlers() {
		ProxyListDownloaderEngine e = new ProxyListDownloaderEngine(db);
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

		try {
			db.storeNewProxies(proxies);
		} catch (SQLException e) {
			L.error("Couldn't store new proxies", e);
		}

		// TODO run easy batch: test ALL proxies in db
	}


}
