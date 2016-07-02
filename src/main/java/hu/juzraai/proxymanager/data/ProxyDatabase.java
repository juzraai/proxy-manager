package hu.juzraai.proxymanager.data;

import hu.juzraai.toolbox.data.OrmLiteDatabase;
import hu.juzraai.toolbox.jdbc.ConnectionString;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author Zsolt Jurányi
 */
public class ProxyDatabase implements Closeable {

	private static final Logger L = LoggerFactory.getLogger(ProxyDatabase.class);
	private static final File DEFAULT_FILE = new File("proxies.db");

	private final OrmLiteDatabase db;

	private ProxyDatabase(OrmLiteDatabase db) {
		this.db = db;
	}

	public static ProxyDatabase build() throws SQLException {
		return build(DEFAULT_FILE);
	}

	public static ProxyDatabase build(File f) throws SQLException {
		if (null == f) {
			f = DEFAULT_FILE;
		}
		L.info("Connecting to SQLite database in file: {}", f.getAbsolutePath());
		OrmLiteDatabase db = OrmLiteDatabase.build(ConnectionString.SQLITE().databaseFile(f).build(), null, null);
		db.createTables(ProxyTestInfo.class);
		// TODO build tables
		return new ProxyDatabase(db);
	}

	@Override
	public void close() throws IOException {
		db.close();
	}

	public void storeNewProxies(Set<String> proxies) throws SQLException {
		L.info("Storing new proxies");
		int c = 0;
		for (String proxy : proxies) {
			ProxyTestInfo pti = db.fetch(ProxyTestInfo.class, proxy);
			if (null == pti) {
				pti = new ProxyTestInfo(proxy);
				db.store(pti);
				c++;
			}
		}
		L.info("{} new proxies stored", c);
	}
}
