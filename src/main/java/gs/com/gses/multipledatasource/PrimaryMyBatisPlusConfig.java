package gs.com.gses.multipledatasource;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {
        //mapper及mapper.xml要分包放，不然sqlSessionFactoryRef无法选择
        "gs.com.gses.mapper.wms"
//        "gs.com.gses.**.mapper"  // 递归扫描所有mapper包
},
//        YAML配置（会创建默认的SqlSessionFactory）
        sqlSessionFactoryRef = "primarySqlSessionFactory"
)
public class PrimaryMyBatisPlusConfig {
    @Autowired
    private MybatisPlusProperties mybatisPlusProperties;

    /**
     * MyBatis Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(DbType.SQL_SERVER);  // SQL Server
        paginationInnerInterceptor.setOverflow(true);
        paginationInnerInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }

    /**
     * SqlSessionFactory
     */
    @Bean(name = "primarySqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("dynamicDataSource") DataSource dataSource,
            MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {

        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // 设置 XML 文件位置
        factoryBean.setMapperLocations(mybatisPlusProperties.resolveMapperLocations());
        factoryBean.setTypeAliasesPackage(mybatisPlusProperties.getTypeAliasesPackage());


        // MyBatis 配置
        MybatisConfiguration configuration = new MybatisConfiguration();
        //下划线转驼峰,默认true
        configuration.setMapUnderscoreToCamelCase(false);
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

    /**
     * 自定义配置
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(false);  // 关闭驼峰转换
            configuration.setCacheEnabled(true);
            configuration.setLazyLoadingEnabled(true);
            configuration.setAggressiveLazyLoading(false);
        };
    }


    @Bean(name = "primarySqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("primarySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    // 防止自动配置创建默认的SqlSessionFactory
    @Bean
    @ConditionalOnMissingBean(name = "sqlSessionFactory")
    public SqlSessionFactory dummySqlSessionFactory() {
        return null;
    }

}