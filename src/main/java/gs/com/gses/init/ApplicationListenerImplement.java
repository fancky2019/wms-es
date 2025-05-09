package gs.com.gses.init;

import gs.com.gses.service.BasicInfoCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//容器初始化完成执行：ApplicationRunner-->CommandLineRunner-->ApplicationReadyEvent

//@Order控制配置类的加载顺序，通过@Order指定执行顺序，值越小，越先执
@Slf4j
@Component
@Order(1)
public class ApplicationListenerImplement implements ApplicationListener<ApplicationReadyEvent> {


    @Autowired
    private BasicInfoCacheService basicInfoCacheService;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        return;
//        basicInfoCacheService.initBasicInfoCache();
//        log.info("初始化缓存完成");
//
//        int m=0;
    }
}
