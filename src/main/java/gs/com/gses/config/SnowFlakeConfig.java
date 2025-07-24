package gs.com.gses.config;

import gs.com.gses.utility.SnowFlake;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SnowFlakeConfig {
    @Bean
    public SnowFlake snowFlake() {
        return new SnowFlake(1,0);
    }
}
