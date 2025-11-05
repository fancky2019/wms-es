package gs.com.gses.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import gs.com.gses.Interceptor.MetaObjectHandlerImp;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * 会覆盖yml 中的mybatis-plus配置
 */
@Configuration
@EnableConfigurationProperties(MybatisPlusProperties.class)
public class MybatisPlusDataSourceConfig {
    @Autowired
    private MybatisPlusProperties mybatisPlusProperties;
    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
//        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
//        // 如果在这里手动创建了 SqlSessionFactory，确保也设置了插件
//         bean.setPlugins(mybatisPlusInterceptor());
//        //获取mybatis-plus全局配置
//        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
//        //mybatis-plus全局配置设置元数据对象处理器为自己实现的那个
//        globalConfig.setMetaObjectHandler(new MetaObjectHandlerImp());
//        bean.setGlobalConfig(globalConfig);
//        bean.setDataSource(dataSource);
//        // 设置对应的xml文件位置
//        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
//        return bean.getObject();

//        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
//        bean.setDataSource(dataSource);
//
//        // 设置插件
//        bean.setPlugins(mybatisPlusInterceptor());
//
//        // 全局配置
//        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
//        globalConfig.setMetaObjectHandler(new MetaObjectHandlerImp());
//        bean.setGlobalConfig(globalConfig);
//
//        // 设置类型别名包（对应 yml 中的 type-aliases-package）
//        bean.setTypeAliasesPackage("gs.com.gses.demos.model.entity");
//
//        // 设置 XML 映射文件位置
//        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
//
//        // 手动创建 MyBatis Configuration 并设置下划线转驼峰为 false
//        MybatisConfiguration configuration = new MybatisConfiguration();
//        configuration.setMapUnderscoreToCamelCase(false);  // 关键配置！
//        bean.setConfiguration(configuration);
//
//        return bean.getObject();


        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);

        // 使用配置文件中的设置
        bean.setMapperLocations(mybatisPlusProperties.resolveMapperLocations());
        bean.setTypeAliasesPackage(mybatisPlusProperties.getTypeAliasesPackage());

        // 从配置文件中获取 Configuration
        MybatisConfiguration configuration =  new MybatisConfiguration();
        // 确保设置为 false #默认true.  M_Str1  默认命中mStr1
        configuration.setMapUnderscoreToCamelCase(false);
        bean.setConfiguration(configuration);

        // 其他配置...
        bean.setPlugins(mybatisPlusInterceptor());

        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        globalConfig.setMetaObjectHandler(new MetaObjectHandlerImp());
        bean.setGlobalConfig(globalConfig);

        return bean.getObject();

    }

//    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //        PaginationInnerInterceptor paginationInterceptor=new PaginationInnerInterceptor(DbType.SQL_SERVER);

        PaginationInnerInterceptor paginationInterceptor=new PaginationInnerInterceptor();
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
