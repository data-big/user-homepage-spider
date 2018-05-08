package info.bbd.user.weibos.spider.service.pool;

import java.io.Serializable;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


/**
 * 默认池配置
 *
 */
public class PoolConfig extends GenericObjectPoolConfig implements Serializable {

	private static final long serialVersionUID = -2414567557372345057L;

	/**
	 * 默认构造方法
	 */
	public PoolConfig() {
		setTestWhileIdle(true);
		setMinEvictableIdleTimeMillis(60000);
		setTimeBetweenEvictionRunsMillis(30000);
		setNumTestsPerEvictionRun(-1);
	}

}