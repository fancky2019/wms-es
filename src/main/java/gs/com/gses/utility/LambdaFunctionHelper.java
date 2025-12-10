package gs.com.gses.utility;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.entity.ShipOrderItem;
import gs.com.gses.model.request.Sort;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//wrapper.orderByAsc(getSFunctionByFieldName(User.class, "age"));

/**
 *
 */
public class LambdaFunctionHelper {
    private static final Map<Class<?>, Map<String, SFunction<?, ?>>> CACHE = new ConcurrentHashMap<>();

    public static List<OrderItem> getWithDynamicSort(Map<String, String> sortParams) {
        List<OrderItem> orderItems = new ArrayList<>();
        Set<String> allowedFields = new HashSet<>(Arrays.asList("name", "age", "create_time"));

        sortParams.forEach((field, direction) -> {
//            if (!allowedFields.contains(field)) {
//                throw new IllegalArgumentException("非法的排序字段: " + field);
//            }

            if ("asc".equalsIgnoreCase(direction)) {
                orderItems.add(OrderItem.asc(field));
            } else if ("desc".equalsIgnoreCase(direction)) {
                orderItems.add(OrderItem.desc(field));
            } else {
                throw new IllegalArgumentException("非法的排序方向: " + direction);
            }
        });

        return orderItems;
    }


    public static List<OrderItem> getWithDynamicSort(List<Sort> sortParams) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (Sort sort : sortParams) {
            if ("asc".equalsIgnoreCase(sort.getSortType())) {
                orderItems.add(OrderItem.asc(sort.getSortField()));
            } else if ("desc".equalsIgnoreCase(sort.getSortType())) {
                orderItems.add(OrderItem.desc(sort.getSortField()));
            } else {
                throw new IllegalArgumentException("非法的排序方向: " + sort.getSortType());
            }
        }


        return orderItems;
    }


    public static String getOrderByClause(String fieldName, String sortType) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(ShipOrderItem.class);
        Optional<String> column = tableInfo.getFieldList().stream()
                .filter(f -> f.getProperty().equals(fieldName))
                .findFirst()
                .map(TableFieldInfo::getColumn);

        return column.map(col -> "ORDER BY " + col + " " + sortType).orElse(null);
    }


    public static <T> void applySort(LambdaQueryWrapper<T> wrapper, Class cla, String fieldName, String sortType) {
        try {
            Method getter = cla.getMethod("get" + StringUtils.capitalize(fieldName));
            SFunction<T, ?> function = entity -> {
                try {
                    return getter.invoke(entity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            if ("desc".equalsIgnoreCase(sortType)) {
                wrapper.orderByDesc(function);
            } else {
                wrapper.orderByAsc(function);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("无效字段: " + fieldName);
        }
    }

    public static <T> SFunction<T, ?> getLambdaByFieldName(Class<T> entityClass, String fieldName) {
        try {
            // 获取字段对应的getter方法
            Method getter = entityClass.getMethod("get" + StringUtils.capitalize(fieldName));

            // 构造Lambda表达式
            return entity -> {
                try {
                    return getter.invoke(entity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("字段不存在: " + fieldName);
        }
    }


    @SuppressWarnings("unchecked")
    public static <T, R> SFunction<T, R> getSFunctionByFieldName1(Class<T> entityClass, String fieldName) {
        Map<String, SFunction<?, ?>> fieldMap = CACHE.computeIfAbsent(entityClass, k -> {
            Map<String, SFunction<?, ?>> map = new HashMap<>();
            // 遍历实体类所有字段，生成对应的SFunction并缓存
            Arrays.stream(entityClass.getMethods())
                    .filter(m -> m.getName().startsWith("get") && m.getParameterCount() == 0)
                    .forEach(m -> {
                        String name = PropertyNamer.methodToProperty(m.getName());
                        map.put(name, (SFunction<T, R>) createLambda(entityClass, m));
                    });
            return map;
        });
        return (SFunction<T, R>) fieldMap.get(fieldName);
    }

    private static <T, R> SFunction<T, R> createLambda(Class<T> entityClass, Method getter) {
        return entity -> {
            try {
                return (R) getter.invoke(entity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }


    /**
     * 获取字段上的 TableField 注解
     * @param func Lambda 表达式
     * @return TableField 注解
     */
//    public static <T> String getColumnName(SFunction<T, ?> func, Class<T> entityClass)
    public static <T> String getColumnName(SFunction<T, ?> func) {
        try {
            // 1. 获取 Lambda 表达式的字段名
            LambdaMeta meta = LambdaUtils.extract(func);
            String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());

//            // 2. 获取实体类 Class
            Class<?> entityClass = meta.getInstantiatedClass();

            // 3. 获取字段对象
            Field field = entityClass.getDeclaredField(fieldName);

            // 4. 获取 TableField 注解
            TableField annotation = field.getAnnotation(TableField.class);
            String columnName = fieldName;
            if (annotation != null) {
                // 获取列名
                columnName = annotation.value();
            }
            return columnName;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("字段不存在", e);
        }
    }


    /**
     * 获取字段名,cong getter 截取后首字母小写
     */
    public static <T> String getFieldName(SFunction<T, ?> func) {
        LambdaMeta meta = LambdaUtils.extract(func);
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        return fieldName;
    }


}
