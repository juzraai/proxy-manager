package hu.juzraai.proxymanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author Zsolt Jurányi
 */
@DatabaseTable(tableName = TableNames.PROXY_LIST_INFO)
public class ProxyListInfo {

	@DatabaseField(id = true)
	private String url;
	@DatabaseField
	private Long lastFetched;
	@DatabaseField
	private Integer proxyCount;
	@DatabaseField
	private Integer workingProxyCount;
	@DatabaseField
	private Integer anonWorkingProxyCount;
	@DatabaseField
	private Integer sharedProxyCount;

	public Integer getAnonWorkingProxyCount() {
		return anonWorkingProxyCount;
	}

	public void setAnonWorkingProxyCount(Integer anonWorkingProxyCount) {
		this.anonWorkingProxyCount = anonWorkingProxyCount;
	}

	public Long getLastFetched() {
		return lastFetched;
	}

	public void setLastFetched(Long lastFetched) {
		this.lastFetched = lastFetched;
	}

	public Integer getProxyCount() {
		return proxyCount;
	}

	public void setProxyCount(Integer proxyCount) {
		this.proxyCount = proxyCount;
	}

	public Integer getSharedProxyCount() {
		return sharedProxyCount;
	}

	public void setSharedProxyCount(Integer sharedProxyCount) {
		this.sharedProxyCount = sharedProxyCount;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getWorkingProxyCount() {
		return workingProxyCount;
	}

	public void setWorkingProxyCount(Integer workingProxyCount) {
		this.workingProxyCount = workingProxyCount;
	}

	@Override
	public String toString() {
		return "ProxyListInfo{" +
				"url='" + url + '\'' +
				", lastFetched=" + lastFetched +
				", proxyCount=" + proxyCount +
				", workingProxyCount=" + workingProxyCount +
				", anonWorkingProxyCount=" + anonWorkingProxyCount +
				", sharedProxyCount=" + sharedProxyCount +
				'}';
	}
}
