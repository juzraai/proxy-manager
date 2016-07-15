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
import hu.juzraai.proxymanager.batch.report.ReportGenerator;
import hu.juzraai.proxymanager.batch.writer.ProxyDatabaseWriter;
import hu.juzraai.proxymanager.batch.writer.StdoutProxyWriter;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.fetch.ProxyListDownloaderEngine;
import hu.juzraai.proxymanager.fetch.ProxyListDownloaderTask;
import hu.juzraai.proxymanager.test.ProxyServerPrivacyDotComProxyTester;
import hu.juzraai.proxymanager.test.ProxyTester;
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
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static hu.juzraai.proxymanager.cli.GetCommand.Test.AUTO;
import static hu.juzraai.proxymanager.cli.GetCommand.Test.NONE;

/**
 * @author Zsolt Jurányi
 */
public class ProxyEngine implements Callable<Void> {

	private static final Logger L = LoggerFactory.getLogger(ProxyEngine.class);

	private final GetCommand params;
	private final ProxyDatabase db;
	private final ProxyListDownloaderEngine plde;
	private final ReportGenerator rg = new ReportGenerator();
	private final List<BlockingQueue<Record>> queues = new ArrayList<>();
	private final List<Class<? extends ProxyTester>> testerClasses = new ArrayList<>();

	{
		// default tester
		testerClasses.add(ProxyServerPrivacyDotComProxyTester.class);
	}

	public ProxyEngine(GetCommand params, ProxyDatabase db) {
		this.params = params;
		this.db = db;
		this.plde = new ProxyListDownloaderEngine(db);
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

		L.info("Waiting for workers");
		for (Future<JobReport> fr : futureReports) {
			reports.add(fr.get());
		}
		reports.remove(0); // we don't need master job's metrics - they contain additional PRs...

		L.info("Report: {}", rg.generateReport(reports));

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
				Set<String> proxies = plde.fetchProxyList(params.getThreads());
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
			for (Class<? extends ProxyTester> testerClass : testerClasses) {
				ProxyTester tester = instantiateTester(testerClass);
				if (null != tester) {
					builder.processor(new ProxyTesterProcessor(tester, AUTO == params.getTest()));
				}
			}

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

	public List<ProxyListDownloaderTask> getProxyListDownloaders() {
		return plde.getCrawlers();
	}

	public List<Class<? extends ProxyTester>> getTesterClasses() {
		return testerClasses;
	}

	protected ProxyTester instantiateTester(Class<? extends ProxyTester> c) {
		try {
			return c.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			L.error("Failed to instantiate tester class, removing from list: {}", c.getName());
		}
		return null;
	}
}
