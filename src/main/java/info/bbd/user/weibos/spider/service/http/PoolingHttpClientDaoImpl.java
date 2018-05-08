package info.bbd.user.weibos.spider.service.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import info.soft.utils.http.ClientDao;


public class PoolingHttpClientDaoImpl implements ClientDao {

	//	private static Logger logger = LoggerFactory.getLogger(PoolingHttpClientDaoImpl.class);

	public class IdleConnectionMonitor extends Thread {

		private final HttpClientConnectionManager cm;
		private volatile boolean shutdown;

		public IdleConnectionMonitor(PoolingHttpClientConnectionManager cm) {
			super();
			this.cm = cm;
		}

		@Override
		public void run() {
			try {
				while (!shutdown) {
					synchronized (this) {
						wait(1000);
						cm.closeExpiredConnections();
						cm.closeIdleConnections(CONNECT_TIMEOUT, TimeUnit.SECONDS);
					}
				}
			} catch (InterruptedException ex) {
				shutdown();
			}
		}

		public void shutdown() {
			shutdown = true;
			synchronized (this) {
				notifyAll();
			}
		}

	}

	// 线程安全
	private static CloseableHttpClient httpClient;

	private static PoolingHttpClientConnectionManager poolingConnManager;

	/**
	 * Socket Timeout
	 */
	public static int SOCKET_TIMEOUT = 5000;

	/**
	 * 最大连接数
	 */
	public final static int MAX_TOTAL_CONNECTIONS = 200;

	/**
	 * 连接超时时间,秒
	 */
	public final static int CONNECT_TIMEOUT = 30;

	/**
	 * 每个路由最大连接数
	 */
	public final static int MAX_ROUTE_CONNECTIONS = 200;

	/*	private void closeExpiredConnectionsPeriodTask(final int timeUnitBySecond) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							TimeUnit.SECONDS.sleep(timeUnitBySecond);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						poolingConnManager.closeExpiredConnections();
					}
				}

			}).start();
		}*/

	/**
	 * 读取超时时间
	 */
	//	public final static int READ_TIMEOUT = 10000;

	static void init() {
		poolingConnManager = new PoolingHttpClientConnectionManager();
		// 连接池的最大连接数
		poolingConnManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
		// 对应每个Host的最大连接数，如果有3个Host则这里最大连接数*3才是公共的连接数（但是要小于连接池的最大连接数）
		poolingConnManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
		// 针对特定的HOST设置最大连接数
		HttpRoute httpRoute = new HttpRoute(new HttpHost("api.weibo.com", 80));
		poolingConnManager.setMaxPerRoute(httpRoute, MAX_ROUTE_CONNECTIONS);

		//		final RequestConfig requestConfig = RequestConfig.custom()
		//				.setSocketTimeout(WAIT_TIMEOUT)
		//				.setConnectTimeout(CONNECT_TIMEOUT)
		//				.setConnectionRequestTimeout(READ_TIMEOUT).build();
		poolingConnManager.setSocketConfig(httpRoute.getTargetHost(),
				SocketConfig.custom().setSoTimeout(SOCKET_TIMEOUT).build());

		// 如果响应中没有“Keep-Alive”，那么下面的策略可以保持连接Keep-Alive 5秒
		ConnectionKeepAliveStrategy strategy = new ConnectionKeepAliveStrategy() {

			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				HeaderElementIterator it = new BasicHeaderElementIterator(
						response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if (value != null && param.equalsIgnoreCase("timeout")) {
						return Long.parseLong(value) * 1000;
					}
				}

				HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
				if ("api.weibo.com".equalsIgnoreCase(target.getHostName())) {
					// Keep alive for 5 seconds only
					return 5 * 1000;
				} else {
					// otherwise keep alive for 30 seconds
					return 30 * 1000;
				}
			}

		};

		httpClient = HttpClients.custom().setConnectionManager(poolingConnManager).setKeepAliveStrategy(strategy)
				.build();
	}

	public PoolingHttpClientDaoImpl() {
		init();
		IdleConnectionMonitor monitor = new IdleConnectionMonitor(poolingConnManager);
		monitor.start();
		//		monitor.join(1000);
		//		closeExpiredConnectionsPeriodTask(WAIT_TIMEOUT);
	}

	public void close() {
		poolingConnManager.close();
	}

	public String doGet(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();
			StringBuilder sb = new StringBuilder();
			Scanner scanner = new Scanner(instream, "utf-8");
			while (scanner.hasNextLine())
				sb.append(scanner.nextLine() + "\n");
			EntityUtils.consume(entity);
			//			httpGet.releaseConnection();
			scanner.close();
			response.close();

			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String doGet(String url, HashMap<String, String> headers) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doGet(String url, HashMap<String, String> headers, String charset) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doGet(String url, HashMap<String, String> headers, String cookie, String charset) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doGet(String url, String charset) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doGet(String url, String cookie, String charset) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doPost(String url, String data) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doPost(String url, String data, String charset) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doPostAndGetResponse(String url, String data) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doPostAndGetResponse(String url, String data, String charset) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doPostAndPutKeepAlive(String url, String data) {
		// TODO Auto-generated method stub
		return null;
	}

	public String doPostAndPutKeepAlive(String url, String data, String charset) {
		// TODO Auto-generated method stub
		return null;
	}

}