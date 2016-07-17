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
import hu.juzraai.proxymanager.batch.writer.BlockingQueueProxyWriter;
import hu.juzraai.proxymanager.batch.writer.ProxyDatabaseWriter;
import hu.juzraai.proxymanager.batch.writer.StdoutProxyWriter;
import hu.juzraai.proxymanager.cli.GetCommand;
import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static hu.juzraai.proxymanager.cli.GetCommand.Test.AUTO;
import static hu.juzraai.proxymanager.cli.GetCommand.Test.NONE;

/**
 * The core of Proxy Manager, this is the engine which calls proxy list
 * downloaders, filters, testers using a batch processing pattern. This engine
 * is called by the <code>get</code> CLI command, it has some options to
 * customize the processing. {@link ProxyEngine} is a {@link Callable} but it
 * also extends {@link Thread} so it can be started on a separate thread by
 * calling <code>start()</code>.
 *
 * @author Zsolt Jurányi
 */
public class ProxyEngine extends Thread implements Callable<Void> {

	public static final String POISON_RECORD = "";
	private static final Logger L = LoggerFactory.getLogger(ProxyEngine.class);
	private final GetCommand params;
	private final ProxyDatabase db;
	private final ProxyListDownloaderEngine plde;
	private final List<BlockingQueue<Record>> queues = new ArrayList<>();
	private final List<Class<? extends ProxyTester>> testerClasses = new ArrayList<>();
	private final BlockingQueue<String> output;
	private final ReportGenerator reportGenerator = new ReportGenerator();

	/**
	 * Creates a new instance. Initializes the internal {@link
	 * ProxyListDownloaderEngine} with the given {@link ProxyDatabase}, and adds
	 * {@link ProxyServerPrivacyDotComProxyTester} as tester class.
	 *
	 * @param params  {@link GetCommand} configuration for input, test mode and
	 *                filtering
	 * @param db      {@link ProxyDatabase} to be used to read and store proxy
	 *                information (source, test results, etc.)
	 * @param cliMode If <code>true</code>, engine will print result proxies to
	 *                standard output, otherwise it will put them into a {@link
	 *                BlockingQueue} initialized in this constructor
	 */
	public ProxyEngine(@Nonnull GetCommand params, @Nonnull ProxyDatabase db, boolean cliMode) {
		this.params = params;
		this.db = db;
		this.plde = new ProxyListDownloaderEngine(db);
		this.testerClasses.add(ProxyServerPrivacyDotComProxyTester.class);
		this.output = cliMode ? null : new LinkedBlockingQueue<String>();
	}

	/**
	 * Firstly it initializes the engine: creates worker jobs and their queues
	 * then builds master job. Then it starts the engine in a thread pool, and
	 * wait for threads to finish. Finally it generates a report of the batch
	 * processing to the log. Additionally if <code>cliMode</code> was set to
	 * <code>false</code> in the constructor, it sends <code>POISON_RECORD</code>
	 * to the output queue.
	 *
	 * @return Returns <code>null</code>
	 * @throws Exception
	 * @see #createMasterJob()
	 * @see #createWorker()
	 */
	@CheckForNull
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
		L.info("Report: {}", reportGenerator.generateReport(reports));

		if (null != output) {
			L.debug("Sending poison record to output queue");
			output.put(POISON_RECORD);
		}

		L.info("Engine shutting down");
		executorService.shutdown();
		return null;
	}

	/**
	 * Builds the master job which reads IP:PORT list from the selected input,
	 * filters for valid IP:PORT strings, converts them to {@link
	 * ProxyTestInfo}, loads their metadata from the database, then dispatches
	 * them to workers.
	 *
	 * @return The master job
	 * @see #createReader()
	 */
	@Nonnull
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

	/**
	 * Creates the {@link RecordReader} for the master job according to {@link
	 * GetCommand}'s <code>input</code> field. If input mode is
	 * <code>CRAWL</code> it will call {@link ProxyListDownloaderEngine} first
	 * and passes the result to the instantiated reader.
	 *
	 * @return An {@link StdinReader} if input mode is <code>STDIN</code>, or
	 * {@link StringIterableReader} containing crawled proxy list if input mode
	 * is <code>CRAWL</code> or {@link ProxyDatabaseReader} if input mode is
	 * <code>DB</code>
	 */
	@Nonnull
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
		throw new UnsupportedOperationException("Unhandled input mode: " + params.getInput());
	}

	/**
	 * Firstly it creates the input queue for the worker then it builds the
	 * worker job which reads from it. If test mode set in {@link GetCommand} is
	 * not <code>NONE</code>, it instantiates all tester classes, adds them to
	 * the processor chain in {@link ProxyTester} processors and also adds a
	 * {@link ProxyDatabaseWriter} object as writer to store test results. Adds
	 * {@link WorkingProxyFilter} and if <code>anon</code> is <code>true</code>,
	 * adds also an {@link AnonProxyFilter}. If <code>cliMode</code> was
	 * <code>true</code>, adds {@link StdoutProxyWriter}, otherwise adds {@link
	 * BlockingQueueProxyWriter} which will write into the output queue.
	 *
	 * @return The worker job
	 */
	@Nonnull
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

		// output
		if (null == output) { // cli mode
			builder.writer(new StdoutProxyWriter());
		} else {
			builder.writer(new BlockingQueueProxyWriter(output));
		}

		return builder.build();
	}

	/**
	 * @return {@link ProxyDatabase} used to read and store proxy information
	 * (source, test results, etc.)
	 */
	@Nonnull
	public ProxyDatabase getDb() {
		return db;
	}

	/**
	 * @return The output queue if <code>cliMode</code> was <code>false</code>,
	 * or <code>null</code> otherwise. The output queue is a {@link
	 * BlockingQueue} of IP:PORT strings.
	 */
	@CheckForNull
	public BlockingQueue<String> getOutput() {
		return output;
	}

	/**
	 * @return {@link GetCommand} configuration for input, test mode and
	 * filtering
	 */
	@Nonnull
	public GetCommand getParams() {
		return params;
	}

	/**
	 * @return The list of proxy downloaders (or crawlers) used to fetch proxy
	 * lists when <code>CRAWL</code> is set as input source
	 */
	@Nonnull
	public List<ProxyListDownloaderTask> getProxyListDownloaders() {
		return plde.getCrawlers();
	}

	/**
	 * @return The list of {@link ProxyTester} classes which are instantiated at
	 * worker job creation if test mode is not <code>NONE</code>
	 */
	@Nonnull
	public List<Class<? extends ProxyTester>> getTesterClasses() {
		return testerClasses;
	}

	/**
	 * Intantiates the given {@link ProxyTester} class.
	 *
	 * @return A {@link ProxyTester} instance or <code>null</code> if some error
	 * occurred
	 */
	@CheckForNull
	protected ProxyTester instantiateTester(@Nonnull Class<? extends ProxyTester> c) {
		try {
			return c.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			L.error("Failed to instantiate tester class, removing from list: {}", c.getName());
		}
		return null;
	}

	/**
	 * Calls <code>call()</code> method and catches it's exceptions.
	 */
	@Override
	public void run() {
		try {
			call();
		} catch (Exception e) {
			L.error("Engine threw exception", e);
		}
	}
}
