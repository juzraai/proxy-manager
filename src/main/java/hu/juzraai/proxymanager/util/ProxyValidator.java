package hu.juzraai.proxymanager.util;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * @author Zsolt Jurányi
 */
public class ProxyValidator {

	public static final Pattern IP_PATTERN = Pattern.compile(repeat("([1-9]?[0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])", 4, "\\."));
	public static final Pattern PORT_PATTERN = Pattern.compile("([0-9]|[1-9][0-9]{1,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])");

	public static boolean isValidIp(String ip) {
		return null != ip && IP_PATTERN.matcher(ip).matches();
	}

	public static boolean isValidIpPort(String ipPort) {
		if (null != ipPort) {
			String[] parts = ipPort.split(":");
			return 2 == parts.length && isValidIp(parts[0]) && isValidPort(parts[1]);
		}
		return false;
	}

	public static boolean isValidPort(String port) {
		return null != port && PORT_PATTERN.matcher(port).matches();
	}

	private static String repeat(@Nonnull String s, int n, @Nonnull String d) {
		StringBuilder sb = new StringBuilder();
		for (; n > 0; n--) {
			sb.append(s);
			if (n > 1) {
				sb.append(d);
			}
		}
		return sb.toString();
	}

}
