package gs.com.gses.service.impl;

import gs.com.gses.config.FlinkConfig;
import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.flink.DataChangeSink;
import gs.com.gses.flink.SqlServerDeserialization;
import gs.com.gses.service.FlinkCdcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.cdc.connectors.base.options.StartupOptions;
import org.apache.flink.cdc.connectors.sqlserver.source.SqlServerSourceBuilder;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Properties;

@Service
@Slf4j
public class FlinkCdcImpl implements FlinkCdcService {

    @Autowired
    private FlinkConfig flinkConfig;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataChangeSink dataChangeSink;

    @Async("threadPoolExecutor")
    @Override
    public void run() throws Exception {
        if (!flinkConfig.getEnable()) {
            log.info("flink is not enable");
            return;
        }
        System.setProperty("java.io.tmpdir", flinkConfig.getTmpdir());  // 设置临时目录
//       // 先删除旧的历史文件再启动任务
//        try {
//            Files.deleteIfExists(Paths.get("D:\\flinkcdc\\dbhistory.dat"));
//        } catch (IOException e) {
//            log.info("Could not delete history file", e);
//        }

        Properties debeziumProps = new Properties();
        //禁用证书
        debeziumProps.setProperty("database.encrypt", "false");
        debeziumProps.setProperty("database.trustServerCertificate", "true"); // 跳过证书验证

//        debeziumProps.setProperty("database.sslProtocol", "TLSv1.2");


// 设置 JDBC 连接和 Socket 超时（单位：毫秒）
        debeziumProps.setProperty("database.socketTimeout", "60000");      // 查询超时 60 秒
        debeziumProps.setProperty("database.connectionTimeout", "30000"); // 连接超时 30 秒

        debeziumProps.setProperty("database.keepAlive", "true");      //  # 启用 TCP 保活
        debeziumProps.setProperty("database.keepAliveInterval", "2"); // 保活探测间隔（秒）


        // 错误处理器配置
        debeziumProps.setProperty("transforms", "errorHandler");
        debeziumProps.setProperty("transforms.errorHandler.type", "io.debezium.transforms.ErrorHandler");
        debeziumProps.setProperty("transforms.errorHandler.on.failure", "retry");
        debeziumProps.setProperty("transforms.errorHandler.retry.delay.ms", "5000");
        debeziumProps.setProperty("transforms.errorHandler.max.retries", "3");

//        // 使用内存存储数据库历史（不持久化，重启后丢失） Debezium 默认使用 MemoryDatabaseHistory
//        debeziumProps.setProperty("database.history", "io.debezium.relational.history.MemoryDatabaseHistory");
//// 必须配置每次启动都执行快照
//debeziumProps.setProperty("snapshot.mode", "initial");


//        // ddl 变更配置 使用文件存储数据库历史 SQL Server connector 不支持 FileDatabaseHistory
//        //The db history topic or its content is fully or partially missing. Please check database history topic configuration and re-execute the snapshot.
//        debeziumProps.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
////        debeziumProps.setProperty("database.history.file.filename", "D:\\flinkcdc\\dbhistory.txt");
//        debeziumProps.setProperty("database.history.file.filename", "D:\\flinkcdc\\dbhistory.dat"); // 使用正斜杠
//        debeziumProps.setProperty("database.history", FileDatabaseHistory.class.getName());


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

//        debeziumProps.setProperty("name", "your-connector-name12");  // 设置 Connector 名称
//        debeziumProps.setProperty("database.server.name", "your_server_name1");


        //kafka  kafka   localhost  SQL Server connector 不支持 FileDatabaseHistory
//        debeziumProps.setProperty("database.history", "io.debezium.relational.history.KafkaDatabaseHistory");
//        debeziumProps.setProperty("database.history.kafka.bootstrap.servers", "localhost:9092");
//        debeziumProps.setProperty("database.history.kafka.topic", "dbhistory");

//        若历史文件缺失，此配置会重新生成数据, 每次启动都执行快照  initial   latest-offset .latest()
//        debeziumProps.setProperty("snapshot.mode", "latest");
//        debeziumProps.setProperty("snapshot.mode", "when_needed");
//        debeziumProps.setProperty("snapshot.isolation.mode", "snapshot");
        debeziumProps.setProperty("scan.interval.ms", "200");
        debeziumProps.setProperty("poll.interval.ms", "100");
        debeziumProps.setProperty("max.batch.size", "2048");
        debeziumProps.setProperty("heartbeat.interval.ms", "500");

//        debeziumProps.setProperty("snapshot.new.tables", "parallel");
//        debeziumProps.setProperty("schema.history.internal.store.only.captured.tables.ddl", "true");
//        debeziumProps.setProperty("schema.history.internal.skip.unparseable.ddl", "true");

        // 推荐设置：避免 snapshot 冲突

//        debeziumProps.setProperty("include.schema.changes", "true"); // 记录 DDL 变更

        debeziumProps.setProperty("max.batch.size", "4096");
        debeziumProps.setProperty("log.level", "DEBUG");
//        Flink DataStream API 方式
        SqlServerSourceBuilder.SqlServerIncrementalSource<DataChangeInfo> sqlServerSource =
                new SqlServerSourceBuilder()
                        .hostname(flinkConfig.getHostname())
                        .port(flinkConfig.getPort())

                        .databaseList(flinkConfig.getDatabaseList()) // monitor sqlserver database
                        .tableList(flinkConfig.getTableList())
                        .username(flinkConfig.getUsername())
                        .password(flinkConfig.getPassword())
                        .debeziumProperties(debeziumProps)
//                        .deserializer(new JsonDebeziumDeserializationSchema())
                        .deserializer(new SqlServerDeserialization())
//                        .startupOptions(StartupOptions.initial())
                        //latest ： 程序启动后最新的变更，启动前的变更捕捉不到
                        .startupOptions(StartupOptions.latest())  // 改成 latest()  initial()


                        .fetchSize(1024)                   // 增大每次fetch行数
                        .connectTimeout(Duration.ofSeconds(30 * 60)) // 增加连接超时
                        .connectionPoolSize(20)             // 增加连接池大小
                        .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // enable checkpoint
        env.enableCheckpointing(300);
        env.setParallelism(Runtime.getRuntime().availableProcessors());// 根据表数量和大小调整
        // set the source parallelism to 2
        env.fromSource(
                        sqlServerSource,
                        WatermarkStrategy.noWatermarks(),
                        "sqlserver43")
                .setParallelism(1)// Sink单线程 Sink单线程确保写入顺序 同一个id 的数据sink 多线程可能乱序
                .keyBy(record -> {
                    return record.getId(); // 按主键分组  .setParallelism(1) >1 解决增删改乱序
                })


//                .print()
                // 添加Sink
                .addSink(dataChangeSink);

        env.setBufferTimeout(50);  // 减少缓冲区超时
        env.getConfig().setAutoWatermarkInterval(100); // 设置watermark间隔


        // Flink 1.15 之前设置
//        env.setStateBackend(new RocksDBStateBackend("file:///D:/flinkcdc/checkpoints", true));

        // Flink 1.15+ 设置 Checkpoint 配置数据获取全量、增量
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("file:///" + flinkConfig.getCheckpointStoragePath());//file:///D:/flinkcdc/checkpoints

        //当前 checkpoint 成功后，Flink 至少等待这个时间，再开始下一个 checkpoint。
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        //任务被手动取消后，保留 checkpoint 数据，可用来恢复
        env.getCheckpointConfig().enableExternalizedCheckpoints(
                CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION
        );

        //重试
        env.setRestartStrategy(
                RestartStrategies.fixedDelayRestart(
                        3, // 最大重试次数，总共尝试3次
                        Time.seconds(2) // 重试间隔
                ));
        log.info("flink cdc start running");
        env.execute("WmsToEs");
    }
}
