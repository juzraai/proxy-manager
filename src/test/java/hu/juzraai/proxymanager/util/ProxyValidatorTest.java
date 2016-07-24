package hu.juzraai.proxymanager.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Zsolt Jurányi
 */
public class ProxyValidatorTest {

	protected boolean isValid(String proxy) {
		return ProxyValidator.isValidIpPort(proxy);
	}

	@Test
	public void shouldReturnFalseForEmptyString() {
		assertFalse(isValid(""));
	}

	@Test
	public void shouldReturnFalseForNull() {
		assertFalse(isValid(null));
	}

	@Test
	public void shouldReturnTrueForValidIpPort() {
		assertTrue(isValid("1.2.3.4:56"));
	}

	@Test
	public void shouldVerifyIp() {
		assertTrue(isValid("1.1.1.1:8080"));
		assertTrue(isValid("255.255.255.255:8080"));
		assertTrue(isValid("192.168.1.1:8080"));
		assertTrue(isValid("10.10.1.1:8080"));
		assertTrue(isValid("132.254.111.10:8080"));
		assertTrue(isValid("26.10.2.10:8080"));
		assertTrue(isValid("127.0.0.1:8080"));

		assertFalse(isValid("10.10.10:8080"));
		assertFalse(isValid("10.10:8080"));
		assertFalse(isValid("10:8080"));
		assertFalse(isValid("a.a.a.a:8080"));
		assertFalse(isValid("10.0.0.a:8080"));
		assertFalse(isValid("10.10.10.256:8080"));
		assertFalse(isValid("222.222.2.999:8080"));
		assertFalse(isValid("999.10.10.20:8080"));
		assertFalse(isValid("2222.22.22.22:8080"));
		assertFalse(isValid("22.2222.22.2:8080"));
	}

	@Test
	public void shouldVerifyPort() {
		assertTrue(isValid("132.254.111.10:0"));
		assertTrue(isValid("132.254.111.10:80"));
		assertTrue(isValid("132.254.111.10:443"));
		assertTrue(isValid("132.254.111.10:8080"));
		assertTrue(isValid("132.254.111.10:65535"));

		assertFalse(isValid("132.254.111.10:65536"));
		assertFalse(isValid("132.254.111.10:a"));
		assertFalse(isValid("132.254.111.10:"));
	}


}