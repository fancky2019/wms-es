package gs.com.gses.config;


import gs.com.gses.mybatisplus.SqlUpdateInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * extends WebMvcConfigurationSupport
 *
 * 添加拦截器
 */
@Slf4j
//@Configuration
public class WebConfig implements WebMvcConfigurer {
    //extends WebMvcConfigurerAdapter 废弃了

    @Autowired
    private SqlUpdateInterceptor sqlUpdateInterceptor;

//    @Autowired
//   private AuthenticationInterceptor authenticationInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(authenticationInterceptor);
//        registry.addInterceptor(sqlUpdateInterceptor);

        log.info("加入拦截器: {}", "TokenVerifyIntercepter");;
    }

//    @Bean
//    public AuthenticationInterceptor authenticationInterceptor() {
//        return new AuthenticationInterceptor();
//    }



}