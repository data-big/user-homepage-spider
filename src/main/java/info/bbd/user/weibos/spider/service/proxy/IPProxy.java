package info.bbd.user.weibos.spider.service.proxy;


/**
 * IP代理
 * 
 * @author gy
 *
 */
public class IPProxy {
	
	/**
	 * IP地址
	 */
	private String ip;
	
	/**
	 * 端口
	 */
	private int port;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public IPProxy() {
		super();
	}
	
}
