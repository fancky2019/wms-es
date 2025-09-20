package gs.com.gses.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lirui
 */
@Target({ElementType.TYPE, ElementType.METHOD})//注解目标，只加在类上
@Retention(RetentionPolicy.RUNTIME)
public @interface DuplicateSubmission {

    /**
     *是否校验重复提交
     */
    boolean value() default true;


    DuplicateSubmissionCheckType checkType() default DuplicateSubmissionCheckType.FINGERPRINT;

    //60s,重复提交校验有效时间   超过提交指纹从redis删除这个时间无法校验
    int timeOut() default 60;
}
