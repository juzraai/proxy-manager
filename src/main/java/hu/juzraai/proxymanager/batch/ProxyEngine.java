package hu.juzraai.proxymanager.batch;

import hu.juzraai.proxymanager.batch.filter.AnonProxyFilter;
import hu.juzraai.proxymanager.batch.filter.ValidProxyFilter;
import hu.juzraai.proxymanager.batch.filter.WorkingProxyFilter;
import hu.juzraai.proxymanager.batch.mapper.IpPortProxyMapper;
import hu.juzraai.proxymanager.batch.processor.ProxyInfoFetcherProcessor;
import hu.juzraai.proxymanager.batch.processor.ProxyTesterProcessor;
import hu.juzraai.proxymanager.batch.reader.ProxyDatabaseReader;
import hu.juzraai.proxymanager.batch.reader.StdinReader;
import hu.juzraai.proxymanager.batch.reader.StringIterableReader;
import hu.juzraai.proxymanager.batch.writer.ProxyDatabaseWriter;
import hu.juzraai.proxymanager.batch.writer.StdoutProxyWriter;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.fetch.ProxyListDownloaderEngine;
import hu.juzraai.proxymanager.test.ProxyServerPrivacyDotComProxyTester;
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
import org.easybatch.tools.reporting.DefaultJobReportMerger;
import org.easybatch.tools.reporting.JobReportMerger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static hu.juzraai.proxymanager.cli.GetCommand.Test.NONE;

/**
 * @author Zsolt Jurányi
 */
public class ProxyEngine implements Callable<Void> {

	// TODO gather working p? for use as lib? maybe Callable<List<String>> ? and bool gather constr arg?

	private static final Logger L = LoggerFactory.getLogger(ProxyEngine.class);

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
		for (int i = 0; i < params.getThreads(); i++) {
			L.trace("Creating worker #{}", i);
			jobs.add(createWorker());
		}

		L.trace("Creating master job");
		jobs.add(0, createMasterJob()); // after creating queues!

		L.info("Starting engine");
		ExecutorService executorService = Executors.newFixedThreadPool(1 + params.getThreads());
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
//		String htmlReport = new HtmlJobReportFormatter().formatReport(finalReport);
//		new FileCacheForStrings(new File(".")).store("report.html", htmlReport);

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
		switch (params.getInput()) {
			case STDIN:
				return new StdinReader();
			case CRAWL:
				ProxyListDownloaderEngine plde = new ProxyListDownloaderEngine(params.getThreads(), db);
				Set<String> proxies = plde.fetchProxyList();
				L.info("Got {} unique proxies from crawlers", proxies.size());
				return new StringIterableReader(proxies);
			case DB:
				return new ProxyDatabaseReader(db);
		}
		return null;
	}

	protected Job createWorker() {
		BlockingQueue<Record> q = new LinkedBlockingQueue<>();
		queues.add(q);

		JobBuilder builder = JobBuilder.aNewJob()
				.silentMode(true)
				.named("proxy-engine-worker-" + queues.size());

		// read from queue
		builder.reader(new BlockingQueueRecordReader(q));
		builder.filter(new PoisonRecordFilter());


		if (NONE != params.getTest()) {
			// TODO AUTO mode - how? pass it to tester and it will filter inside?

			// test proxies
			builder.processor(new ProxyTesterProcessor(new ProxyServerPrivacyDotComProxyTester()));

			// write test results to db
			builder.writer(new ProxyDatabaseWriter(db));
		}

		// need only working proxies
		builder.filter(new WorkingProxyFilter());

		if (params.getAnon()) {
			// need only anon working proxies
			builder.filter(new AnonProxyFilter());
		}

		// TODO later: recently tested filter - drop old ones

		// print working proxies
		builder.writer(new StdoutProxyWriter());

		return builder.build();
	}

	public ProxyDatabase getDb() {
		return db;
	}

	public GetCommand getParams() {
		return params;
	}
}
