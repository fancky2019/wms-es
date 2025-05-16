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
import org.apache.flink.runtime.checkpoint.Checkpoint;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
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



        System.setProperty("java.io.tmpdir", "D:\\flinkcdc\\temp");  // 设置临时目录
// 先删除旧的历史文件再启动任务
        try {
            Files.deleteIfExists(Paths.get("D:\\flinkcdc\\dbhistory.dat"));
        } catch (IOException e) {
            log.info("Could not delete history file", e);
        }

        Properties debeziumProps = new Properties();
        //禁用证书
        debeziumProps.setProperty("database.encrypt", "false");
        debeziumProps.setProperty("database.trustServerCertificate", "true"); // 跳过证书验证


//        // 使用内存存储数据库历史（不持久化，重启后丢失） Debezium 默认使用 MemoryDatabaseHistory
//        debeziumProps.setProperty("database.history", "io.debezium.relational.history.MemoryDatabaseHistory");
//// 必须配置每次启动都执行快照
//debeziumProps.setProperty("snapshot.mode", "initial");


        // 使用文件存储数据库历史
        //The db history topic or its content is fully or partially missing. Please check database history topic configuration and re-execute the snapshot.
        debeziumProps.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
//        debeziumProps.setProperty("database.history", "io.debezium.storage.file.history.FileDatabaseHistory");
//        debeziumProps.setProperty("database.history.file.filename", "D:\\flinkcdc\\dbhistory.txt");
        debeziumProps.setProperty("database.history.file.filename", "D:\\flinkcdc\\dbhistory.dat"); // 使用正斜杠





//        scan.interval.ms 控制增量数据扫描的时间间隔（毫秒），即两次增量数据捕获之间的间隔时间。
//      .poll.interval.ms  控制从数据库日志（如 MySQL binlog）轮询新数据的频率（毫秒）
//        poll.interval.ms 更底层，直接控制 Debezium 引擎轮询日志的间隔；scan.interval.ms 是 Flink CDC 对增量数据的全局扫描间隔

//        1. 控制数据提取频率
//scan.interval.ms：设置增量同步的时间间隔（毫秒）。例如设置为 5000 表示每5秒读取一次变更数据。
//poll.interval.ms（部分连接器）：控制轮询间隔，例如 debezium.poll.interval.ms=5000 实现每5秒轮询
//        Checkpoint 2.  保证数据处理语义
//Exactly-Once 语义：通过 Checkpoint 机制，Flink 可以确保每条数据仅被处理一次（即使发生故障），避免重复或丢失数据。这是通过 ​​分布式快照算法（Chandy-Lamport）​​ 实现的。
//对齐与非对齐 Checkpoint：
//对齐 Checkpoint：算子会等待所有输入流的 Barrier（屏障）到达后再快照状态，确保状态一致性。
//非对齐 Checkpoint（Flink 1.11+）：允许在反压场景下跳过对齐，减少延迟，但可能牺牲部分一致性。

        debeziumProps.setProperty("name", "your-connector-name12");  // 设置 Connector 名称
        debeziumProps.setProperty("database.server.name", "your_server_name1");






//        //kafka  kafka   localhost
//        debeziumProps.setProperty("database.history", "io.debezium.relational.history.KafkaDatabaseHistory");
//        debeziumProps.setProperty("database.history.kafka.bootstrap.servers", "localhost:9092");
//        debeziumProps.setProperty("database.history.kafka.topic", "dbhistory");

//        若历史文件缺失，此配置会重新生成数据, 每次启动都执行快照  initial   latest-offset
        debeziumProps.setProperty("snapshot.mode", "initial");
//        debeziumProps.setProperty("snapshot.mode", "when_needed");
        debeziumProps.setProperty("snapshot.isolation.mode", "snapshot");
        debeziumProps.setProperty("scan.interval.ms", "200");
        debeziumProps.setProperty("poll.interval.ms", "200");
        debeziumProps.setProperty("max.batch.size", "2048");


        debeziumProps.setProperty("snapshot.new.tables", "parallel");
        debeziumProps.setProperty("schema.history.internal.store.only.captured.tables.ddl", "true");
        debeziumProps.setProperty("schema.history.internal.skip.unparseable.ddl", "true");

        debeziumProps.setProperty("log.level", "DEBUG");

        SqlServerSourceBuilder.SqlServerIncrementalSource<DataChangeInfo> sqlServerSource =
                new SqlServerSourceBuilder()
                        .hostname("10.100.200.43")
                        .port(1433)
                        .databaseList("wms_liku") // monitor sqlserver database
                        .tableList("dbo.InventoryItemDetail_copy1")
                        .username("sa")
                        .password("gen@song123")
                        .debeziumProperties(debeziumProps)
//                        .deserializer(new JsonDebeziumDeserializationSchema())

                        .deserializer(new MysqlDeserialization())
                        .startupOptions(StartupOptions.initial())
                        .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // enable checkpoint
        env.enableCheckpointing(1000);
        // set the source parallelism to 2
        env.fromSource(
                        sqlServerSource,
                        WatermarkStrategy.noWatermarks(),
                        "f1")
                .setParallelism(1)
//                .print()
                // 添加Sink
                .addSink(dataChangeSink)
                .setParallelism(1);

        env.execute("f1");


    }
}
