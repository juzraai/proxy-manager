package hu.juzraai.proxymanager.batch.reader;

import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.reader.RecordReaderClosingException;
import org.easybatch.core.reader.RecordReaderOpeningException;
import org.easybatch.core.reader.RecordReadingException;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.PoisonRecord;
import org.easybatch.core.record.Record;
import org.easybatch.core.record.StringRecord;

import java.util.Date;
import java.util.Scanner;

/**
 * @author Zsolt Jurányi
 */
public class StdinProxyReader implements RecordReader {

	private Scanner scanner;
	private long recordNumber;
	private boolean stop = false;

	@Override
	public void close() throws RecordReaderClosingException {
		scanner.close();
	}

	@Override
	public String getDataSourceName() {
		return "stdin";
	}

	@Override
	public Long getTotalRecords() {
		return null;
	}

	@Override
	public boolean hasNextRecord() {
		return !stop;
	}

	@Override
	public void open() throws RecordReaderOpeningException {
		scanner = new Scanner(System.in);
	}

	@Override
	public Record readNextRecord() throws RecordReadingException {
		String line = null;
		if (scanner.hasNextLine()) {
			line = scanner.nextLine();
		}
		if (null == line || line.isEmpty()) {
			stop = true;
			return new PoisonRecord();
		} else {
			Header header = new Header(++recordNumber, getDataSourceName(), new Date());
			return new StringRecord(header, line);
		}
	}
}
