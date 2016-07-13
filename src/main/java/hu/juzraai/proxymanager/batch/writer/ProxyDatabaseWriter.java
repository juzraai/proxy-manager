package hu.juzraai.proxymanager.batch.writer;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.writer.RecordWriter;
import org.easybatch.core.writer.RecordWritingException;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.SQLException;

/**
 * @author Zsolt Jurányi
 */
public class ProxyDatabaseWriter implements RecordWriter<ProxyRecord> {

	private static final Logger L = LoggerFactory.getLogger(ProxyDatabaseWriter.class);

	private final ProxyDatabase db;

	public ProxyDatabaseWriter(@Nonnull ProxyDatabase db) {
		this.db = db;
	}

	@Nonnull
	@Override
	public ProxyRecord processRecord(@Nonnull ProxyRecord record) throws RecordWritingException {
		try {
			L.trace("Storing proxy: {}", record.getPayload());
			db.getDb().store(record.getPayload());
		} catch (SQLException e) {
			String m = "Failed to store proxy in database";
			L.error(m, e);
			throw new RecordWritingException(m, e);
		}
		return record;
	}
}
