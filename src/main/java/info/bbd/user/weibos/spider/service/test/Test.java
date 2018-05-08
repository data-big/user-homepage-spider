package info.bbd.user.weibos.spider.service.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Test {

	public static void main(String[] args) throws Exception {
		String birthday = "1993年5月2日";
		if (birthday.contains("年") && birthday.contains("月") && birthday.contains("日")) {
			birthday = birthday.replaceAll("年", "-");
			birthday = birthday.replaceAll("月", "-");
			birthday = birthday.replaceAll("日", "");
			System.out.println(birthday);
		}
	}

	public static void testCrawler() throws Exception {
		/** HtmlUnit请求web页面 */
		WebClient wc = new WebClient();
		wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
		wc.getOptions().setCssEnabled(false); // 禁用css支持
		wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
		wc.getOptions().setTimeout(10000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
		HtmlPage page = wc.getPage("http://cq.qq.com/baoliao/detail.htm?294064");
		String pageXml = page.asXml(); // 以xml的形式获取响应文本

		/** jsoup解析文档 */
		Document doc = Jsoup.parse(pageXml, "http://cq.qq.com");
		Elements pv = doc.select(".detail-content");
		System.out.println(pv.text());

		System.out.println("Thank God!");
	}

	public static void testUserHttpUnit() throws Exception {

		/** HtmlUnit请求web页面 */
		WebClient wc = new WebClient(BrowserVersion.CHROME);
		wc.getOptions().setUseInsecureSSL(true);
		wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
		wc.getOptions().setCssEnabled(false); // 禁用css支持
		wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
		wc.getOptions().setTimeout(100000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
		wc.getOptions().setDoNotTrackEnabled(false);
		HtmlPage page = wc.getPage("http://weibo.com/u/2713968254?is_hot=1");

		DomNodeList<DomElement> links = page.getElementsByTagName("a");

		for (DomElement link : links) {
			System.out.println(link.asText() + link.getAttribute("href"));
		}
	}
	
}
