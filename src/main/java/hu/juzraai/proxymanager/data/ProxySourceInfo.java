package hu.juzraai.proxymanager.data;

import java.util.Date;

/**
 * @author Zsolt Jurányi
 */
public class ProxySourceInfo {

	private String ipPort;
	private String source;
	private Date firstFetched;
	private Date lastFetched;

	public Date getFirstFetched() {
		return firstFetched;
	}

	public void setFirstFetched(Date firstFetched) {
		this.firstFetched = firstFetched;
	}

	public String getIpPort() {
		return ipPort;
	}

	public void setIpPort(String ipPort) {
		this.ipPort = ipPort;
	}

	public Date getLastFetched() {
		return lastFetched;
	}

	public void setLastFetched(Date lastFetched) {
		this.lastFetched = lastFetched;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return "ProxySourceInfo{" +
				"ipPort='" + ipPort + '\'' +
				", source='" + source + '\'' +
				", firstFetched=" + firstFetched +
				", lastFetched=" + lastFetched +
				'}';
	}
}
