package hu.juzraai.proxymanager.batch.writer;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.writer.RecordWriter;
import org.easybatch.core.writer.RecordWritingException;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;

/**
 * Puts proxies coming from {@link ProxyRecord} into a {@link BlockingQueue} in
 * IP:PORT format.
 *
 * @author Zsolt Jurányi
 */
public class BlockingQueueProxyWriter implements RecordWriter<ProxyRecord> {

	private static final Logger L = LoggerFactory.getLogger(BlockingQueueProxyWriter.class);

	private final BlockingQueue<String> queue;

	/**
	 * Creates a new instance.
	 *
	 * @param queue The {@link BlockingQueue} to write
	 */
	public BlockingQueueProxyWriter(@Nonnull BlockingQueue<String> queue) {
		this.queue = queue;
	}

	/**
	 * @return The {@link BlockingQueue} which is used as output
	 */
	public BlockingQueue<String> getQueue() {
		return queue;
	}

	/**
	 * Puts <code>ipPort</code> field's value of {@link ProxyTestInfo} payload
	 * object into the queue.
	 *
	 * @param record {@link ProxyRecord} to read IP:PORT from
	 * @return The input record
	 * @throws RecordWritingException
	 */
	@Nonnull
	@Override
	public ProxyRecord processRecord(@Nonnull ProxyRecord record) throws RecordWritingException {
		try {
			queue.put(record.getPayload().getIpPort());
		} catch (InterruptedException e) {
			String m = "Queue put operation interrupted";
			L.error(m, e);
			Thread.currentThread().interrupt();
			throw new RecordWritingException(m, e);
		}
		return record;
	}
}
