package gs.com.gses.multipledatasource;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

//public class ThirdMybatisPlusConfig {
//}
@Configuration
@ConditionalOnProperty(
        prefix = "spring.datasource.third",
        name = "jdbc-url"
)
//@ConditionalOnExpression("'${spring.datasource.third.jdbc-url:}' != ''") // 配置了secondary.url才生效
@MapperScan(basePackages = {
        //mapper及mapper.xml要分包放，不然sqlSessionFactoryRef无法选择
        "gs.com.gses.mapper.erp"
//        "gs.com.gses.**.mapper"  // 递归扫描所有mapper包
},
//        YAML配置（会创建默认的SqlSessionFactory）
        sqlSessionFactoryRef = "thirdSqlSessionFactory")
public class ThirdMybatisPlusConfig {
    @Autowired
    private MybatisPlusProperties mybatisPlusProperties;


    /**
     * SqlSessionFactory
     */
    @Bean(name = "thirdSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("dynamicDataSource") DataSource dataSource,
            MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {

        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // XML 映射文件位置
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/erp/*.xml"));
        factoryBean.setTypeAliasesPackage("gs.com.gses.demos.model.entity.erp");


        // MyBatis 配置
        MybatisConfiguration configuration = new MybatisConfiguration();
        //下划线转驼峰
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        configuration.setDefaultStatementTimeout(30);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        factoryBean.setConfiguration(configuration);

        // 添加插件
        factoryBean.setPlugins(mybatisPlusInterceptor);

        // 全局配置
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setBanner(false);
        factoryBean.setGlobalConfig(globalConfig);

        return factoryBean.getObject();
    }


    @Bean(name = "thirdSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("thirdSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}