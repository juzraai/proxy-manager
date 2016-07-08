package hu.juzraai.proxymanager.batch;

import hu.juzraai.proxymanager.batch.reader.StdinProxyReader;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import org.easybatch.core.dispatcher.PoisonRecordBroadcaster;
import org.easybatch.core.dispatcher.RoundRobinRecordDispatcher;
import org.easybatch.core.filter.EmptyRecordFilter;
import org.easybatch.core.filter.PoisonRecordFilter;
import org.easybatch.core.job.Job;
import org.easybatch.core.job.JobBuilder;
import org.easybatch.core.job.JobReport;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.StandardOutputRecordWriter;
import org.easybatch.tools.reporting.DefaultJobReportMerger;
import org.easybatch.tools.reporting.JobReportMerger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Zsolt Jurányi
 */
public class ProxyEngine implements Callable<Void> {

	private static final int WORKERS = 5;

	private final GetCommand params;
	private final ProxyDatabase db;
	private final List<BlockingQueue<Record>> queues = new ArrayList<>();

	public ProxyEngine(GetCommand params, ProxyDatabase db) {
		this.params = params;
		this.db = db;
	}

	@Override
	public Void call() throws Exception {

		List<Job> jobs = new ArrayList<>();
		for (int i = 0; i < WORKERS; i++) {
			jobs.add(createWorker());
		}
		jobs.add(0, createMasterJob()); // after creating queues!

		ExecutorService executorService = Executors.newFixedThreadPool(1 + WORKERS);
		List<Future<JobReport>> futureReports = executorService.invokeAll(jobs);
		List<JobReport> reports = new ArrayList<>();
		for (Future<JobReport> fr : futureReports) {
			reports.add(fr.get());
		}
		reports.remove(0);
		JobReportMerger reportMerger = new DefaultJobReportMerger();
		JobReport finalReport = reportMerger.mergerReports(reports.toArray(new JobReport[]{}));
		// TODO poison records are also counted in report... they shouldn't be...

		System.out.println(finalReport);

		executorService.shutdown();

		return null;
	}

	private Job createMasterJob() {
		return JobBuilder.aNewJob()
				.silentMode(true)
				.named("proxy-engine")
				.reader(createReader())
				.filter(new EmptyRecordFilter()) // TODO filter for valid proxies
				.dispatcher(new RoundRobinRecordDispatcher<>(queues))
				.jobListener(new PoisonRecordBroadcaster<>(queues))
				.build();
	}

	private RecordReader createReader() {
		if (GetCommand.Input.STDIN == params.getInput()) {
			return new StdinProxyReader();
		}
		return null;
	}

	private Job createWorker() {
		BlockingQueue<Record> q = new LinkedBlockingQueue<>();
		queues.add(q);
		return JobBuilder.aNewJob()
				.silentMode(true)
				.named("proxy-engine-worker-" + queues.size())
				.reader(new BlockingQueueRecordReader(q))
				.filter(new PoisonRecordFilter())
				// TODO tester: throw error when test site / connection is dead
				// TODO filter: filter out non working proxies
				.writer(new StandardOutputRecordWriter())
				.build();
	}

	public GetCommand getParams() {
		return params;
	}
}
