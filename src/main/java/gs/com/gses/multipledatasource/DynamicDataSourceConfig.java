package gs.com.gses.multipledatasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig {

    /**
     * 主数据源配置
     */
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * 从数据源配置
     */
    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    @ConditionalOnProperty(prefix = "spring.datasource.secondary", name = "jdbc-url")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * 从数据源配置
     */
    @Bean(name = "thirdDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.third")
    @ConditionalOnProperty(prefix = "spring.datasource.third", name = "jdbc-url")
    public DataSource thirdDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }


    /**
     * 动态数据源 - 使用required = false允许空值
     */
    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") @Autowired(required = false) DataSource slaveDataSource,
            @Qualifier("thirdDataSource") @Autowired(required = false) DataSource thirdDataSource) {

        Map<Object, Object> targetDataSources = new HashMap<>();

        // 主数据源必须存在
        targetDataSources.put(DataSourceType.MASTER, masterDataSource);

        // 从数据源可能不存在
        if (slaveDataSource != null) {
            targetDataSources.put(DataSourceType.SLAVE, slaveDataSource);
        }

        // 第三个数据源可能不存在
        if (thirdDataSource != null) {
            targetDataSources.put(DataSourceType.THIRD, thirdDataSource);
        }

        return new DynamicDataSource(masterDataSource, targetDataSources);
    }
}
