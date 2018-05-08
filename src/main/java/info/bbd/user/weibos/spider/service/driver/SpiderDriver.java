package info.bbd.user.weibos.spider.service.driver;

import info.bbd.user.weibos.spider.service.common.ProgramDriver;
import info.bbd.user.weibos.spider.service.test.HbaseDemo;

public class SpiderDriver {

	/**
	 * 主函数
	 */
	public static void main(String[] args) {

		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();
		try {
			pgd.addClass("sinaUserInfoSpider", SinaUserInfoSpider.class, "新浪公共微博用户主页基本信息抓取并存储");
			pgd.addClass("hbaseDemo", HbaseDemo.class, "查询hbase");
			pgd.driver(args);
			// Success
			exitCode = 0;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		System.exit(exitCode);

	}

}
