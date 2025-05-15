package gs.com.gses.init;


import com.ververica.cdc.connectors.base.options.StartupOptions;
import com.ververica.cdc.connectors.sqlserver.SqlServerSource;
import com.ververica.cdc.connectors.sqlserver.source.SqlServerSourceBuilder;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.flink.DataChangeSink;
import gs.com.gses.flink.MysqlDeserialization;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

//import java.util.Properties;


//容器初始化完成执行：ApplicationRunner-->CommandLineRunner-->ApplicationReadyEvent

/**
 * @author lirui
 */
//@Order控制配置类的加载顺序，通过@Order指定执行顺序，值越小，越先执行
@Component
@Order(1)
@Slf4j
public class CommandLineImp implements CommandLineRunner {
    //    @Value("${config.configmodel.fist-Name}")
//    private String fistName;
    private static Logger LOGGER = LogManager.getLogger(CommandLineImp.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataChangeSink dataChangeSink;

    @Override
    public void run(String... args) throws Exception {
        System.setProperty("java.io.tmpdir", "D:\\flnkcdc\\temp");  // 设置临时目录
// 先删除旧的历史文件再启动任务
        try {
            Files.deleteIfExists(Paths.get("D:\\flnkcdc\\dbhistory.dat"));
        } catch (IOException e) {
            log.info("Could not delete history file", e);
        }

        Properties debeziumProps = new Properties();
        //禁用证书
        debeziumProps.setProperty("database.encrypt", "false");
        debeziumProps.setProperty("database.trustServerCertificate", "true"); // 跳过证书验证


//        // 使用内存存储数据库历史（不持久化，重启后丢失）
//        debeziumProps.setProperty("database.history", "io.debezium.relational.history.MemoryDatabaseHistory");
//// 必须配置每次启动都执行快照
//debeziumProps.setProperty("snapshot.mode", "initial");


//        // 使用文件存储数据库历史
//        //The db history topic or its content is fully or partially missing. Please check database history topic configuration and re-execute the snapshot.
////        debeziumProps.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
//        debeziumProps.setProperty("database.history", "io.debezium.storage.file.history.FileDatabaseHistory");
//        debeziumProps.setProperty("database.history.file.filename", "D:\\flnkcdc\\dbhistory.dat");


//        若历史文件缺失，此配置会重新生成数据, 每次启动都执行快照
        debeziumProps.setProperty("snapshot.mode", "initial");
        debeziumProps.setProperty("snapshot.isolation.mode", "snapshot");
        debeziumProps.setProperty("poll.interval.ms", "500");
        debeziumProps.setProperty("max.batch.size", "2048");


        debeziumProps.setProperty("snapshot.new.tables", "parallel");
        debeziumProps.setProperty("schema.history.internal.store.only.captured.tables.ddl", "true");
        debeziumProps.setProperty("schema.history.internal.skip.unparseable.ddl", "true");

        SqlServerSourceBuilder.SqlServerIncrementalSource<DataChangeInfo> sqlServerSource =
                new SqlServerSourceBuilder()
                        .hostname("10.100.200.43")
                        .port(1433)
                        .databaseList("wms_liku") // monitor sqlserver database
                        .tableList("dbo.MqMessage")
                        .username("sa")
                        .password("gen@song123")
                        .debeziumProperties(debeziumProps)
//                        .deserializer(new JsonDebeziumDeserializationSchema())

                        .deserializer(new MysqlDeserialization())
                        .startupOptions(StartupOptions.initial())
                        .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // enable checkpoint
        env.enableCheckpointing(3000);
        // set the source parallelism to 2
        env.fromSource(
                        sqlServerSource,
                        WatermarkStrategy.noWatermarks(),
                        "SqlServerIncrementalSource11")
                .setParallelism(2)
//                .print()
                // 添加Sink
                .addSink(dataChangeSink)
                .setParallelism(1);

        env.execute("name11");


    }
}
