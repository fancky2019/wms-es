package gs.com.gses.service.demo.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import gs.com.gses.mapper.demo.ProductTestMapper;
import gs.com.gses.model.entity.demo.ProductTest;
import gs.com.gses.multipledatasource.DataSource;
import gs.com.gses.multipledatasource.DataSourceType;
import gs.com.gses.multipledatasource.DynamicDataSource;
import gs.com.gses.service.demo.ProductTestService;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * mybatisPlus批量
 * 服务实现类
 *
 *
 * @author author
 * @since 2022-11-17
 */
@Service
@Slf4j
public class ProductTestServiceImpl extends ServiceImpl<ProductTestMapper, ProductTest> implements ProductTestService {

    // 显式注入secondary的SqlSessionTemplate
    @Autowired
    @Qualifier("secondarySqlSessionTemplate")
    private SqlSessionTemplate secondarySqlSessionTemplate;


    @DataSource(DataSourceType.SLAVE)
    @Override
    public ProductTest getProductTestById(int id) {
        //sqlSession.getConnection().getMetaData().getURL()
        DataSourceType dataSourceType = DynamicDataSource.getDataSource();
//        if (dataSource instanceof HikariDataSource) {
//            HikariDataSource hikari = (HikariDataSource) dataSource;
//
//            System.out.println("=== HikariCP 连接池配置信息 ===");
//            System.out.println("1. 连接池名称: " + hikari.getPoolName());
//            System.out.println("2. 数据库URL: " + hikari.getJdbcUrl());
//            System.out.println("3. 用户名: " + hikari.getUsername());
//        }

//        执行的sqlSession
//        return this.mapperMethod.execute(sqlSession, args);
        // //mapper及mapper.xml要分包放，不然sqlSessionFactoryRef无法选择
        ProductTest productTest = this.getById(id);

        /*
         查看数据库连接字符串：
         1、计算表达式获取HikariProxy
         sqlSession.getConfiguration().getEnvironment().getDataSource().getConnection()
         2、查看Hikari字段
         poolEntry--> hikariPool --> config --> jdbcUrl
         */


        //指定SqlSessionTemplate 不必关系自动配置的sqlSessionFactory。
        ProductTestMapper mapper = secondarySqlSessionTemplate.getMapper(ProductTestMapper.class);
        return mapper.selectById(id);
    }
}