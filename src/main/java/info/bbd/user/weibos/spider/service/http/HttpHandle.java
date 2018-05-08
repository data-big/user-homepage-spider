package info.bbd.user.weibos.spider.service.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bbd.user.weibos.spider.service.proxy.IPProxy;
import info.bbd.user.weibos.spider.service.proxy.IPProxyPool;
import info.soft.utils.config.ConfigUtil;
import info.soft.utils.http.HttpClientDaoImpl;


/**
 * HTTP请求处理程序
 * 
 */
public class HttpHandle extends HttpClientDaoImpl {

	private final static Logger LOGGER = LoggerFactory.getLogger(HttpHandle.class);

	private final static boolean IS_PROXY;

	static {
		IS_PROXY = ConfigUtil.getProps("app.properties").get("ip_proxy").equals("true") ? true : false;
	}

	@Override
	public String doGet(String url) {
		HttpClient httpClient = new HttpClient();
		// 设置连接管理器取出连接超时时间
		httpClient.getParams().setConnectionManagerTimeout(1 * 1000);
		// 设置建立连接超时时间
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(2 * 1000);
		// 设置响应超时时间
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(4 * 1000);
		// 禁用Nagle算法，降低网络延迟并提高性能
		httpClient.getHttpConnectionManager().getParams().setTcpNoDelay(true);
		HttpMethod method = new GetMethod(url);
		// 禁用Cookie提高性能
		method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		method.setRequestHeader("Cookie", "special-cookie=value");
		// 采取服务端响应结束后立即关闭连接
		method.setRequestHeader("Connection", "close");
		// HttpClient默认网络层面请求失败会重试三次，在连接超时或响应中断的情况下，程序则直接抛出异常，不会触发默认的重试操作
		// 下面的重试保护属于业务层面的重试，最大限度的保证每次请求都成功
		// 重试计数器
		int retry = 0;
		// 异常
		HttpHandleException exception = new HttpHandleException("request fail");
		// 开启重试保护
		do {
			try {
				// 设置代理，在此处设置可以保证每次重试更换新的代理
				if (IS_PROXY) {
					url = url.replace("https", "http");// 代理对HTTPS接口支持不稳定，替换为HTTP接口。
					IPProxy proxy = IPProxyPool.getProxy();
					httpClient.getHostConfiguration().setProxy(proxy.getIp(), proxy.getPort());
				}
				// 执行请求
				httpClient.executeMethod(method);
				StringBuilder stringBuilder = new StringBuilder();
				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));) {
					String lineString = null;
					while ((lineString = bufferedReader.readLine()) != null) {
						stringBuilder.append(lineString);
					}
				}
				// 记录重试成功的信息
				if (0 != retry)
					LOGGER.info("httphandle retry success, retry={}, info={}", retry,
							exception.getMessage().replace("Exception", ""));
				// 返回
				return stringBuilder.toString();
			} catch (URIException e) {
				exception = new HttpHandleException(e);
			} catch (IOException e) {
				exception = new HttpHandleException(e);
			} catch (Exception e) {
				exception = new HttpHandleException(e);
			} finally {
				// 关闭响应
				method.releaseConnection();
			}
		} while (++retry < 3);// 最大重试3次
		throw exception;
	}

	public String doGet(String url, String cookie, String charset) {
		HttpClient httpClient = new HttpClient();
		// 设置连接管理器取出连接超时时间
		httpClient.getParams().setConnectionManagerTimeout(1 * 1000);
		// 设置建立连接超时时间
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(2 * 1000);
		// 设置响应超时时间
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(4 * 1000);
		// 禁用Nagle算法，降低网络延迟并提高性能
		httpClient.getHttpConnectionManager().getParams().setTcpNoDelay(true);
		HttpMethod method = new GetMethod(url);
		// 禁用Cookie提高性能
		//method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		method.setRequestHeader("Cookie", cookie);
		// 采取服务端响应结束后立即关闭连接
		method.setRequestHeader("Connection", "close");
		// HttpClient默认网络层面请求失败会重试三次，在连接超时或响应中断的情况下，程序则直接抛出异常，不会触发默认的重试操作
		// 下面的重试保护属于业务层面的重试，最大限度的保证每次请求都成功
		// 重试计数器
		int retry = 0;
		// 异常
		HttpHandleException exception = new HttpHandleException("request fail");
		// 开启重试保护
		do {
			try {
				// 设置代理，在此处设置可以保证每次重试更换新的代理
				if (IS_PROXY) {
					url = url.replace("https", "http");// 代理对HTTPS接口支持不稳定，替换为HTTP接口。
					IPProxy proxy = IPProxyPool.getProxy();
					httpClient.getHostConfiguration().setProxy(proxy.getIp(), proxy.getPort());
				}
				// 执行请求
				httpClient.executeMethod(method);
				StringBuilder stringBuilder = new StringBuilder();
				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));) {
					String lineString = null;
					while ((lineString = bufferedReader.readLine()) != null) {
						stringBuilder.append(lineString);
					}
				}
				// 记录重试成功的信息
				if (0 != retry)
					LOGGER.info("httphandle retry success, retry={}, info={}", retry,
							exception.getMessage().replace("Exception", ""));
				// 返回
				return stringBuilder.toString();
			} /*catch (URIException e) {
				exception = new HttpHandleException(e);
			} catch (IOException e) {
				exception = new HttpHandleException(e);
			} */catch (Exception e) {
//				e.printStackTrace();
//				exception = new HttpHandleException(e);
				LOGGER.error("Exception:{}", e.toString().substring(0, 20));
			} finally {
				// 关闭响应
				method.releaseConnection();
			}
		} while (++retry < 3);// 最大重试3次
		
		return "request fail";
	}
	
	@Override
	public String doPost(String url, String data, String dataType) {
		HttpClient httpClient = new HttpClient();
		// 设置连接管理器取出连接超时时间
		httpClient.getParams().setConnectionManagerTimeout(1 * 1000);
		// 设置建立连接超时时间
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(2 * 1000);
		// 设置响应超时时间
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(4 * 1000);
		// 禁用Nagle算法，降低网络延迟并提高性能
		httpClient.getHttpConnectionManager().getParams().setTcpNoDelay(true);
		PostMethod method = new PostMethod(url);
		// 请求类型
		method.setRequestHeader("Content-Type", "application/" + dataType);
		// 接收类型
		method.setRequestHeader("Accept", "application/" + dataType);
		// 构建请求参数
		try {
			RequestEntity requestEntity = new StringRequestEntity(data, "application/" + dataType, "UTF-8");
			method.setRequestEntity(requestEntity);
		} catch (UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		}
		// 禁用Cookie提高性能
		method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		method.setRequestHeader("Cookie", "special-cookie=value");
		// 采取服务端响应结束后立即关闭连接
		method.setRequestHeader("Connection", "close");
		// HttpClient默认网络层面请求失败会重试三次，在连接超时或响应中断的情况下，程序则直接抛出异常，不会触发默认的重试操作
		// 下面的重试保护属于业务层面的重试，最大限度的保证每次请求都成功
		// 重试计数器
		int retry = 0;
		// 异常
		HttpHandleException exception = new HttpHandleException("request fail");
		// 开启重试保护
		do {
			try {
				// 设置代理，在此处设置可以保证每次重试更换新的代理
				if (IS_PROXY) {
					url = url.replace("https", "http");// 代理对HTTPS接口支持不稳定，替换为HTTP接口。
					IPProxy proxy = IPProxyPool.getProxy();
					httpClient.getHostConfiguration().setProxy(proxy.getIp(), proxy.getPort());
				}
				// 执行请求
				httpClient.executeMethod(method);
				StringBuilder stringBuilder = new StringBuilder();
				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));) {
					String lineString = null;
					while ((lineString = bufferedReader.readLine()) != null) {
						stringBuilder.append(lineString);
					}
				}
				// 记录重试成功的信息
				if (0 != retry)
					LOGGER.info("httphandle retry success, retry={}, info={}", retry,
							exception.getMessage().replace("Exception", ""));
//				LOGGER.info("solr api request api={},data={}",url,data);
				// 返回
				return stringBuilder.toString();
			} catch (URIException e) {
				exception = new HttpHandleException(e);
			} catch (IOException e) {
				exception = new HttpHandleException(e);
			} catch (Exception e) {
				exception = new HttpHandleException(e);
			} finally {
				// 关闭响应
				method.releaseConnection();
			}
		} while (++retry < 3);// 最大重试3次
		throw exception;
	}
	
}
