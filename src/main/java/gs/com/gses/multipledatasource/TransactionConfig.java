package gs.com.gses.multipledatasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.util.Properties;

//@Configuration
//@EnableTransactionManagement
public class TransactionConfig {

    /**
     * 使用Primary注解确保这是主要的事务管理器
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("dynamicDataSource") DataSource dynamicDataSource) {
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dynamicDataSource);
        manager.setNestedTransactionAllowed(true);
        return manager;
    }

//    /**
//     * 显式配置事务属性
//     */
//    @Bean
//    public TransactionAttributeSource transactionAttributeSource() {
//        return new AnnotationTransactionAttributeSource();
//    }
//
//    /**
//     * 显式配置事务拦截器
//     */
//    @Bean
//    public TransactionInterceptor transactionInterceptor(
//            PlatformTransactionManager transactionManager,
//            TransactionAttributeSource transactionAttributeSource) {
//
//        TransactionInterceptor interceptor = new TransactionInterceptor();
//        interceptor.setTransactionManager(transactionManager);
//        interceptor.setTransactionAttributeSource(transactionAttributeSource);
//
//        // 设置事务属性
//        Properties properties = new Properties();
//        properties.setProperty("get*", "PROPAGATION_SUPPORTS,readOnly");
//        properties.setProperty("find*", "PROPAGATION_SUPPORTS,readOnly");
//        properties.setProperty("select*", "PROPAGATION_SUPPORTS,readOnly");
//        properties.setProperty("*", "PROPAGATION_REQUIRED");
//
//        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
//        source.setProperties(properties);
//        interceptor.setTransactionAttributeSource(source);
//
//        return interceptor;
//    }
//
//    /**
//     * 显式配置事务advisor
//     */
//    @Bean
//    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
//            TransactionInterceptor transactionInterceptor) {
//
//        BeanFactoryTransactionAttributeSourceAdvisor advisor =
//                new BeanFactoryTransactionAttributeSourceAdvisor();
//        advisor.setAdvice(transactionInterceptor);
//        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE);  // 设置高优先级
//
//        return advisor;
//    }
}
