package hu.juzraai.proxymanager;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import hu.juzraai.proxymanager.batch.ProxyEngine;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.cli.MainParameters;
import hu.juzraai.proxymanager.cli.StatCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.apache.log4j.Level;
import org.slf4j.Logger;

import java.io.File;

/**
 * @author Zsolt Jurányi
 */
public class App {

	private static final Logger L = LoggerFactory.getLogger(App.class);

	// TODO ? Proxy.notWorkingSince
	// TODO ? crawling: do not fetch a proxy site more than one in a minute! (ProxyListInfo.lastFetched)
	// TODO site stat
	// query db, generate ProxyListInfo
	// we can query new proxies from last crawl: firstFetched = lastFetched = MAX(lastFetched)

	// TODO checkfornull, nonnull

	public static void main(String[] args) throws Exception {
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.TRACE);
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
	}

}
