package hu.juzraai.proxymanager;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import hu.juzraai.proxymanager.batch.ProxyEngine;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.cli.MainParameters;
import hu.juzraai.proxymanager.cli.StatCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.fetch.ProxyListDownloaderEngine;
import hu.juzraai.proxymanager.util.ProxyValidator;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.apache.log4j.Level;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class App {

	private static final Logger L = LoggerFactory.getLogger(App.class);

	// TODO Proxy.notWorkingSince

	// TODO easy batch (why? why not :D)
	// RecordReader: reader db->ProxyTest
	// Filter: filter freshly checked
	// Filter: long time not working (notWorkingSince > 24h)
	// Proc: test, update test info in model
	// Writer: write into db

	// TODO site stat
	// query db, generate ProxyListInfo
	// we can query new proxies from last crawl: firstFetched = lastFetched = MAX(lastFetched)

	// TODO crawling: do not fetch a proxy site more than one in a minute! (ProxyListInfo.lastFetched)

	// TODO checkfornull, nonnull
	private final boolean readFromStdIn;
	private final ProxyDatabase db;

	public App(boolean readFromStdIn, ProxyDatabase db) {
		this.readFromStdIn = readFromStdIn;
		this.db = db;
	}

	public static void main(String[] args) throws Exception {
		org.apache.log4j.Logger.getLogger("hu.juzraai.toolbox").setLevel(Level.WARN);
		org.apache.log4j.Logger.getLogger("com.j256.ormlite").setLevel(Level.WARN);

		MainParameters main = new MainParameters();
		GetCommand get = new GetCommand();
		StatCommand stat = new StatCommand();

		JCommander jc = new JCommander(main);
		jc.addCommand(get);
		jc.addCommand(stat);
		try {
			jc.parse(args);
			String cmd = jc.getParsedCommand();

			if ("get".equalsIgnoreCase(cmd)) {
				try (ProxyDatabase db = ProxyDatabase.build(new File(main.getDatabaseFile()))) {
					new ProxyEngine(get, db).call();
				}
			} else if ("stat".equalsIgnoreCase(cmd)) {
				// TRY-W-R ProxyDatabase db = ProxyDatabase.build(new File(main.getDatabaseFile()));
				// TODO
				// pass StatCommand and ProxyDatabase
			} else {
				jc.usage();
			}
		} catch (ParameterException e) {
			System.out.println("ERROR: " + e.getMessage() + "\n");
			jc.setProgramName("java -jar proxy-manager-VERSION.jar");
			jc.usage();
		}

//		boolean readFromStdIn = false;
//		File dbFile = null;
//		for (String arg : args) {
//			readFromStdIn = readFromStdIn || "--stdin".equalsIgnoreCase(arg);
//			// TODO override dbFile
//		}
//		ProxyDatabase db = ProxyDatabase.build(dbFile);
//		new App(readFromStdIn, db).start();
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
		db.storeNewProxies(proxies);

		// TODO run easy batch: test ALL proxies in db
	}

}
