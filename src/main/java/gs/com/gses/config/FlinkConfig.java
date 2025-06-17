package gs.com.gses.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


import java.util.List;

/**
 * @    @Value("${spring.datasource.username}") 配置文件不配置会报错，采用此种不配置就是用默认值不会报错
 */
@Configuration
@ConfigurationProperties(prefix = "sbp.flink")
@Data
public class FlinkConfig {

    private Boolean enable;
    private String hostname;

    private Integer port = 1433;

    private String username;

    private String password;

    private String[] databaseList;

    private String[] tableList;
    private String checkpointStoragePath;
    private String tmpdir;
}

