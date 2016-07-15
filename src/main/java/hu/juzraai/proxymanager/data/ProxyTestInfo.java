package hu.juzraai.proxymanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import hu.juzraai.toolbox.data.Identifiable;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
@DatabaseTable(tableName = TableNames.PROXY_TEST_INFO)
public class ProxyTestInfo implements Identifiable<String> {

	@DatabaseField(id = true)
	private String ipPort;
	@DatabaseField
	private Boolean working;
	@DatabaseField
	private Boolean anon;
	@DatabaseField
	private Long lastChecked;
	@DatabaseField
	private Long sameResultSince;

	public ProxyTestInfo() {
	}

	public ProxyTestInfo(String ipPort) {
		this.ipPort = ipPort;
	}

	public ProxyTestInfo(String ip, int port) {
		this(ip + ":" + Integer.toString(port));
	}

	public Boolean getAnon() {
		return anon;
	}

	public void setAnon(Boolean anon) {
		this.anon = anon;
	}

	@Nonnull
	@Override
	public String getId() {
		return ipPort;
	}

	public String getIpPort() {
		return ipPort;
	}

	public void setIpPort(String ipPort) {
		this.ipPort = ipPort;
	}

	public Long getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Long lastChecked) {
		this.lastChecked = lastChecked;
	}

	public Long getSameResultSince() {
		return sameResultSince;
	}

	public void setSameResultSince(Long sameResultSince) {
		this.sameResultSince = sameResultSince;
	}

	public Boolean getWorking() {
		return working;
	}

	public void setWorking(Boolean working) {
		this.working = working;
	}

	public String ip() {
		return null == ipPort ? null : ipPort.split(":")[0];
	}

	public int port() {
		return null == ipPort ? 8080 : Integer.parseInt(ipPort.split(":")[1]);
	}

	@Override
	public String toString() {
		return "ProxyTestInfo{" +
				"ipPort='" + ipPort + '\'' +
				", working=" + working +
				", anon=" + anon +
				", lastChecked=" + lastChecked +
				", sameResultSince=" + sameResultSince +
				'}';
	}
}
