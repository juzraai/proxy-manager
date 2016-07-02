package hu.juzraai.proxymanager.data;

import java.util.Date;

/**
 * @author Zsolt Jurányi
 */
public class ProxyTestInfo {

	private String ipPort;
	private boolean working;
	private boolean anon;
	private Date lastChecked;
	private Date lastUsed;

	public ProxyTestInfo() {
	}

	public ProxyTestInfo(String ipPort) {
		this.ipPort = ipPort;
	}

	public ProxyTestInfo(String ip, int port) {
		this(ip + ":" + Integer.toString(port));
	}

	public String getIpPort() {
		return ipPort;
	}

	public void setIpPort(String ipPort) {
		this.ipPort = ipPort;
	}

	public Date getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Date lastChecked) {
		this.lastChecked = lastChecked;
	}

	public Date getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}

	public String ip() {
		return null == ipPort ? null : ipPort.split(":")[0];
	}

	public boolean isAnon() {
		return anon;
	}

	public void setAnon(boolean anon) {
		this.anon = anon;
	}

	public boolean isWorking() {
		return working;
	}

	public void setWorking(boolean working) {
		this.working = working;
	}

	public int port() {
		return null == ipPort ? 8080 : Integer.valueOf(ipPort.split(":")[1]);
	}

	@Override
	public String toString() {
		return "ProxyTestInfo{" +
				"ipPort='" + ipPort + '\'' +
				", working=" + working +
				", anon=" + anon +
				", lastChecked=" + lastChecked +
				", lastUsed=" + lastUsed +
				'}';
	}
}
