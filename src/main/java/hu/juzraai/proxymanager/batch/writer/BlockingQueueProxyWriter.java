package hu.juzraai.proxymanager.batch.writer;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.writer.RecordWriter;
import org.easybatch.core.writer.RecordWritingException;
import org.slf4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * @author Zsolt Jurányi
 */
public class BlockingQueueProxyWriter implements RecordWriter<ProxyRecord> {

	private static final Logger L = LoggerFactory.getLogger(BlockingQueueProxyWriter.class);

	private final BlockingQueue<String> queue;

	public BlockingQueueProxyWriter(BlockingQueue<String> queue) {
		this.queue = queue;
	}

	public BlockingQueue<String> getQueue() {
		return queue;
	}

	@Override
	public ProxyRecord processRecord(ProxyRecord record) throws RecordWritingException {
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
