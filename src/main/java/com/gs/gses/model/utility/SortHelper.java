//package gs.com.gses.model.utility;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
//import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//public class SortHelper {
//    public static <T> LambdaQueryWrapper<T> applyAnnotationBasedSort(
//            LambdaQueryWrapper<T> wrapper,
//            Class<T> entityClass,
//            String fieldName,
//            String direction) {
//
//        // 获取实体类所有字段
//        Field[] fields = entityClass.getDeclaredFields();
//
//        for (Field field : fields) {
//            Sortable sortable = field.getAnnotation(Sortable.class);
//            if (sortable != null) {
//                String compareName = sortable.alias().isEmpty() ?
//                        field.getName() : sortable.alias();
//
//                if (compareName.equals(fieldName)) {
//                    try {
//                        String getterName = "get" +
//                                field.getName().substring(0, 1).toUpperCase() +
//                                field.getName().substring(1);
//                        Method getter = entityClass.getMethod(getterName);
//                        @SuppressWarnings("unchecked")
//                        SFunction<T, ?> function = (SFunction<T, ?>) LambdaUtils.createLambda(entityClass, getter);
//
//                        if ("asc".equalsIgnoreCase(direction)) {
//                            wrapper.orderByAsc(function);
//                        } else {
//                            wrapper.orderByDesc(function);
//                        }
//                        break;
//                    } catch (Exception e) {
//                        throw new RuntimeException("Failed to create sort lambda", e);
//                    }
//                }
//            }
//        }
//        return wrapper;
//    }
//}
