package com.gs.gses.config;

import com.gs.gses.utility.SnowFlake;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SnowFlakeConfig {
    @Bean
    public SnowFlake snowFlake() {
        return new SnowFlake(1,0);
    }
}
