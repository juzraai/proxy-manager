package hu.juzraai.proxymanager.batch.reader;

import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.reader.RecordReaderClosingException;
import org.easybatch.core.reader.RecordReaderOpeningException;
import org.easybatch.core.reader.RecordReadingException;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.StringRecord;

import java.util.Date;
import java.util.Iterator;

/**
 * @author Zsolt Jurányi
 */
public class StringIterableReader implements RecordReader {

	private final Iterator<String> i;
	private long currentRecordNumber;

	public StringIterableReader(Iterable<String> c) {
		i = c.iterator();
	}

	@Override
	public void close() throws RecordReaderClosingException {
	}

	@Override
	public String getDataSourceName() {
		return "collection-reader";
	}

	@Override
	public Long getTotalRecords() {
		return null;
	}

	@Override
	public boolean hasNextRecord() {
		return i.hasNext();
	}

	@Override
	public void open() throws RecordReaderOpeningException {
		currentRecordNumber = 0;
	}

	@Override
	public StringRecord readNextRecord() throws RecordReadingException {
		Header header = new Header(++currentRecordNumber, getDataSourceName(), new Date());
		return new StringRecord(header, i.next());
	}
}
