package hu.juzraai.proxymanager.stat;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import hu.juzraai.proxymanager.cli.StatCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.data.ProxyListInfo;
import hu.juzraai.proxymanager.data.ProxySourceInfo;
import hu.juzraai.proxymanager.data.TableNames;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Zsolt Jurányi
 */
public class ProxyListStatsGenerator implements Callable<List<ProxyListInfo>> {

	private static final Logger L = LoggerFactory.getLogger(ProxyListStatsGenerator.class);

	private final StatCommand params;
	private final ProxyDatabase db;

	public ProxyListStatsGenerator(StatCommand params, ProxyDatabase db) {
		this.params = params;
		this.db = db;
	}

	@Nonnull
	@Override
	public List<ProxyListInfo> call() throws Exception {
		List<ProxyListInfo> plis = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT s.source, COUNT(s.ipPort), SUM(t.working), SUM(t.anon), MAX(s.lastFetched), sh.shared FROM ");
		sql.append(TableNames.PROXY_SOURCE_INFO).append(" s INNER JOIN ");
		sql.append(TableNames.PROXY_TEST_INFO).append(" t ON t.ipPort = s.ipPort INNER JOIN ");
		sql.append("(SELECT source, COUNT(DISTINCT ipPort) shared FROM ");
		sql.append("(SELECT ss.source, ss.ipPort, (SELECT COUNT(*) FROM ");
		sql.append(TableNames.PROXY_SOURCE_INFO).append(" sss WHERE sss.ipPort = ss.ipPort) c FROM ");
		sql.append(TableNames.PROXY_SOURCE_INFO).append(" ss WHERE c > 1");
		sql.append(") h GROUP BY source ");
		sql.append(") sh ON sh.source = s.source ");
		sql.append("GROUP BY s.source");

		L.info("Querying stats");
		GenericRawResults<ProxyListInfo> r = db.getDb().dao(ProxySourceInfo.class).queryRaw(sql.toString(), new RawRowMapper<ProxyListInfo>() {
			@Override
			public ProxyListInfo mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
				// resultColumns: source, count, working, anon
				ProxyListInfo pli = new ProxyListInfo();
				pli.setUrl(resultColumns[0]); // TODO rename URL to name + introduce name in PLD
				pli.setAnonWorkingProxyCount(Integer.parseInt(resultColumns[3]));
				pli.setLastFetched(new Date(Long.parseLong(resultColumns[4])));
				pli.setProxyCount(Integer.parseInt(resultColumns[1]));
				pli.setSharedProxyCount(Integer.parseInt(resultColumns[5]));
				pli.setWorkingProxyCount(Integer.parseInt(resultColumns[2]));
				return pli;
			}
		});

		plis.addAll(r.getResults());
		printPLIs(plis);

		L.info("Storing stats");
		for (ProxyListInfo pli : plis) {
			try {
				db.getDb().store(pli);
			} catch (SQLException e) {
				L.error("Failed to store proxy list info", e);
			}
		}
		L.info("Done");

		return plis;
	}

	public ProxyDatabase getDb() {
		return db;
	}

	public StatCommand getParams() {
		return params;
	}

	private void printPLIs(List<ProxyListInfo> plis) {
		int maxLen = 0;
		for (ProxyListInfo pli : plis) {
			maxLen = Math.max(maxLen, pli.getUrl().length());
		}
		System.out.printf("%-" + maxLen + "s | proxies | shared | working | anon | last fetched\n", "source");
		for (ProxyListInfo pli : plis) {
			System.out.printf("%-" + maxLen + "s | %7d | %6d | %7d | %4d | %s\n",
					pli.getUrl(),
					pli.getProxyCount(),
					pli.getSharedProxyCount(),
					pli.getWorkingProxyCount(),
					pli.getAnonWorkingProxyCount(),
					pli.getLastFetched());
		}
	}


}
