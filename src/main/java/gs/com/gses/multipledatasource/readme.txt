
HA设计应设计成单独的服务，通过http调用。灵活。

一个应用多数据源设计：
1、排除自动配置
@SpringBootApplication(exclude = {RedissonAutoConfiguration.class,DataSourceAutoConfiguration.class})
public class WmsEsApplication
2、注意和mybatis-plus 的配置冲突
3、 是  jdbc-url  不是url
 datasource:
      primary:
        jdbc-url
4、mapper 要分包放。路径要做区分
@MapperScan(basePackages = {
          //mapper及mapper.xml要分包放，不然sqlSessionFactoryRef无法选择
          "gs.com.gses.mapper.wms"
  //        "gs.com.gses.**.mapper"  // 递归扫描所有mapper包
  },
  //        YAML配置（会创建默认的SqlSessionFactory）
          sqlSessionFactoryRef = "primarySqlSessionFactory"
  )