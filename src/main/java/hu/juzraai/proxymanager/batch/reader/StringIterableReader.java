package hu.juzraai.proxymanager.batch.reader;

import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.reader.RecordReaderClosingException;
import org.easybatch.core.reader.RecordReaderOpeningException;
import org.easybatch.core.reader.RecordReadingException;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.StringRecord;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Iterator;

/**
 * Reads an <code>Iterable&lt;String&gt;</code> object and produces {@link
 * StringRecord} instances.
 *
 * @author Zsolt Jurányi
 */
public class StringIterableReader implements RecordReader {

	private final Iterator<String> i;
	private long recordNumber;

	/**
	 * Creates a new instance.
	 *
	 * @param c The {@link Iterable} to read
	 */
	public StringIterableReader(@Nonnull Iterable<String> c) {
		i = c.iterator();
	}

	/**
	 * Does absolutely nothing.
	 *
	 * @throws RecordReaderClosingException
	 */
	@Override
	public void close() throws RecordReaderClosingException {
	}

	/**
	 * @return Name of the datasource
	 */
	@Nonnull
	@Override
	public String getDataSourceName() {
		return "collection-reader";
	}

	/**
	 * @return Return <code>null</code> because total record count is not known
	 */
	@CheckForNull
	@Override
	public Long getTotalRecords() {
		return null;
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
	 * Initializes the reader by setting record counter to 0.
	 *
	 * @throws RecordReaderOpeningException
	 */
	@Override
	public void open() throws RecordReaderOpeningException {
		recordNumber = 0;
	}

	/**
	 * Reads the next element from the {@link Iterable} and produces a {@link
	 * StringRecord}.
	 *
	 * @return A {@link StringRecord} which has the element read from the {@link
	 * Iterable} as payload
	 * @throws RecordReadingException
	 */
	@Nonnull
	@Override
	public StringRecord readNextRecord() throws RecordReadingException {
		Header header = new Header(++recordNumber, getDataSourceName(), new Date());
		return new StringRecord(header, i.next());
	}
}
