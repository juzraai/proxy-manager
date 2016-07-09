package hu.juzraai.proxymanager;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import hu.juzraai.proxymanager.batch.ProxyEngine;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.cli.MainParameters;
import hu.juzraai.proxymanager.cli.StatCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

import java.io.File;

/**
 * @author Zsolt Jurányi
 */
public class App {

	// TODO ? Proxy.notWorkingSince
	// TODO ? crawling: do not fetch a proxy site more than one in a minute! (ProxyListInfo.lastFetched)
	// TODO site stat
	// query db, generate ProxyListInfo
	// we can query new proxies from last crawl: firstFetched = lastFetched = MAX(lastFetched)

	// TODO checkfornull, nonnull

	public static void main(String[] args) throws Exception {

		// override Toolbox 16.07 log settings

		org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
		Layout layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} [%-5p] [%-10t] %c{1}:%L >> %m%n");
		org.apache.log4j.Logger.getRootLogger().addAppender(new FileAppender(layout, "proxy-manager.log", true, true, 1024 * 8));
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
		org.apache.log4j.Logger.getLogger("hu.juzraai.toolbox").setLevel(Level.WARN);
		org.apache.log4j.Logger.getLogger("com.j256.ormlite").setLevel(Level.WARN);

		// command line argument parsing

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
