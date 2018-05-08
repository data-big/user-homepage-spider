package info.bbd.user.weibos.spider.service.test;

import javax.script.ScriptException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import info.bbd.user.weibos.spider.service.http.HttpHandle;
import info.soft.utils.http.ClientDao;

public class SpiderDemo {

	public static void main(String[] args) throws ScriptException {
		ClientDao clientDao = new HttpHandle();
		String responseData = clientDao.doGet("http://weibo.com/u/2701528082","_s_tentry=www.ukettle.org; UOR=www.ukettle.org,widget.weibo.com,www.ukettle.org; Apache=6689852923609.923.1525787810642; SINAGLOBAL=6689852923609.923.1525787810642; ULV=1525787811433:1:1:1:6689852923609.923.1525787810642:; YF-Page-G0=00acf392ca0910c1098d285f7eb74a11; SUB=_2AkMtrc-Ef8NxqwJRmPASzGzgbIt1wgHEieKb8T5fJRMxHRl-yT83qlUztRB6Bi3ha6MK6QfEB_lhEK262BR3BUgFYTjY; SUBP=0033WrSXqPxfM72-Ws9jqgMF55529P9D9WFNqAOC8.MWoFyIJYSHbsqy","utf-8");
		Document doc = Jsoup.parse(responseData);
		Elements elements = doc.getElementsByTag("script");
		
		for (Element ele : elements) {
			String script = ele.html();
			if (script.indexOf("PCD_person_info") > -1) {
				script = ele.childNode(0).toString();
				script = script.replaceAll("\\\\t", "");
				script = script.replaceAll("\\\\r", "");
				script = script.replaceAll("\\\\n", "");
				script =  script.replaceAll("\\\\", "");
				
				Document document = Jsoup.parse(script);
				Elements e1 = document.select("div.PCD_person_info");
				String introduce = e1.select("p.info").text();
				System.out.println(introduce);
				
				Elements elements2 = document.getElementsByClass("ul_detail");
				for (Element element : elements2) {
					Elements elementsByTag = element.getElementsByTag("li");
					for (Element e : elementsByTag) {
						String tag = e.tagName();
						if(e.text().contains("2 ")) {
							String place = e.text();
							System.out.println(place.substring(place.indexOf(" ")+1));
						} 
						if(e.text().contains("ö ")) {
							String birthday = e.text();
							if (birthday != null && birthday.length() > 0) {
							}
							System.out.println(birthday.substring(birthday.indexOf(" ")+1));
						} if(e.text().contains("标签 ")) {
							String label = e.text();
							System.out.println(label.substring(label.indexOf("标签 ")+3));
						} 
						
						
					}
				}
				}
			}
	}
}
