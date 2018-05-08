package info.bbd.user.weibos.spider.service.driver;

import java.io.IOException;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bbd.user.weibos.spider.service.core.HbaseAdmin;
import info.bbd.user.weibos.spider.service.http.HttpHandle;
import info.soft.utils.config.ConfigUtil;

public class SinaUserInfoSpiderThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SinaUserInfoSpiderThread.class);

	private final static Properties CONFIG = ConfigUtil.getProps("app.properties");

	private static HbaseAdmin hbaseAdmin = HbaseAdmin.getInstence();

	private final HttpHandle clientDao;

	public SinaUserInfoSpiderThread(HttpHandle clientDao) {
		this.clientDao = clientDao;
	}

	@Override
	public void run() {
		try {
			String[] values = new String[] { "", "", "", "", "" };
			String cookie = CONFIG.getProperty("cookie");
			//微博用户id
//			String uid = CacheCloudUtil.getUid();
			String uid = "5688458199";
			if (uid != null && uid.length() > 0) {
				values[0] = uid;
				String responseData = clientDao.doGet("http://weibo.com/u/" + uid, cookie, "utf-8");
				Document doc = Jsoup.parse(responseData);
				Elements elements = doc.getElementsByTag("script");
				for (Element ele : elements) {
					String script = ele.html();
					if (script.indexOf("PCD_person_info") > -1) {
						script = ele.childNode(0).toString();
						script = script.replaceAll("\\\\t", "");
						script = script.replaceAll("\\\\r", "");
						script = script.replaceAll("\\\\n", "");
						script = script.replaceAll("\\\\", "");

						Document document = Jsoup.parse(script);
						Elements e1 = document.select("div.PCD_person_info");
						String introduce = e1.select("p.info").text();
						if (introduce.length() > 0 && introduce != null) {
							values[1] = introduce;
						}

						Elements elements2 = document.getElementsByClass("ul_detail");
						for (Element element : elements2) {
							Elements elementsByTag = element.getElementsByTag("li");
							for (Element e : elementsByTag) {
								String tag = e.tagName();
								if (e.text().contains("2 ")) {
									String place = e.text();
									String tmp_place = place.substring(place.indexOf(" ") + 1);
									if (tmp_place != null && tmp_place.length() > 0) {
										values[2] = tmp_place;
									}
								}
								if (e.text().contains("ö ")) {
									String birthday = e.text();
									String tmp_birthday = birthday.substring(birthday.indexOf(" ") + 1);
									if (tmp_birthday != null && tmp_birthday.length() > 0) {
										if (tmp_birthday.contains("年") && tmp_birthday.contains("月")
												&& tmp_birthday.contains("日")) {
											tmp_birthday = tmp_birthday.replaceAll("年", "-");
											tmp_birthday = tmp_birthday.replaceAll("月", "-");
											tmp_birthday = tmp_birthday.replaceAll("日", "");
											values[3] = tmp_birthday;
										}
									}
								}
								if (e.text().contains("标签 ")) {
									String label = e.text();
									String tmp_lable = label.substring(label.indexOf("标签 ") + 3);
									if (tmp_lable != null && tmp_lable.length() > 0) {
										values[4] = tmp_lable;
									}
								}

							}
						}
					}
				}
			} else {
				logger.error("Error:the cache queue's sina uid is null");
			}
//			logger.info("spider sina user info:{},{},{},{},{}", values[0], values[1], values[2], values[3], values[4]);
			if (values[0] != null && values[0].length() > 0) {
				if ((values[1] != null && values[1].length() > 0)
						|| (values[2] != null && values[2].length() > 0 || (values[3] != null && values[3].length() > 0
								|| (values[4] != null && values[4].length() > 0)))) {
					toHbase(values[0], values);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void toHbase(String rowKey, String[] values) {
		try {
			String tableName = CONFIG.getProperty("tablename");
			String[] columnfamilyNames = CONFIG.getProperty("columnFamily").split(",");
//			hbaseAdmin.createTable(tableName, columnfamilyNames, (short) 3, 1024 * 1024 * 10L);
			String[] colums = CONFIG.getProperty("columns").split(",");
			hbaseAdmin.addData(rowKey, tableName, colums, values);
			logger.info("insert hbase successed:{},{},{},{},{}", values[0], values[1], values[2], values[3], values[4]);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("post data error :{}", e.getMessage());
		}
	}
}
