//package gs.com.gses.tool;
//
//import com.power.doc.builder.ApiDocBuilder;
//import com.power.doc.constants.DocLanguage;
//import com.power.doc.model.ApiConfig;
//
//public class SmartDocGenerator  {
//    public static void main(String[] args) {
//        // 创建配置
//        ApiConfig config = new ApiConfig();
//        // 可能在不同版本中参数名不同
//        // 项目根路径
//        config.setServerUrl("http://localhost:8088");
//
//        // 输出文档格式
//        config.setOutPath("D:/document");
//        // 是否生成严格模式的文档
//        config.setStrict(true);
//
//        // 是否覆盖旧文件
//        config.setCoverOld(true);
//
//        // 指定包路径
//        config.setPackageFilters("gs.com.gses.controller");
//        config.setLanguage(DocLanguage.CHINESE);
//        // 5. 可选：开启调试模式
////        config.setAllInOne(true);                    // 合并为单个文件
//        config.setCoverOld(true);                    // 覆盖旧文件
//
////        config.setAllInOneDocFileName("DOC_WORD");
//
//        // ③ 关键信息：合并到一个 Word 文件
//        config.setAllInOne(true);                              // 合并
//
//
//        // 启用分组
//        // 生成文档
//        ApiDocBuilder.buildApiDoc(config);
//    }
//}