package hu.juzraai.proxymanager.batch;

import hu.juzraai.proxymanager.batch.filter.ValidProxyFilter;
import hu.juzraai.proxymanager.batch.mapper.IpPortProxyMapper;
import hu.juzraai.proxymanager.batch.reader.StdinProxyReader;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.dispatcher.PoisonRecordBroadcaster;
import org.easybatch.core.dispatcher.RoundRobinRecordDispatcher;
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
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Zsolt Jurányi
 */
public class ProxyEngine implements Callable<Void> {

	private static final Logger L = LoggerFactory.getLogger(ProxyEngine.class);
	private static final int WORKERS = 5; // TODO GetCommand.threads

	private final GetCommand params;
	private final ProxyDatabase db;
	private final List<BlockingQueue<Record>> queues = new ArrayList<>();

	public ProxyEngine(GetCommand params, ProxyDatabase db) {
		this.params = params;
		this.db = db;
	}

	@Override
	public Void call() throws Exception {

		L.info("Initializing engine");
		List<Job> jobs = new ArrayList<>();
		for (int i = 0; i < WORKERS; i++) {
			L.trace("Creating worker #{}", i);
			jobs.add(createWorker());
		}

		L.trace("Creating master job");
		jobs.add(0, createMasterJob()); // after creating queues!

		L.info("Starting engine");
		ExecutorService executorService = Executors.newFixedThreadPool(1 + WORKERS);
		List<Future<JobReport>> futureReports = executorService.invokeAll(jobs);
		List<JobReport> reports = new ArrayList<>();
		for (Future<JobReport> fr : futureReports) {
			reports.add(fr.get());
		}
		reports.remove(0); // we don't need master job's metrics - they contain additional PRs...

		L.info("Generating report");
		JobReportMerger reportMerger = new DefaultJobReportMerger();
		JobReport finalReport = reportMerger.mergerReports(reports.toArray(new JobReport[]{}));
		// TODO poison records are also counted in report... they shouldn't be...

		System.out.println(finalReport);

		L.info("Engine shutting down");
		executorService.shutdown();
		return null;
	}

	private Job createMasterJob() {
		return JobBuilder.aNewJob()
				.silentMode(true)
				.named("proxy-engine")
				.reader(createReader())
				.filter(new ValidProxyFilter())
				.mapper(new IpPortProxyMapper())
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
