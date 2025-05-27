package gs.com.gses.job;

import gs.com.gses.service.BasicInfoCacheService;
import gs.com.gses.service.InventoryInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    @Autowired
    private InventoryInfoService inventoryInfoService;

    @Autowired
    private BasicInfoCacheService basicInfoCacheService;

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
            System.out.println("ScheduledTasks initInventoryInfoFromDb - ");
            inventoryInfoService.initInventoryInfoFromDb();
        } catch (Exception ex) {
            log.error("", ex);
        }

    }

    /**
     * 三点
     * @throws InterruptedException
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void initBasicInfoCache() throws InterruptedException {
        try {
            System.out.println("ScheduledTasks initBasicInfoCache - ");
            basicInfoCacheService.initBasicInfoCache();
        } catch (Exception ex) {
            log.error("", ex);
        }

    }
}