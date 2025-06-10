package gs.com.gses.easyecel;


import java.util.Arrays;
import java.util.Optional;

/**
 * https://blog.csdn.net/qq_26182739/article/details/105820870?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Edefault-7.control&dist_request_id=1619674577995_93473&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Edefault-7.control
 */
public class ResoveDropAnnotationUtil {
    public static String[] resove(DropDownSetField dropDownSetField){
        if(!Optional.ofNullable(dropDownSetField).isPresent()){
            return null;
        }

        // 获取固定下拉信息
        String[] source = dropDownSetField.source();
        if(null != source && source.length > 0){
            return source;
        }

        // 获取动态的下拉数据
        Class<? extends DropDownSetInterface>[] classes = dropDownSetField.sourceClass();
        if(null != classes && classes.length > 0){
            try {
                DropDownSetInterface dropDownSetInterface = Arrays.stream(classes).findFirst().get().newInstance();
                String[] dynamicSource = dropDownSetInterface.getSource();
                if(null != dynamicSource && dynamicSource.length > 0){
                    return dynamicSource;
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
