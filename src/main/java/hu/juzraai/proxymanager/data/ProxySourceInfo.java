package hu.juzraai.proxymanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import hu.juzraai.toolbox.data.Identifiable;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
@DatabaseTable
public class ProxySourceInfo implements Identifiable<String> {

	@DatabaseField(id = true)
	private String id;
	@DatabaseField
	private String ipPort;
	@DatabaseField
	private String source;
	@DatabaseField
	private Long firstFetched;
	@DatabaseField
	private Long lastFetched;

	public Long getFirstFetched() {
		return firstFetched;
	}

	public void setFirstFetched(Long firstFetched) {
		this.firstFetched = firstFetched;
	}

	@Nonnull
	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIpPort() {
		return ipPort;
	}

	public void setIpPort(String ipPort) {
		this.ipPort = ipPort;
	}

	public Long getLastFetched() {
		return lastFetched;
	}

	public void setLastFetched(Long lastFetched) {
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
				"id='" + id + '\'' +
				", ipPort='" + ipPort + '\'' +
				", source='" + source + '\'' +
				", firstFetched=" + firstFetched +
				", lastFetched=" + lastFetched +
				'}';
	}
}
