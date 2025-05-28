package gs.com.gses.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/*
不能用springboot的配置，有个问题没调查明白。用此配置没问题。 把springboot 版本2.1.1升级到2.5.4没有问题
文档：https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter
 */

/*
排除redisson的自动配置，采用此配置。
 */
@Configuration
public class RedissonConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException {
        //1、创建配置
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("127.0.0.1:6379")
//                .setPassword("fancky123456");
//        return Redisson.create(config);


//        //单机
//        //默认数据库index:0
//        //redisson-->connectionManager-->config-->database
//        Config config = new Config();
//        ((SingleServerConfig) config.useSingleServer().setTimeout(1000000))
//                .setAddress("redis://127.0.0.1:6379")
//                .setPassword("fancky123456");


//        // connects to 127.0.0.1:6379 by default
//        return Redisson.create(config);


//        //集群
//        Config config = new Config();
//        config.useClusterServers()
//                .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
//                //可以用"rediss://"来启用SSL连接
//                .addNodeAddress("redis://127.0.0.1:7000", "redis://127.0.0.1:7001")
//                .addNodeAddress("redis://127.0.0.1:7002");
//
//        RedissonClient redisson = Redisson.create(config);


//        Config  config = Config.fromYAML(new File("redisson-config.yml"));
        Config  config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResource("redisson-config.yml"));
//        // 使用 Jackson 序列化
//        config.setCodec(new JsonJacksonCodec());

        return Redisson.create(config);
    }
}
