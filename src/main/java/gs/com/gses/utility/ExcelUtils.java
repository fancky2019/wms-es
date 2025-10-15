package gs.com.gses.utility;


import com.alibaba.excel.annotation.ExcelProperty;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class ExcelUtils {
    /**
     * 获取修改ExcelProperty的value值的class，用于导出
     * @param t 类对象
     * @param keys key为修改的字段和value为它对应表头值
     * @return
     */
    public static <T> Class<T> getClassNew(T t, LinkedHashMap<String, String> keys) throws NoSuchFieldException, IllegalAccessException {
        if (t == null) {
            return null;
        }
        try {
            int i = 0;
            for (String key : keys.keySet()) {
                Field value = t.getClass().getDeclaredField(key);
                value.setAccessible(true);
                ExcelProperty property = value.getAnnotation(ExcelProperty.class);
                if(property==null)
                {
                    continue;
                }
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(property);
                Field memberValues = invocationHandler.getClass().getDeclaredField("memberValues");
                memberValues.setAccessible(true);
                Map<String, Object> values = (Map<String, Object>) memberValues.get(invocationHandler);
                String val = keys.get(key);
                if (StringUtils.isEmpty(val)) {
                    val = key;
                }
                values.put("value", new String[]{val});
                values.put("index", i++);
            }
        } catch (Exception e) {
            throw e;
        }
        return (Class<T>) t.getClass();
    }
}
