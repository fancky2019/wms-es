package gs.com.gses.init;

import gs.com.gses.rabbitMQ.mqtt.MqttConsume;
import gs.com.gses.service.BasicInfoCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


//容器初始化完成执行：ApplicationRunner-->CommandLineRunner-->ApplicationReadyEvent

//@Order控制配置类的加载顺序，通过@Order指定执行顺序，值越小，越先执行
@Component
@Order(1)
@Slf4j
public class ApplicationRunnerImp implements ApplicationRunner {
    private static Logger LOGGER = LogManager.getLogger(ApplicationRunnerImp.class);

    @Autowired
    private MqttConsume mqttConsume;
    @Resource
    ApplicationContext applicationContext;

    @Autowired
    private BasicInfoCacheService basicInfoCacheService;


    @Value("${sbp.initCache}")
    private Boolean initCache;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        LOGGER.info("ApplicationRunnerImp");
        log.info("threadId - {}", Thread.currentThread().getId());
        if (!initCache) {
            log.info("Don't run initCache");
            return;
        }
        basicInfoCacheService.initBasicInfoCache();
        log.info("ApplicationRunnerImp Complete");
//        mqttConsume.init();

    }
}
