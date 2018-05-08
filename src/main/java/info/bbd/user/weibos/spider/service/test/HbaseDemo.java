package info.bbd.user.weibos.spider.service.test;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bbd.user.weibos.spider.service.core.HbaseAdmin;
import info.soft.utils.config.ConfigUtil;

public class HbaseDemo {
	
	private static final Logger logger = LoggerFactory.getLogger(HbaseDemo.class);

	private final static Properties CONFIG = ConfigUtil.getProps("app.properties");

	private static HbaseAdmin hbaseAdmin = HbaseAdmin.getInstence();
	
	public static void main(String[] args) {
		try {
			hbaseAdmin.scanAll();
//			System.out.println(hbaseAdmin.rowCount());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
