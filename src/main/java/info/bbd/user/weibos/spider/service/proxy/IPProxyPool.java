package info.bbd.user.weibos.spider.service.proxy;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.soft.utils.config.ConfigUtil;
import info.soft.utils.log.LogbackUtil;

/**
 * IP代理池
 * 
 * @author gy
 * 
 */
public class IPProxyPool {

	private final static Logger LOGGER = LoggerFactory.getLogger(IPProxyPool.class);

	/**
	 * 私有实例
	 */
	private static IPProxyPool instance;
	
	/**
	 * 私有构造方法
	 */
	private IPProxyPool() {
		// 初始化
		systemConfig=ConfigUtil.getProps("app.properties");
		pool = new HashMap<Integer, IPProxy>();
//		refresh();
		refreshByCookie(systemConfig.getProperty("cookie"));
		// 创建定时刷新任务
		Thread refreshTask = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						refresh();
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		});
		// 异常处理
		refreshTask.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOGGER.error("ip's proxy pool is stop refresh thread={}, throwable={}", t.getId(), e.getMessage());
				throw new RuntimeException(e);
			}
		});
		// 启动刷新任务
		refreshTask.start();
	}
	
	/**
	 * 系统配置
	 */
	private Properties systemConfig;
	
	/**
	 * 代理池
	 */
	private Map<Integer, IPProxy> pool;
	

	/**
	 * 获取IP代理对象
	 * 
	 * @return IPProxy IP代理对象
	 */
	public static IPProxy getProxy() {
		if (null == instance)
			synchronized (IPProxyPool.class) {
				if (null == instance)
					instance = new IPProxyPool();
			}
		return instance.pool.get((int) (Math.random() * Integer.parseInt(instance.systemConfig.getProperty("ip_proxy_pool_size"))));
	}

	/**
	 * 刷新代理池通过cookie
	 */
	private void refreshByCookie(String cookie) {
		try {
			String responses = doGet(systemConfig.getProperty("ip_proxy_pool_api")+"&num="+systemConfig.getProperty("ip_proxy_pool_size"),cookie);
			String[] array = responses.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] tmp = array[i].split(":");
				IPProxy ipProxy = new IPProxy();
				ipProxy.setIp(tmp[0]);
				ipProxy.setPort(Integer.parseInt(tmp[1]));
				pool.put(i, ipProxy);
			}
		} catch (Exception e) {
			LOGGER.warn("ip's proxy pool refresh fail, response={}", LogbackUtil.expection2Str(e));
		}
	}
	
	/**
	 * 刷新代理池
	 */
	private void refresh() {
		try {
			String responses = doGet(systemConfig.getProperty("ip_proxy_pool_api")+"&num="+systemConfig.getProperty("ip_proxy_pool_size"));
			String[] array = responses.split(",");
			for (int i = 0; i < array.length; i++) {
				String[] tmp = array[i].split(":");
				IPProxy ipProxy = new IPProxy();
				ipProxy.setIp(tmp[0]);
				ipProxy.setPort(Integer.parseInt(tmp[1]));
				pool.put(i, ipProxy);
			}
		} catch (Exception e) {
			LOGGER.warn("ip's proxy pool refresh fail, response={}", LogbackUtil.expection2Str(e));
		}
	}
	
	/**
	 * GET请求方法
	 * @param url 接口地址
	 * @return String 响应信息
	 */
	private String doGet(String url) {
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
		RuntimeException exception = new RuntimeException("request fail");
		// 开启重试保护
		do {
			try {
				// 执行请求
				httpClient.executeMethod(method);
				StringBuilder stringBuilder = new StringBuilder();
				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));) {
					String lineString = null;
					while ((lineString = bufferedReader.readLine()) != null) {
						stringBuilder.append(lineString + ",");
					}
					if (stringBuilder.length() > 0)
						stringBuilder.deleteCharAt(stringBuilder.length() - 1);
				}
				// 返回
				return stringBuilder.toString();
			} catch (URIException e) {
				exception = new RuntimeException(e);
			} catch (IOException e) {
				exception = new RuntimeException(e);
			} catch (Exception e) {
				exception = new RuntimeException(e);
			} finally {
				// 关闭响应
				method.releaseConnection();
			}
		} while (++retry < 3);// 最大重试3次
		throw exception;
	}

	/**
	 * GET请求方法
	 * @param url 接口地址
	 * @return String 响应信息
	 */
	private String doGet(String url,String cookie) {
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
//		method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		method.setRequestHeader("Cookie", cookie);
		// 采取服务端响应结束后立即关闭连接
		method.setRequestHeader("Connection", "close");
		// HttpClient默认网络层面请求失败会重试三次，在连接超时或响应中断的情况下，程序则直接抛出异常，不会触发默认的重试操作
		// 下面的重试保护属于业务层面的重试，最大限度的保证每次请求都成功
		// 重试计数器
		int retry = 0;
		// 异常
		RuntimeException exception = new RuntimeException("request fail");
		// 开启重试保护
		do {
			try {
				// 执行请求
				httpClient.executeMethod(method);
				StringBuilder stringBuilder = new StringBuilder();
				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));) {
					String lineString = null;
					while ((lineString = bufferedReader.readLine()) != null) {
						stringBuilder.append(lineString + ",");
					}
					if (stringBuilder.length() > 0)
						stringBuilder.deleteCharAt(stringBuilder.length() - 1);
				}
				// 返回
				return stringBuilder.toString();
			} catch (URIException e) {
				exception = new RuntimeException(e);
			} catch (IOException e) {
				exception = new RuntimeException(e);
			} catch (Exception e) {
				exception = new RuntimeException(e);
			} finally {
				// 关闭响应
				method.releaseConnection();
			}
		} while (++retry < 3);// 最大重试3次
		throw exception;
	}
}
