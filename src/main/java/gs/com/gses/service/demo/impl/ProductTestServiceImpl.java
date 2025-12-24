package gs.com.gses.service.demo.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import gs.com.gses.mapper.demo.ProductTestMapper;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.entity.demo.ProductTest;
import gs.com.gses.multipledatasource.DataSource;
import gs.com.gses.multipledatasource.DataSourceType;
import gs.com.gses.multipledatasource.DynamicDataSource;
import gs.com.gses.service.MqMessageService;
import gs.com.gses.service.demo.ProductTestService;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MqMessageService mqMessageService;


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


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mssqlSlaveTranTest1() {
        long id=1979085143561035777L;
        MqMessage mqMessage=  mqMessageService.getById(id);
        LambdaUpdateWrapper<MqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(MqMessage::getFailureReason, "1");
        updateWrapper.eq(MqMessage::getId, mqMessage.getId());
        mqMessageService.update(null, updateWrapper);
        ProductTestService productTestService = applicationContext.getBean(ProductTestService.class);
        productTestService.mssqlSlaveTranTest2();
        int n = Integer.parseInt("n");
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
//    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mssqlSlaveTranTest2() {
        long id=1979085249085530113L;
        MqMessage mqMessage=  mqMessageService.getById(id);
        LambdaUpdateWrapper<MqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(MqMessage::getFailureReason, "2");
        updateWrapper.eq(MqMessage::getId, mqMessage.getId());
        mqMessageService.update(null, updateWrapper);
    }

    @DataSource(DataSourceType.SLAVE)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mysqlSlaveTranTest1() {
        ProductTest productTest = this.getById(1);
        LambdaUpdateWrapper<ProductTest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ProductTest::getProductName, "1");
        updateWrapper.eq(ProductTest::getId, productTest.getId());
        this.update(null, updateWrapper);
        ProductTestService productTestService = applicationContext.getBean(ProductTestService.class);
        productTestService.mysqlSlaveTranTest2();
        int n = Integer.parseInt("n");

    }

    @DataSource(DataSourceType.SLAVE)
//    @Transactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void mysqlSlaveTranTest2() {
        ProductTest productTest = this.getById(2);
        LambdaUpdateWrapper<ProductTest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ProductTest::getProductName, "2");
        updateWrapper.eq(ProductTest::getId, productTest.getId());
        this.update(null, updateWrapper);
    }


}