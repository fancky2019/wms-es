package gs.com.gses.multipledatasource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(-10) // 保证在事务之前执行
public class DynamicDataSourceAspect {

    /**
     * 切入带有@DataSource注解的方法。有@DataSource注解的方法才会拦截
     */
//    @Pointcut("execution(* gs.com.gses.service.impl.*.*(..))")
    @Pointcut("@annotation(gs.com.gses.multipledatasource.DataSource)")
    public void dataSourcePointCut() {}

    @Before("dataSourcePointCut()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DataSource dataSource = method.getAnnotation(DataSource.class);
        if (dataSource != null) {
            DynamicDataSource.setDataSource(dataSource.value());
        }
        else {
            DynamicDataSource.setDataSource(DataSourceType.MASTER);
        }
    }

    @After("dataSourcePointCut()")
    public void after(JoinPoint joinPoint) {
        DynamicDataSource.clearDataSource();
    }
}
