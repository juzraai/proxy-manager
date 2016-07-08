package hu.juzraai.proxymanager.cli;

import com.beust.jcommander.Parameter;

/**
 * @author Zsolt Jurányi
 */
public class MainParameters {

	@Parameter(names = {"-db", "--database"}, description = "SQLite database filename")
	private String databaseFile = "proxies.db";

	public String getDatabaseFile() {
		return databaseFile;
	}

	public void setDatabaseFile(String databaseFile) {
		this.databaseFile = databaseFile;
	}

	@Override
	public String toString() {
		return "MainParameters{" +
				"databaseFile='" + databaseFile + '\'' +
				'}';
	}
}
