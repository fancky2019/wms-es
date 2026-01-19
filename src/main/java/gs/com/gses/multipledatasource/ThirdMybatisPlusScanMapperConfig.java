package gs.com.gses.multipledatasource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 只扫mapper
 */
@Configuration
@ConditionalOnProperty(
        prefix = "spring.datasource.third",
        name = "jdbc-url",
        matchIfMissing = true
)
//@ConditionalOnExpression("'${spring.datasource.third.jdbc-url:}' == ''") // 配置了secondary.url才生效
@MapperScan(basePackages = {
        //mapper及mapper.xml要分包放，不然sqlSessionFactoryRef无法选择
        "gs.com.gses.mapper.erp"
//        "gs.com.gses.**.mapper"  // 递归扫描所有mapper包
})
public class ThirdMybatisPlusScanMapperConfig {
}
