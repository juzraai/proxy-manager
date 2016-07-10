package hu.juzraai.proxymanager.batch.reader;

import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.reader.RecordReaderClosingException;
import org.easybatch.core.reader.RecordReaderOpeningException;
import org.easybatch.core.reader.RecordReadingException;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.PoisonRecord;
import org.easybatch.core.record.Record;
import org.easybatch.core.record.StringRecord;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Scanner;

/**
 * Reads raw IP:PORT list from standard input. Stops on EOF or empty line.
 *
 * @author Zsolt Jurányi
 */
public class StdinReader implements RecordReader {

	private Scanner scanner;
	private long recordNumber;
	private boolean stop = false;

	/**
	 * Closes the {@link Scanner}.
	 *
	 * @throws RecordReaderClosingException
	 */
	@Override
	public void close() throws RecordReaderClosingException {
		scanner.close();
	}

	/**
	 * @return Name of the datasource
	 */
	@Nonnull
	@Override
	public String getDataSourceName() {
		return "stdin";
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
		return !stop;
	}

	/**
	 * Initializes the {@link Scanner} to read from standard input.
	 *
	 * @throws RecordReaderOpeningException
	 */
	@Override
	public void open() throws RecordReaderOpeningException {
		recordNumber = 0;
		scanner = new Scanner(System.in);
	}

	/**
	 * Reads the next line from standard input and produces a new {@link
	 * StringRecord}. If {@link Scanner} says there are no more lines or the
	 * current line is empty it produces a {@link PoisonRecord} instead.
	 *
	 * @return A {@link StringRecord} containing the next line read from input
	 * or a {@link PoisonRecord} if reached the end of input (or an empty line)
	 * @throws RecordReadingException
	 */
	@Nonnull
	@Override
	public Record readNextRecord() throws RecordReadingException {
		String line = null;
		if (scanner.hasNextLine()) {
			line = scanner.nextLine().trim();
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
