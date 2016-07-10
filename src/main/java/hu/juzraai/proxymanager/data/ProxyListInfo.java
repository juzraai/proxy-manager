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
	private Date lastFetched;
	@DatabaseField
	private int proxyCount;
	@DatabaseField
	private int workingProxyCount;
	@DatabaseField
	private int anonWorkingProxyCount;
	@DatabaseField
	private int sharedProxyCount;

	public int getAnonWorkingProxyCount() {
		return anonWorkingProxyCount;
	}

	public void setAnonWorkingProxyCount(int anonWorkingProxyCount) {
		this.anonWorkingProxyCount = anonWorkingProxyCount;
	}

	public Date getLastFetched() {
		return lastFetched;
	}

	public void setLastFetched(Date lastFetched) {
		this.lastFetched = lastFetched;
	}

	public int getProxyCount() {
		return proxyCount;
	}

	public void setProxyCount(int proxyCount) {
		this.proxyCount = proxyCount;
	}

	public int getSharedProxyCount() {
		return sharedProxyCount;
	}

	public void setSharedProxyCount(int sharedProxyCount) {
		this.sharedProxyCount = sharedProxyCount;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getWorkingProxyCount() {
		return workingProxyCount;
	}

	public void setWorkingProxyCount(int workingProxyCount) {
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
