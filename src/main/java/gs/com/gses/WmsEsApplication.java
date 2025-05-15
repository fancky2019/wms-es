package gs.com.gses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)//开启spring注解aop配置的支持，获取当前代理对象 (PersonService) AopContext.currentProxy();
@SpringBootApplication
public class WmsEsApplication {

    public static void main(String[] args) {
        // 在 Flink 作业初始化代码中添加
        System.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, TLSv1, TLSv1.1, RC4, DES, MD5withRSA");
        SpringApplication.run(WmsEsApplication.class, args);
        System.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, TLSv1, TLSv1.1, RC4, DES, MD5withRSA");
    }

}
