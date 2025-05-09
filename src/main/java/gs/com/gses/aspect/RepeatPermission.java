package gs.com.gses.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})//注解目标，只加在类上
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatPermission {
//   String  apiName() default "";

   /**
    * 方法名称
    * @return
    */
   String value()default "";

}
