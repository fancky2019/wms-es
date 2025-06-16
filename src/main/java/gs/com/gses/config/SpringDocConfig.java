package gs.com.gses.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/*


访问：
http://localhost:8212/swagger-ui/index.html

 */

/**
 * 内部依赖 swagger UI
 * <dependency>
 * <groupId>org.springdoc</groupId>
 * <artifactId>springdoc-openapi-ui</artifactId>
 * <version>${springdoc.version}</version>
 * </dependency>
 * swagger
 * 3.0
 * http://localhost:8088/swagger-ui/index.html
 */
@Configuration
public class SpringDocConfig {
//    @Bean
    public OpenAPI springShopOpenAPI() {
//        List<SecurityRequirement> list = new ArrayList<SecurityRequirement>();
//        list.add(new SecurityRequirement().addList("api_token"));

        return new OpenAPI()
                .info(new Info().title("SpringShop API")
                        .description("Spring shop sample application")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                        .externalDocs(new ExternalDocumentation()
                        .description("SpringShop Wiki Documentation")
                        .url("https://springshop.wiki.github.org/docs"))
              //  .security(list)
                ;


    }

    /**
     * 添加token 避免每个@Operation写入安全性 方法 ：Authorization:Bearer token_info_strW
     *
     * @return
     */
    @Bean
    public OpenAPI customOpenAPI() {
       String moduleName="service-provider-two";
        final String securitySchemeName = "bearerAuth";
        final String apiTitle = String.format("%s API", StringUtils.capitalize(moduleName));
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .info(new Info().title(apiTitle).version("v0.0.1"));
    }
}
