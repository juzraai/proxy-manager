package hu.juzraai.proxymanager.data;

import java.util.Date;

/**
 * @author Zsolt Jurányi
 */
public class ProxyListInfo {

	private String url;
	private Date lastFetched;
	private int proxyCount;
	private int workingProxyCount;
	private int anonWorkingProxyCount;
	private int sharedProxyCount;
}
