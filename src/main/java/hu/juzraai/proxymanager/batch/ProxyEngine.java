package hu.juzraai.proxymanager.batch;

import hu.juzraai.proxymanager.batch.filter.ValidProxyFilter;
import hu.juzraai.proxymanager.batch.filter.WorkingProxyFilter;
import hu.juzraai.proxymanager.batch.mapper.IpPortProxyMapper;
import hu.juzraai.proxymanager.batch.processor.ProxyInfoFetcherProcessor;
import hu.juzraai.proxymanager.batch.processor.ProxyTesterProcessor;
import hu.juzraai.proxymanager.batch.reader.StdinProxyReader;
import hu.juzraai.proxymanager.batch.writer.ProxyDatabaseWriter;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.test.ProxyServerPrivacyDotComProxyTester;
import hu.juzraai.toolbox.cache.FileCacheForStrings;
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
import org.easybatch.tools.reporting.HtmlJobReportFormatter;
import org.easybatch.tools.reporting.JobReportMerger;
import org.slf4j.Logger;

import java.io.File;
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
		// TODO poison records are also counted in report... they shouldn't be...
		JobReportMerger reportMerger = new DefaultJobReportMerger();
		JobReport finalReport = reportMerger.mergerReports(reports.toArray(new JobReport[]{}));
		String htmlReport = new HtmlJobReportFormatter().formatReport(finalReport);
		new FileCacheForStrings(new File(".")).store("report.html", htmlReport);

		System.out.println(finalReport);

		L.info("Engine shutting down");
		executorService.shutdown();
		return null;
	}

	protected Job createMasterJob() {
		return JobBuilder.aNewJob()
				.silentMode(true)
				.named("proxy-engine")
				.reader(createReader())
				.filter(new ValidProxyFilter())
				.mapper(new IpPortProxyMapper())
				.processor(new ProxyInfoFetcherProcessor(db))
				.dispatcher(new RoundRobinRecordDispatcher<>(queues))
				.jobListener(new PoisonRecordBroadcaster<>(queues))
				.build();
	}

	protected RecordReader createReader() {
		if (GetCommand.Input.STDIN == params.getInput()) {
			return new StdinProxyReader();
		}
		return null;
	}

	protected Job createWorker() {
		BlockingQueue<Record> q = new LinkedBlockingQueue<>();
		queues.add(q);
		return JobBuilder.aNewJob()
				.silentMode(true)
				.named("proxy-engine-worker-" + queues.size())
				.reader(new BlockingQueueRecordReader(q))
				.filter(new PoisonRecordFilter())
				// TODO add tester only if GetCommand.test <> NONE
				// TODO also pass mode to tester: AUTO or ALL
				.processor(new ProxyTesterProcessor(new ProxyServerPrivacyDotComProxyTester()))
				.writer(new ProxyDatabaseWriter(db))
				.filter(new WorkingProxyFilter())
				// TODO anon filter - if needed
				.writer(new StandardOutputRecordWriter())
				.build();
	}

	public ProxyDatabase getDb() {
		return db;
	}

	public GetCommand getParams() {
		return params;
	}
}
