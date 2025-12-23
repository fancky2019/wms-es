package gs.com.gses.job;

import gs.com.gses.service.BasicInfoCacheService;
import gs.com.gses.service.InventoryInfoService;
import gs.com.gses.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class ScheduledTasks {

    //    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    @Autowired
    private InventoryInfoService inventoryInfoService;

    @Autowired
    private BasicInfoCacheService basicInfoCacheService;

    @Autowired
    private MqMessageService mqMessageService;

//    // 每5秒执行一次
//    @Scheduled(fixedRate = 5000)
//    public void taskWithFixedRate() {
//        System.out.println("固定频率任务 - " + System.currentTimeMillis());
//    }

//    // 上次任务结束后延迟3秒执行
//    @Scheduled(fixedDelay = 3000)
//    public void taskWithFixedDelay() {
//        System.out.println("固定延迟任务 - " + System.currentTimeMillis());
//    }

    /**
     * 三点
     * @throws InterruptedException
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void initInventoryInfoFromDb() throws InterruptedException {
        try {
            log.info("ScheduledTasks initInventoryInfoFromDb");
            inventoryInfoService.initInventoryInfoFromDb();
        } catch (Exception ex) {
            log.error("", ex);
        }

    }

    /**
     * 两点
     * @throws InterruptedException
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void initBasicInfoCache() throws InterruptedException {
        try {
            log.info("ScheduledTasks initBasicInfoCache");
            basicInfoCacheService.initBasicInfoCache();
        } catch (Exception ex) {
            log.error("", ex);
        }

    }

    /**
     * 5分钟一次
     * @throws Exception
     */
//    @Scheduled(cron = "*/10 * * * * ?")  //10s 一次
    @Scheduled(cron = "0 */5 * * * ?") //5min 一次
    public void mqOperation() throws Exception {

        try {

            log.info("ScheduledTasks mqOperation");
            //定时任务内不要使用AopContext.currentProxy()，使用注册自身
            boolean proxy = AopUtils.isAopProxy(mqMessageService);

            // 1. 检查代理类型
            boolean isAopProxy = AopUtils.isAopProxy(mqMessageService);
            boolean isCglibProxy = AopUtils.isCglibProxy(mqMessageService);
            boolean isJdkProxy = AopUtils.isJdkDynamicProxy(mqMessageService);
            mqMessageService.mqOperation();
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    /**
     * 5分钟一次
     * @throws Exception
     */
    @Scheduled(cron = "*/10 * * * * ?")  //10s 一次
//    @Scheduled(cron = "0 */5 * * * ?") //5min 一次
    public void mysqlAsyncTran() throws Exception {

        try {

            log.info("mysqlAsyncTran mqOperation");
            //定时任务内不要使用AopContext.currentProxy()，使用注册自身
            boolean proxy = AopUtils.isAopProxy(mqMessageService);

            // 1. 检查代理类型
            boolean isAopProxy = AopUtils.isAopProxy(mqMessageService);
            boolean isCglibProxy = AopUtils.isCglibProxy(mqMessageService);
            boolean isJdkProxy = AopUtils.isJdkDynamicProxy(mqMessageService);
            mqMessageService.syncMethod();
        } catch (Exception ex) {
            log.error("", ex);
        }
    }
}