package hu.juzraai.proxymanager.batch.processor;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import org.easybatch.core.processor.RecordProcessingException;
import org.easybatch.core.processor.RecordProcessor;

import java.sql.SQLException;

/**
 * @author Zsolt Jurányi
 */
public class ProxyInfoFetcherProcessor implements RecordProcessor<ProxyRecord, ProxyRecord> {

	private final ProxyDatabase db;

	public ProxyInfoFetcherProcessor(ProxyDatabase db) {
		this.db = db;
	}

	@Override
	public ProxyRecord processRecord(ProxyRecord proxyRecord) throws RecordProcessingException {
		try {
			ProxyTestInfo proxyFromDb = db.getDb().fetch(ProxyTestInfo.class, proxyRecord.getPayload().getId());
			if (null != proxyFromDb) {
				proxyRecord = new ProxyRecord(proxyRecord.getHeader(), proxyFromDb);
			}
		} catch (SQLException e) {
			throw new RecordProcessingException("Failed to fetch proxy info for database", e);
		}
		return proxyRecord;
	}
}
