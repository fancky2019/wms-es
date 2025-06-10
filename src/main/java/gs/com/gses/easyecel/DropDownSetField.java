package gs.com.gses.easyecel;


import java.lang.annotation.*;

@Documented
// 作用在字段上
@Target(ElementType.FIELD)
// 运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface DropDownSetField {
    // 固定下拉内容
    String[] source() default {};
    // 动态下拉内容
    Class[] sourceClass() default {};
}
