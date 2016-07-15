package hu.juzraai.proxymanager;

import hu.juzraai.proxymanager.batch.ProxyEngine;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.fetch.ProxyListDownloaderTask;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Zsolt Jurányi
 */
public class TempTest {

	@Test
	@Ignore
	public void t() throws Exception {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		org.apache.log4j.Logger.getLogger("hu.juzraai.toolbox").setLevel(Level.WARN);
		org.apache.log4j.Logger.getLogger("com.j256.ormlite").setLevel(Level.WARN);
		ProxyDatabase db = ProxyDatabase.build();
		GetCommand get = new GetCommand();
		get.setInput(GetCommand.Input.CRAWL);
		get.setTest(GetCommand.Test.AUTO);
		ProxyEngine e = new ProxyEngine(get, db);
		ProxyListDownloaderTask c = e.getProxyListDownloaders().get(0);
		e.getProxyListDownloaders().clear();
		e.getProxyListDownloaders().add(c);
		e.call();
	}
}
