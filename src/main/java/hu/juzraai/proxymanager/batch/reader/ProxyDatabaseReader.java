package hu.juzraai.proxymanager.batch.reader;

import com.j256.ormlite.dao.CloseableIterator;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.reader.RecordReaderClosingException;
import org.easybatch.core.reader.RecordReaderOpeningException;
import org.easybatch.core.reader.RecordReadingException;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.StringRecord;
import org.slf4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Date;

/**
 * Reads proxy list from the database and produces {@link StringRecord} objects.
 *
 * @author Zsolt Jurányi
 */
public class ProxyDatabaseReader implements RecordReader {

	private static final Logger L = LoggerFactory.getLogger(ProxyDatabaseReader.class);
	private final ProxyDatabase db;
	private CloseableIterator<ProxyTestInfo> i;
	private long recordNumber;

	/**
	 * Creates a new instance.
	 *
	 * @param db {@link ProxyDatabase} to read
	 */
	public ProxyDatabaseReader(@Nonnull ProxyDatabase db) {
		this.db = db;
	}

	/**
	 * Closes the iterator used to read proxies from the database.
	 *
	 * @throws RecordReaderClosingException
	 */
	@Override
	public void close() throws RecordReaderClosingException {
		try {
			i.close();
		} catch (SQLException e) {
			String m = "Failed to close iterator for proxies in database";
			L.error(m, e);
			throw new RecordReaderClosingException(m, e);
		}
	}

	/**
	 * @return Name of the datasource
	 */
	@Nonnull
	@Override
	public String getDataSourceName() {
		return "database";
	}

	/**
	 * @return Total number of {@link ProxyTestInfo} records in the database
	 */
	@CheckForNull
	@Override
	public Long getTotalRecords() {
		try {
			return db.getDb().dao(ProxyTestInfo.class).countOf();
		} catch (SQLException e) {
			L.error("Failed to query proxy count from database", e);
			return null;
		}
	}

	/**
	 * @return <code>true</code> if there are more records to read or
	 * <code>false</code> otherwise
	 */
	@Override
	public boolean hasNextRecord() {
		return i.hasNext();
	}

	/**
	 * Initializes the {@link ProxyTestInfo} iterator to be able to read records
	 * from the database.
	 *
	 * @throws RecordReaderOpeningException
	 */
	@Override
	public void open() throws RecordReaderOpeningException {
		recordNumber = 0;
		try {
			i = db.getDb().dao(ProxyTestInfo.class).iterator();
		} catch (SQLException e) {
			String m = "Failed to open iterator for proxies in database";
			L.error(m, e);
			throw new RecordReaderOpeningException(m, e);
		}
	}

	/**
	 * Reads the next {@link ProxyTestInfo} record from the database and
	 * produces a {@link StringRecord}.
	 *
	 * @return A {@link StringRecord} which has the <code>ipPort</code> field of
	 * the {@link ProxyTestInfo} object as payload
	 * @throws RecordReadingException
	 */
	@Nonnull
	@Override
	public StringRecord readNextRecord() throws RecordReadingException {
		ProxyTestInfo proxy = i.next();
		Header header = new Header(++recordNumber, getDataSourceName(), new Date());
		return new StringRecord(header, proxy.getIpPort());
	}
}
