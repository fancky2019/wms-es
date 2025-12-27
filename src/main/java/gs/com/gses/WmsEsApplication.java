package gs.com.gses;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


//@EnableAspectJAutoProxy(exposeProxy = true,
//        proxyTargetClass = true)//强制使用 CGLIB 代理)//开启spring注解aop配置的支持，获取当前代理对象 (PersonService) AopContext.currentProxy();

@EnableAspectJAutoProxy(exposeProxy = true)//开启spring注解aop配置的支持，获取当前代理对象 (PersonService) AopContext.currentProxy();
//@EnableTransactionManagement //默认是开启的

//@EnableAspectJAutoProxy(
//        exposeProxy = true,        // 必须为true
//        proxyTargetClass = true    // 使用CGLIB代理
//)

//@MapperScan({
//        "gs.com.gses.mapper",
//        "gs.com.gses.**.mapper"   // 递归扫描所有层级的mapper包
//})
@EnableScheduling //quartz
@EnableFeignClients
@SpringBootApplication(exclude = {RedissonAutoConfiguration.class})

//@SpringBootApplication(exclude = {RedissonAutoConfiguration.class,DataSourceAutoConfiguration.class})
public class WmsEsApplication {

    public static void main(String[] args) {
        // 在 Flink 作业初始化代码中添加
        SpringApplication.run(WmsEsApplication.class, args);
    }

}
