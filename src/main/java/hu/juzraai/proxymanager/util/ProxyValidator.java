package hu.juzraai.proxymanager.util;

/**
 * @author Zsolt Jurányi
 */
public class ProxyValidator {

	public static boolean isValidIpPort(String ipPort) {
		return ipPort.matches("\\d{1,3}(\\.\\d{1,3}){3}:\\d{2,5}");
	}
}
