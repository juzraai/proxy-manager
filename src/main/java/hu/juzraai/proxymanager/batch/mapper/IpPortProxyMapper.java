package hu.juzraai.proxymanager.batch.mapper;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.mapper.RecordMapper;
import org.easybatch.core.mapper.RecordMappingException;
import org.easybatch.core.record.StringRecord;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * Transforms {@link StringRecord} into {@link ProxyRecord} which has payload of
 * type {@link ProxyTestInfo} instead of <code>String</code>.
 *
 * @author Zsolt Jurányi
 */
public class IpPortProxyMapper implements RecordMapper<StringRecord, ProxyRecord> {

	private static final Logger L = LoggerFactory.getLogger(IpPortProxyMapper.class);

	/**
	 * Transforms {@link StringRecord} into {@link ProxyRecord} which has
	 * payload of type {@link ProxyTestInfo}. The original IP:PORT
	 * <code>String</code> payload will stored in <code>ipPort</code> field.
	 *
	 * @param record {@link StringRecord} to be converted
	 * @return {@link ProxyRecord} which has the original IP:PORT
	 * <code>String</code> payload in <code>ipPort</code> field
	 * @throws RecordMappingException
	 */
	@Nonnull
	@Override
	public ProxyRecord processRecord(@Nonnull StringRecord record) throws RecordMappingException {
		ProxyTestInfo proxy = new ProxyTestInfo();
		proxy.setIpPort(record.getPayload());
		L.trace("Raw proxy '{}' is mapped to {}", record.getPayload(), proxy);
		return new ProxyRecord(record.getHeader(), proxy);
	}
}
