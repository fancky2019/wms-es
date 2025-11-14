package gs.com.gses.mybatisplus;


import com.alibaba.fastjson.JSON;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Map;
import java.util.Properties;

/**
 *暂时未调试通，使用
 */

//@Intercepts({
//        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
//        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class})
//})

@Component
@Intercepts({
        @Signature(type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}),
})

public class SqlUpdateInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        // 获取MappedStatement
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object param = null;
        //获取类型
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (SqlCommandType.DELETE.equals(sqlCommandType) ||
                SqlCommandType.UPDATE.equals(sqlCommandType) ||
                SqlCommandType.INSERT.equals(sqlCommandType)) {
            // 获取SQL语句的ID，哪个方法执行
            //sqlId:com.example.demo.dao.demo.ProductTestMapper.update
            String sqlId = mappedStatement.getId();
            if (args.length >= 2) {
                param = args[1];
            }
//            // 获取SQL语句
//            BoundSql boundSql = mappedStatement.getBoundSql(args[1]);
//            String sql = boundSql.getSql();
        }

        try {
            return invocation.proceed();
        } finally {
//            if(param!=null&&SqlUtil.getLocalHistory()) {
//                SqlUtil.clearLocalHistory();
//                // 变更信息入表
//                HistoryInsertService.insertIntoHistory(SqlUtil.getTableName(), param);
//
//            }

            Object data = param;
            String jsonString = JSON.toJSONString(data);

            // 将 JSON 字符串转换为 Map
//            Map<String, Object> jsonMap = JSON.parseObject(jsonString, new TypeReference<Map<String, Object>>() {
//            });

            int m = 0;


//            String id = jsonMap.get("id").toString();
//            if (StringUtils.isBlank(id)) {
//                return;
//            }
//            HistoryTest historyTest = new HistoryTest();
//            historyTest.setData(jsonString);
//            historyTest.setTableName(table);
//            historyTest.setDataId(id);
//            List<HistoryTest> historyTests = historyTestMapper.selectByExample(historyTest);
//            if (historyTests == null || historyTests.isEmpty()) {
//                historyTest.setVersion("1");
//            } else {
//                historyTest.setVersion(String.valueOf((historyTests.size() + 1)));
//            }
//            historyTestMapper.insert(historyTest);


        }

    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以配置拦截器的属性
    }
}
