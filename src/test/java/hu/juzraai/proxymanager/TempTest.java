package hu.juzraai.proxymanager;

import hu.juzraai.proxymanager.batch.ProxyEngine;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.fetch.ProxyNovaDotComPLD;
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
		Logger.getRootLogger().setLevel(Level.INFO);
		org.apache.log4j.Logger.getLogger("hu.juzraai.toolbox").setLevel(Level.WARN);
		org.apache.log4j.Logger.getLogger("com.j256.ormlite").setLevel(Level.WARN);
		System.out.println("init");
		try (ProxyDatabase db = ProxyDatabase.build()) {
			GetCommand get = new GetCommand();
			get.setInput(GetCommand.Input.CRAWL);
			get.setTest(GetCommand.Test.AUTO);
			final ProxyEngine e = new ProxyEngine(get, db, false);
			e.getProxyListDownloaders().clear();
			e.getProxyListDownloaders().add(new ProxyNovaDotComPLD());
			System.out.println("start");
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						e.call();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}).start();
			System.out.println("results");
			if (null != e.getOutput()) {
				String p = null;
				while (!ProxyEngine.POISON_RECORD.equals((p = e.getOutput().take()))) {
					System.out.println("- " + p);
				}
			}
		}

	}
}
