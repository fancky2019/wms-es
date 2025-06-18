package gs.com.gses.tool;

import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;

/**
 * 添加screw-core依赖
 */
public class ScrewGenerator {

    public static void main(String[] args) {
        // 数据源配置
        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/your_database");
//        hikariConfig.setUsername("your_username");
//        hikariConfig.setPassword("your_password");
        hikariConfig.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        hikariConfig.setJdbcUrl("jdbc:sqlserver://10.100.200.43;DatabaseName=liku;trustServerCertificate=true");
        hikariConfig.setUsername("sa");
        hikariConfig.setPassword("gen@song123");
        // 设置可以获取tables remarks信息
        hikariConfig.addDataSourceProperty("useInformationSchema", "true");
        DataSource dataSource = new HikariDataSource(hikariConfig);

        // 生成文档配置
        EngineConfig engineConfig = EngineConfig.builder()
                // 生成文件路径
                .fileOutputDir("D:/document")
                // 打开目录
                .openOutputDir(true)
                // 文件类型
                .fileType(EngineFileType.WORD)
                // 生成模板实现
                .produceType(EngineTemplateType.freemarker)
                .build();

        // 文档配置
        Configuration config = Configuration.builder()
                .version("1.0.0")
                .description("数据库设计文档")
                .dataSource(dataSource)
                .engineConfig(engineConfig)
                .produceConfig(getProcessConfig())
                .build();

        // 执行生成
        new DocumentationExecute(config).execute();
    }

    /**
     * 配置想要生成的表+ 配置想要忽略的表
     */
    private static ProcessConfig getProcessConfig() {
        // 忽略表名
        ArrayList<String> ignoreTableName = new ArrayList<>();
        ignoreTableName.add("test_user");
        ignoreTableName.add("test_group");

        // 忽略表前缀
        ArrayList<String> ignorePrefix = new ArrayList<>();
        ignorePrefix.add("Abp");

        // 忽略表后缀
        ArrayList<String> ignoreSuffix = new ArrayList<>();
        ignoreSuffix.add("_test");

        return ProcessConfig.builder()
                // 指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
                .designatedTableName(new ArrayList<>())
                .designatedTablePrefix(new ArrayList<>())
                .designatedTableSuffix(new ArrayList<>())
                // 忽略表名
                .ignoreTableName(ignoreTableName)
                // 忽略表前缀
                .ignoreTablePrefix(ignorePrefix)
                // 忽略表后缀
                .ignoreTableSuffix(ignoreSuffix)
                .build();
    }
}
