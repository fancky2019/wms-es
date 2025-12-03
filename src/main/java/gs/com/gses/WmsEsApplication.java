package gs.com.gses;

import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy(exposeProxy = true)//开启spring注解aop配置的支持，获取当前代理对象 (PersonService) AopContext.currentProxy();

//@EnableAspectJAutoProxy(
//        exposeProxy = true,        // 必须为true
//        proxyTargetClass = true    // 使用CGLIB代理
//)
@EnableScheduling
@EnableFeignClients
@SpringBootApplication(exclude = {RedissonAutoConfiguration.class})
public class WmsEsApplication {

    public static void main(String[] args) {
        // 在 Flink 作业初始化代码中添加
        SpringApplication.run(WmsEsApplication.class, args);
    }

}
