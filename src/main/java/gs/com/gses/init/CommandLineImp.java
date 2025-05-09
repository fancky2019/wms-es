//package gs.com.gses.init;
//
//import com.ververica.cdc.connectors.mysql.source.MySqlSource;
//import com.ververica.cdc.connectors.mysql.table.StartupOptions;
//import gs.com.gses.flink.DataChangeInfo;
//import gs.com.gses.flink.DataChangeSink;
//import gs.com.gses.flink.MysqlDeserialization;
//import org.apache.flink.api.common.eventtime.WatermarkStrategy;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.util.Properties;
//
//
////容器初始化完成执行：ApplicationRunner-->CommandLineRunner-->ApplicationReadyEvent
//
///**
// * @author lirui
// */
////@Order控制配置类的加载顺序，通过@Order指定执行顺序，值越小，越先执行
//@Component
//@Order(1)
//public class CommandLineImp implements CommandLineRunner {
//    //    @Value("${config.configmodel.fist-Name}")
////    private String fistName;
//    private static Logger LOGGER = LogManager.getLogger(CommandLineImp.class);
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private DataChangeSink dataChangeSink;
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        Properties jdbcProperties = new Properties();
//        jdbcProperties.setProperty("useSSL", "false");
////            MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
//        MySqlSource<DataChangeInfo> mySqlSource = MySqlSource.<DataChangeInfo>builder()
//                .hostname("127.0.0.1")
//                .port(3306)
//                .databaseList("demo") // set captured database, If you need to synchronize the whole database, Please set tableList to ".*".
//                .tableList("demo.person") // set captured table
//                .username("root")
//                .password("123456")
//
////                    .jdbcProperties(jdbcProperties)
//                // 包括schema的改变
//                .includeSchemaChanges(true)
//                // 反序列化设置
////                    .deserializer(new JsonDebeziumDeserializationSchema())
//                .deserializer(new MysqlDeserialization())
//                // 启动模式；关于启动模式下面详细介绍
//                .startupOptions(StartupOptions.initial())
//                .build();
////            RichSinkFunction richSinkFunction=new DataChangeSink();
////        RichSinkFunction richSinkFunction = new DataChangeSink();
////            RichSinkFunction richSinkFunction=new PersonDeserialization();
//
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
//        // enable checkpoint
//        env.enableCheckpointing(3000);
//
//        env
//                .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
//                // 添加Sink
//                .addSink(dataChangeSink)
//
////                    .print()
//                // set 4 parallel source tasks
//                .setParallelism(4)
//
//                .setParallelism(1); // use parallelism 1 for sink to keep message ordering
////            streamSource.addSink(dataChangeSink);
//        env.execute("Print MySQL Snapshot + Binlog");
//
//
////            // 环境配置
////            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment() ;
////            // 设置 6s 的 checkpoint 间隔
////            env.enableCheckpointing(6000) ;
////            // 设置 source 节点的并行度为 4
////            env.setParallelism(4) ;
////            env.fromSource(source, WatermarkStrategy.noWatermarks(), "MySQL")
////                    // 添加Sink
////                    .addSink(this.customSink) ;
////            env.execute() ;
//
//
//    }
//}
