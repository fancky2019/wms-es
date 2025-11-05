package gs.com.gses.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class MapperDeleteAspect {
    // 拦截 MyBatis Plus Mapper 中 delete 开头的方法
    @Pointcut("execution(* gs.com.gses.mapper.*.delete*(..))")
    public void deletePointcut() {}

    @AfterReturning(pointcut = "deletePointcut()", returning = "result")
    public void logAfterDelete(JoinPoint joinPoint, Object result) {
        // 获取方法名
        String methodName = joinPoint.getSignature().getName();
        // 获取传入的参数
        Object[] args = joinPoint.getArgs();
        // 记录日志，包括方法名、参数、影响行数
        log.info("执行物理删除操作 - 方法名: {}, 参数: {}, 影响行数: {}", methodName, args, result);

        // 你也可以在这里记录更详细的信息，比如操作人、时间等
        // 例如通过 SecurityContextHolder 获取当前用户
    }
}
