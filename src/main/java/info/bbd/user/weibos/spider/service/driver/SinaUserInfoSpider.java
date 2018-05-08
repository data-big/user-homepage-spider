package info.bbd.user.weibos.spider.service.driver;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bbd.user.weibos.spider.service.http.HttpHandle;
import info.soft.utils.config.ConfigUtil;
import info.soft.utils.log.LogbackUtil;
import info.soft.utils.threads.ApplyThreadPool;


public class SinaUserInfoSpider {

	private final static Logger logger = LoggerFactory.getLogger(SinaUserInfoSpider.class);
	
	public static void main(String[] args) {
		Properties config = ConfigUtil.getProps("app.properties");
		HttpHandle clientDao = new HttpHandle();
        final ThreadPoolExecutor threadPool = ApplyThreadPool.getThreadPoolExector(Integer.parseInt(config
                .getProperty("thread_count")));

        // 程序退出前的资源清理工作
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                // /////////////////////////////关闭线程池///////////////////////////////
                threadPool.shutdown();
                try {
                    if (!threadPool.awaitTermination(30, TimeUnit.SECONDS))
                        threadPool.shutdownNow();
                } catch (InterruptedException e) {
                    logger.error("Exception:clean the thread's pool happen a problem {}", LogbackUtil.expection2Str(e));
                    threadPool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                logger.info("the thread's pool has cleaned");
            }
        }));
        try {
            while (!threadPool.isShutdown()) { // 如果线程池开启，则循环生产线程任务
//                logger.info("the thread's pool size={}, activecount={}, taskcount={}", threadPool.getPoolSize(),
//                        threadPool.getActiveCount(), threadPool.getTaskCount());
                threadPool.execute(new SinaUserInfoSpiderThread(clientDao));
            }
        } catch (Exception e) {
            logger.error("Exception:the thread's pool happen a problem {}", LogbackUtil.expection2Str(e));
        }
        // 程序异常结束
        logger.error("Error:the thread's pool is shutdown,the thread's pool size={}, activecount={}, taskcount={}",
                threadPool.getPoolSize(), threadPool.getActiveCount(), threadPool.getTaskCount());
	}
}
