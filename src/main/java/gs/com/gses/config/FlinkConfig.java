package gs.com.gses.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "sbp.flink")
@Data
public class FlinkConfig {

    private String hostname;

    private Integer port = 1433;

    private String username;

    private String password;

    private String[] databaseList;

    private String[] tableList;
    private String checkpointStoragePath;
    private String tmpdir;
}

