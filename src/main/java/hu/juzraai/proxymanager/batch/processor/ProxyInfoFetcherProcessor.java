package hu.juzraai.proxymanager.batch.processor;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.processor.RecordProcessingException;
import org.easybatch.core.processor.RecordProcessor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.SQLException;

/**
 * Queries proxy information from the database and replaces the {@link
 * ProxyTestInfo} payload object in {@link ProxyRecord}.
 *
 * @author Zsolt Jurányi
 */
public class ProxyInfoFetcherProcessor implements RecordProcessor<ProxyRecord, ProxyRecord> {

	private static final Logger L = LoggerFactory.getLogger(ProxyInfoFetcherProcessor.class);

	private final ProxyDatabase db;

	/**
	 * Creates a new instance.
	 *
	 * @param db {@link ProxyDatabase} to use for querying
	 */
	public ProxyInfoFetcherProcessor(@Nonnull ProxyDatabase db) {
		this.db = db;
	}

	/**
	 * Queries proxy information from the database and replaces the {@link
	 * ProxyTestInfo} payload object in {@link ProxyRecord}.
	 *
	 * @param record {@link ProxyRecord} to be replaced. It should have its
	 *                    <code>ipPort</code> field filled
	 * @return A new {@link ProxyRecord} with the queried {@link ProxyTestInfo}
	 * as payload or the original record if there were no such proxy in the
	 * database
	 * @throws RecordProcessingException
	 */
	@Nonnull
	@Override
	public ProxyRecord processRecord(@Nonnull ProxyRecord record) throws RecordProcessingException {
		try {
			L.trace("Querying proxy information from database: {}", record.getPayload().getId());
			ProxyTestInfo proxyFromDb = db.getDb().fetch(ProxyTestInfo.class, record.getPayload().getId());
			if (null != proxyFromDb) {
				L.trace("Found proxy information in database: {}", proxyFromDb);
				record = new ProxyRecord(record.getHeader(), proxyFromDb);
			}
		} catch (SQLException e) {
			String m = String.format("Failed to fetch proxy info from database: %s", record.getPayload().getId());
			L.error(m, e);
			throw new RecordProcessingException(m, e);
		}
		return record;
	}
}
