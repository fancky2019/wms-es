package gs.com.gses.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
页面提交前从后面获取一个token ,提交时候返回给后端，后端利用这个token 作为存入redis key
redisKey设计：ip&userid&path&param
 */
@Target({ElementType.TYPE, ElementType.METHOD,})//注解目标，只加在类上
@Retention(RetentionPolicy.RUNTIME)
public @interface NoRepeatSubmit {
}
