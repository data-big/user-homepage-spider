package info.bbd.user.weibos.spider.service.utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {

	private static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

	public static Properties getProps(String confFileName) {
		Properties result = new Properties();
		logger.info("Load resource: " + confFileName);
		try (InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream(confFileName);) {
			result.load(in);
			return result;
		} catch (final Exception e) {
			logger.error("Config file '{}' does not exist!", confFileName);
			throw new RuntimeException();
		}
	}

}
