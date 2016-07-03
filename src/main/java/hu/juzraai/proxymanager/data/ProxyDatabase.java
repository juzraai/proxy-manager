package hu.juzraai.proxymanager.data;

import hu.juzraai.toolbox.data.OrmLiteDatabase;
import hu.juzraai.toolbox.hash.MD5;
import hu.juzraai.toolbox.jdbc.ConnectionString;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
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
		db.createTables(ProxyTestInfo.class, ProxySourceInfo.class);
		// TODO build tables
		return new ProxyDatabase(db);
	}

	@Override
	public synchronized void close() throws IOException {
		db.close();
	}

	public synchronized void storeNewProxies(Set<String> proxies) {
		L.info("Storing new proxies");
		int newProxies = 0;
		for (String proxy : proxies) {
			try {
				ProxyTestInfo pti = db.fetch(ProxyTestInfo.class, proxy);
				if (null == pti) {
					pti = new ProxyTestInfo(proxy);
					db.store(pti);
					newProxies++;
				}
			} catch (SQLException e) {
				L.error("Could not fetch/store proxy", e);
			}
		}
		L.info("{} new proxies stored", newProxies);
	}

	public synchronized void storeProxySourceInfo(Set<String> proxies, String crawlerName, Date timestamp) {
		L.info("Storing proxy source info for crawler: {}", crawlerName);
		for (String proxy : proxies) {
			try {
				String id = String.format("%s/%s", proxy, MD5.fromString(crawlerName));
				ProxySourceInfo psi = db.fetch(ProxySourceInfo.class, id);
				if (null == psi) {
					psi = new ProxySourceInfo();
					psi.setId(id);
					psi.setIpPort(proxy);
					psi.setSource(crawlerName);
					psi.setFirstFetched(timestamp.getTime());
				}
				psi.setLastFetched(timestamp.getTime());
				db.store(psi);
			} catch (SQLException e) {
				L.error("Could not fetch/store proxy source info", e);
			} catch (NoSuchAlgorithmException e) {
				L.error("Could not create MD5 hash, giving up");
				break;
			}
		}
	}
}
