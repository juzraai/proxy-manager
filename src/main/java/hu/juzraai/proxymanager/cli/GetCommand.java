package hu.juzraai.proxymanager.cli;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import hu.juzraai.proxymanager.batch.ProxyEngine;

/**
 * Model of the <code>get</code> CLI command and configuration of {@link
 * ProxyEngine}. Fields tell the proxy source, the test mode, thread count and
 * output filtering.
 *
 * @author Zsolt Jurányi
 */
@Parameters(commandNames = "get", commandDescription = "Prints working proxies")
public class GetCommand {

	@Parameter(names = {"-i", "--input"}, description = "Source of proxies", required = true, converter = InputConverter.class)
	private Input input;
	@Parameter(names = {"-t", "--test"}, description = "Test mode of proxies", converter = TestConverter.class)
	private Test test = Test.AUTO;
	@Parameter(names = {"-a", "--anon"}, description = "If specified, prints only anonymous proxies")
	private Boolean anon = false;
	@Parameter(names = {"-w", "--workers"}, description = "Number of threads to use to test proxies")
	private Integer threads = 10;

	public Boolean getAnon() {
		return anon;
	}

	public void setAnon(Boolean anon) {
		this.anon = anon;
	}

	public Input getInput() {
		return input;
	}

	public void setInput(Input input) {
		this.input = input;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	public Integer getThreads() {
		return threads;
	}

	public void setThreads(Integer threads) {
		this.threads = threads;
	}

	@Override
	public String toString() {
		return "GetCommand{" +
				"input=" + input +
				", test=" + test +
				", anon=" + anon +
				", threads=" + threads +
				'}';
	}

	public enum Input {
		STDIN, CRAWL, DB
	}

	public enum Test {
		NONE, AUTO, ALL
	}

	public static class InputConverter implements IStringConverter<Input> {

		@Override
		public Input convert(String s) {
			try {
				return Input.valueOf(s.toUpperCase());
			} catch (Exception e) {
				throw new ParameterException("Possible values for input: stdin, crawl, db");
			}
		}
	}

	public static class TestConverter implements IStringConverter<Test> {

		@Override
		public Test convert(String s) {
			try {
				return Test.valueOf(s.toUpperCase());
			} catch (Exception e) {
				throw new ParameterException("Possible values for test: none, auto, all");
			}
		}
	}
}
