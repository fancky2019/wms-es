package gs.com.gses.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "sbp")
@Data
public class CorsProperties {
    private List<String> allowedoriginpatterns;
}