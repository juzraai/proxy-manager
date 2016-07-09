package hu.juzraai.proxymanager.batch.mapper;

import hu.juzraai.proxymanager.batch.record.ProxyRecord;
import hu.juzraai.proxymanager.data.ProxyTestInfo;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.easybatch.core.mapper.RecordMapper;
import org.easybatch.core.mapper.RecordMappingException;
import org.easybatch.core.record.StringRecord;
import org.slf4j.Logger;

/**
 * @author Zsolt Jurányi
 */
public class IpPortProxyMapper implements RecordMapper<StringRecord, ProxyRecord> {

	private static final Logger L = LoggerFactory.getLogger(IpPortProxyMapper.class);

	@Override
	public ProxyRecord processRecord(StringRecord record) throws RecordMappingException {
		ProxyTestInfo proxy = new ProxyTestInfo();
		proxy.setIpPort(record.getPayload());
		L.trace("Raw proxy '{}' is mapped to {}", record.getPayload(), proxy);
		return new ProxyRecord(record.getHeader(), proxy);
	}
}
