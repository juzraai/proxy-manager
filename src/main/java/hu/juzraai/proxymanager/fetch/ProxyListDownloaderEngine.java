package hu.juzraai.proxymanager.fetch;

import hu.juzraai.proxymanager.data.ProxyDatabase;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Zsolt Jurányi
 */
public class ProxyListDownloaderEngine {

	private static final Logger L = LoggerFactory.getLogger(ProxyListDownloaderEngine.class);
	private final int threads;

	private final List<ProxyListDownloaderTask> crawlers = new ArrayList<ProxyListDownloaderTask>();
	private final ProxyDatabase db;

	{
		crawlers.add(new FreeProxyListsDotComPLD());
		crawlers.add(new GatherProxyDotComPLD());
		crawlers.add(new HideMyIpDotComPLD());
		crawlers.add(new IdCloakDotComPLD());
		crawlers.add(new InCloakDotComPLD());
		crawlers.add(new IpAdressDotComPLD()); // ~12/50
		crawlers.add(new ProxyDotMooDotJpPLD());
		crawlers.add(new ProxyNovaDotComPLD());
	}

	public ProxyListDownloaderEngine(int threads, ProxyDatabase db) {
		this.threads = threads;
		this.db = db;
	}

	public Set<String> fetchProxyList() {
		Set<String> proxies = new HashSet<>();

		L.info("Starting crawlers");
		Date timestamp = new Date();
		ExecutorService threadPool = Executors.newFixedThreadPool(threads);
		Map<String, Future<Set<String>>> futures = new HashMap<String, Future<Set<String>>>();
		for (ProxyListDownloaderTask crawler : crawlers) {
			L.debug("Starting crawler: {}", crawler.getClass().getName());
			futures.put(crawler.getClass().getName(), threadPool.submit(crawler));
		}
		threadPool.shutdown();

		for (Map.Entry<String, Future<Set<String>>> entry : futures.entrySet()) {
			String crawlerName = entry.getKey();
			Future<Set<String>> future = entry.getValue();
			Set<String> currentProxies = new HashSet<>();
			try {
				currentProxies.addAll(future.get());
				L.debug("Got {} unique proxies from: {}", currentProxies.size(), crawlerName);
				proxies.addAll(currentProxies);
			} catch (Exception e) {
				L.warn("Couldn't get future object of crawler " + crawlerName + ": ", e);
			}
			db.storeProxySourceInfo(currentProxies, crawlerName, timestamp);
		}

		L.info("Got {} unique proxies", proxies.size());
		return proxies;
	}

	public List<ProxyListDownloaderTask> getCrawlers() {
		return crawlers;
	}
}
