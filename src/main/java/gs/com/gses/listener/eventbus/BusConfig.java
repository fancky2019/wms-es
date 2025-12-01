package gs.com.gses.listener.eventbus;

import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@RemoteApplicationEventScan
//@RemoteApplicationEventScan(basePackages = "com.yourpackage.events")  // 扫描指定包下的远程事件
public class BusConfig {
    // 自动扫描并注册远程事件
    // 默认扫描当前包及其子包
    //落rabbitmq
}
