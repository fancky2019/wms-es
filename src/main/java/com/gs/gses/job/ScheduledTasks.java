package com.gs.gses.job;

import com.gs.gses.service.BasicInfoCacheService;
import com.gs.gses.service.InventoryInfoService;
import com.gs.gses.service.MqMessageService;
import com.gs.gses.sse.ISseEmitterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    @Autowired
    private ISseEmitterService sseEmitterService;

    @Value("${sbp.flag:1}")
    private Integer flag;

//    private final  MqMessageService mqMessageService;
//
//    public ScheduledTasks(MqMessageService mqMessageService) {
//        this.mqMessageService = mqMessageService;
//        // 可以安全使用，因为 serviceB 是完全初始化好的
//    }


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


    @Scheduled(cron = "0 0 3 * * ?")
    public void initInventoryInfoFromDb() {
        try {
            log.info("ScheduledTasks initInventoryInfoFromDb {}", flag);
            inventoryInfoService.initInventoryInfoFromDb(flag);
        } catch (Exception ex) {
            log.error("", ex);
        }

    }

    /**
     * 两点
     *
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void initBasicInfoCache() {
        try {
            log.info("ScheduledTasks initBasicInfoCache");
            basicInfoCacheService.initBasicInfoCache();
        } catch (Exception ex) {
            log.error("", ex);
        }

    }

    @Scheduled(fixedRate = 10000)
    public void sseHeartbeat() {
        sseEmitterService.sseHeartbeat();
    }

    /**
     * 5分钟一次
     *
     */
    @Scheduled(cron = "*/10 * * * * ?")  //10s 一次
//    @Scheduled(cron = "0 */1 * * * ?") //1min 一次
    public void mqOperation() {

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
    public void mysqlAsyncTran() {

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