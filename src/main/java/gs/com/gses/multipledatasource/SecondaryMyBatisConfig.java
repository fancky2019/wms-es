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
import gs.com.gses.mybatisplus.MetaObjectHandlerImp;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

//@Configuration
//@MapperScan(

////        basePackages = "com.example.mapper.secondary",
//        basePackages = "gs.com.gses.mapper",
//        sqlSessionFactoryRef = "secondarySqlSessionFactory"
//)
//public class SecondaryMyBatisConfig {
//
//    @Bean(name = "secondarySqlSessionFactory")
//    public SqlSessionFactory sqlSessionFactory(
//            @Qualifier("dynamicDataSource") DataSource dataSource) throws Exception {
//        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
//        sessionFactory.setDataSource(dataSource);
//
//        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
//                .getResources("classpath:mapper/*.xml"));
//
//        org.apache.ibatis.session.Configuration configuration =
//                new org.apache.ibatis.session.Configuration();
//        configuration.setMapUnderscoreToCamelCase(true);
//        configuration.setCacheEnabled(true);
//        sessionFactory.setConfiguration(configuration);
//
//        return sessionFactory.getObject();
//    }
//
//    @Bean(name = "secondarySqlSessionTemplate")
//    public SqlSessionTemplate sqlSessionTemplate(
//            @Qualifier("secondarySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
//}



//
@Configuration
//@ConditionalOnExpression("'${spring.datasource.secondary.jdbc-url:}' != ''") // 配置了secondary.url才生效
@ConditionalOnProperty(
        prefix = "spring.datasource.secondary",
        name = "jdbc-url"
)
@MapperScan(basePackages = {
        //mapper及mapper.xml要分包放，不然sqlSessionFactoryRef无法选择
        "gs.com.gses.mapper.demo"
//        "gs.com.gses.**.mapper"  // 递归扫描所有mapper包
},
//        YAML配置（会创建默认的SqlSessionFactory）
        sqlSessionFactoryRef = "secondarySqlSessionFactory")
public class SecondaryMyBatisConfig {
    @Autowired
    private MybatisPlusProperties mybatisPlusProperties;
    @Autowired
    private MetaObjectHandlerImp metaObjectHandlerImp;

    /**
     * SqlSessionFactory
     */
    @Bean(name = "secondarySqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("dynamicDataSource") DataSource dataSource,
            MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {

        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // XML 映射文件位置
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/demo/*.xml"));
        factoryBean.setTypeAliasesPackage("gs.com.gses.demos.model.entity.demo");


        // MyBatis 配置
        MybatisConfiguration configuration = new MybatisConfiguration();
        //下划线转驼峰
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        configuration.setDefaultStatementTimeout(30);
        //StdOutImpl sql打印在控制台，Slf4jImpl  #SQL脚本输出文件。使用配置文件的配置
//        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        factoryBean.setConfiguration(configuration);

        // 添加插件
        factoryBean.setPlugins(mybatisPlusInterceptor);

        // 全局配置
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setBanner(false);
        globalConfig.setMetaObjectHandler(metaObjectHandlerImp);
        factoryBean.setGlobalConfig(globalConfig);

        return factoryBean.getObject();
    }


    @Bean(name = "secondarySqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("secondarySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}