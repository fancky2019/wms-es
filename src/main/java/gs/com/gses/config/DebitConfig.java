package gs.com.gses.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sbp.debit")
@Data
public class DebitConfig {
    //00128   0012800  cf00d984be00cf8e77fb559ae44c5aa5
    //   "accountName": "admin",  // "password":"228cd269b5f3ca6d07440f278ae27836"
    private String accountName = "00128";
    private String password = "cf00d984be00cf8e77fb559ae44c5aa5";
}
