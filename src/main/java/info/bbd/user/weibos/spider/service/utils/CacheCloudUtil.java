//package info.bbd.user.weibos.spider.service.utils;
//
//import java.util.Properties;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import info.bbd.sina.cache.Cache;
//import info.bbd.sina.cache.cloud.CloudCache;
//
//public class CacheCloudUtil {
//	
//	private static final Logger logger = LoggerFactory.getLogger(CacheCloudUtil.class);
//	
//	/**
//     * 加载资源文件
//     */
//    public static Properties pros = ConfigUtil.getProps("app.properties");
//	
//	/**
//     * 加载cachecloud资源
//     */
//    private final static Cache cache =
//            CloudCache.getInstance(Integer.parseInt(pros.getProperty("app_id")));
//
//    /**
//     * 获取uid
//     * @return
//     */
//    public static String getUid(){
//       String uid = cache.rpop("uids");
//        if (null == uid) {
//        	 logger.error("Error:the cache queue's sina uid is null");
//            return "";
//        }
//        cache.lpush("uids", uid);
//        return uid;
//    }
//}
