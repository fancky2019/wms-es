package gs.com.gses.aspect;


import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.utility.RedisKeyConfigConst;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/*


<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-aop</artifactId>
</dependency>

切点表达式:参考https://www.cnblogs.com/zhangxufeng/p/9160869.html
execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern) throws-pattern?)
modifiers-pattern：方法的可见性，如public，protected；
ret-type-pattern：方法的返回值类型，如int，void等；
declaring-type-pattern：方法所在类的全路径名，如com.spring.Aspect；
name-pattern：方法名类型，如buisinessService()；
param-pattern：方法的参数类型，如java.lang.String；
throws-pattern：方法抛出的异常类型，如java.lang.Exception；

实例：
execution(public * com.spring.service.BusinessObject.businessService(java.lang.String,..))
匹配使用public修饰，返回值为任意类型，并且是com.spring.BusinessObject类中名称为businessService的方法，
方法可以有多个参数，但是第一个参数必须是java.lang.String类型的方法


//日志采用ControllerAdvice,不采用Aspect

 *通配符：该通配符主要用于匹配单个单词，或者是以某个词为前缀或后缀的单词。
..通配符：该通配符表示0个或多个项，主要用于declaring-type-pattern和param-pattern中，如果用于declaring-type-pattern中，
          则表示匹配当前包及其子包，如果用于param-pattern中，则表示匹配0个或多个参数。
 */
@Aspect
@Component
@Order(101)
//@Slf4j
@Log4j2
public class LogAspect {


    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ObjectMapper objectMapper;



    @Pointcut("execution(* gs.com.gses.controller.*.*(..))")
    public void pointCut() {
    }



    //region redis token version

    /**
     * 环绕增强：目标方法执行前后分别执行一些代码，发生异常的时候执行另外一些代码
     *
     * @return
     */
//    @Around(value = "execution(* com.example.demo.controller.*.*(..))")
    @Around(value = "pointCut()")
    public Object aroundMethod(ProceedingJoinPoint jp) throws Throwable {

//        //并发访问，加锁控制
//        RLock lock1 = redissonClient.getLock("operationLockKey");
////       boolean re= lock1.tryLock();
//        lock1.tryLock(10, -1, TimeUnit.SECONDS);

        String httpMethod = httpServletRequest.getMethod();
        ///sbp/demo/demoProductTest
        String uri = httpServletRequest.getRequestURI();
        // /sbp
        String contextPath = httpServletRequest.getContextPath();
        ///demo/demoProductTest
        String servletPath = httpServletRequest.getServletPath();
        switch (httpMethod) {
            case "POST":
                break;
            case "DELETE":
                break;
            case "PUT":
                break;
            case "GET":
                break;
            default:
                break;
        }
        String methodName = jp.getSignature().getName();
        //获取方法
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        //方法参数
        Object[] args = jp.getArgs();


        String className = jp.getTarget().getClass().toString();
//        String methodName = jp.getSignature().getName();
//        Object[] args = jp.getArgs();
//
//
        String json=objectMapper.writeValueAsString(args);
        log.info("{} : {} - {} 开始处理,参数列表 - {}", uri, className, methodName, json);
//        log.info("{} : {} - {} 开始处理,参数列表 - {}", uri, className, methodName, Arrays.toString(args));
//        Object result = jp.proceed();
//        log.info("{} - {} 处理完成,返回结果 - {}", className, methodName,objectMapper.writeValueAsString(result));
//


        RepeatPermission repeatPermission = method.getDeclaredAnnotation(RepeatPermission.class);
        Object result = null;
        if (repeatPermission != null) {
            String apiName = repeatPermission.value();
            if (StringUtils.isEmpty(apiName)) {
                apiName = method.getName();
            }
            String repeatToken = httpServletRequest.getHeader("repeat_token");
            if (StringUtils.isEmpty(repeatToken)) {
                // 抛出让ControllerAdvice全局异常处理
                throw new Exception("can not find token!");
            }
            /**
             * 一锁二判三更新四数据库兜底 唯一约束
             *
             * rpc ：request消息中加requestId或者把请求参数md5摘要成字符串
             * 方案一
             * 1、唯一索引，
             * 2、（1）前台打开新增页面访问后台获取该表的token (存储在redis 中的uuid)key:用户id_功能.value token
             *        获取token时候判断用户有没有没有过期时间的token，有就说明已请求，直接返回
             *         UtilityController  getRepeatToken
             *   （2） 检测前段提交的token是不是在redis 中而且过期时间不为0，验证通过更新redis 中的token过期时间
             * 3、对于篡改的api请求通过加密方式，防止信息泄密。https://host:port//api。 nginx
             *
             *
             *
             *方案二
             *设计思路新加一个处理结果状态，
             *
             * 1、唯一索引，
             *
             * token 对象{tokenStr uuid,state枚举值：0 未处理 1：处理中 2：处理完成 }
             * 2、（1）前台打开新增页面访问后台获取该表的token 对象key:用户id_功能.value token 对象
             *       并将token 对象存入数据库
             *   （2） 检测前段提交时候，获取token时候判断是不是在redis
             *         a:token 不存在返回。
             *         b:token 存在，判断token对象的状态是不是0，0说明没请求，否则直接返回
             *         验证通过入库成功更新redis的状态值1和token过期时间，事务成功更新token的状态值2
             *
             *
             *
             * 3、对于篡改的api请求通过加密方式，防止信息泄密。https://host:port//api。 nginx
             */
            //重复提交：redis 中设置带有过期的key,判断是否存在。  过期防止程序异常，不释放锁
            //在redis中判断 userid + path 是否存在

            //redis 中设置key
            BigInteger userId = new BigInteger("1");
//            String uri = httpServletRequest.getRequestURI();
            String key = "repeat:" + userId + ":" + apiName;
//            String key = "repeat:" + userId + ":" + repeatToken;
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            try {
                //getRepeatToken 时候向redis 插入一个token
                //添加重复消费redis 校验，不会存在并发同一个message
                Object tokenObj = valueOperations.get(key);
                if (tokenObj == null) {
                    return MessageResult.faile("token is not exist!");
                }
                if (!repeatToken.equals(tokenObj.toString())) {
                    return MessageResult.faile("token is incorrect!");
                }
                Long expireTime = redisTemplate.getExpire(key);
                //有过期时间
                if (expireTime != null && !expireTime.equals(-1L)) {
                    return MessageResult.faile("repeat commit,please get token first!");
                }
                //设置过期时间：此处要加锁，防止并发（两个线程同时访问过期时间都为-1）
                //加锁可以设置redisson 或者jdk synchronized  ReentrantLock cas
                //单机使用cas AtomicInteger 性能好点。 web 服务考虑可拓展 使用分布式锁
//                redisTemplate.expire(key, 1, TimeUnit.DAYS);
                //先设置redis 过期，然后调用业务，业务异常就重新调用key,也就浪费一个key
                //否则设计 异常的时候要把过期时间设置为-1，建议采用上面毕竟已经处理过，尽管异常了


                String operationLockKey = key + RedisKeyConfigConst.KEY_LOCK_SUFFIX;
                //并发访问，加锁控制
                RLock lock = redissonClient.getLock(operationLockKey);

                try {
                    //tryLock(long waitTime, long leaseTime, TimeUnit unit)
                    //获取锁等待时间
                    long waitTime = 1;
                    //持有所超时释放锁时间  24 * 60 * 60;
                    // 注意：锁超时自动释放，另外一个线程就会获取锁继续执行，代码版本号处理
                    long leaseTime = 30;
                    boolean lockSuccessfully = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
//                    boolean  lockSuccessfully=lock.tryLock();
                    if (lockSuccessfully) {
                        //获取锁之后判断过期时间是否被之前线程设置过，设置过就处理过业务
                        expireTime = redisTemplate.getExpire(key);
                        //有过期时间
                        if (expireTime != null && !expireTime.equals(-1L)) {
                            return MessageResult.faile("repeat commit,please get token first!");
                        }

//                        Object obj = monitor(jp, servletPath);
//                        //业务处理成功再设计才设置过期时间
                        redisTemplate.expire(key, 1, TimeUnit.DAYS);
//                        return obj;
                    } else {
                        //如果controller是void 返回类型，此处返回 MessageResult<Void>  也不会返回给前段
                        //超过waitTime ，扔未获得锁
                        return MessageResult.faile("重复提交:获取锁失败");
                    }
                } catch (Exception e) {
                    // throw  e;
                    return MessageResult.faile(e.getMessage());
                } finally {
                    //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                    //unlock 删除key
                    lock.unlock();
                }
                result = monitor(jp, servletPath);
            } catch (Exception e) {
                //redis 保证高可用
                // redisTemplate.delete(key);
                return MessageResult.faile(e.getMessage());
            }

        } else {
            result = monitor(jp, servletPath);
        }
//        如果是列别插叙数据量大，会影响性能
        log.debug("{} : {} - {} 处理完成,返回结果 - {}", uri, className, methodName, objectMapper.writeValueAsString(result));
        return result;

    }

    private Object monitor(ProceedingJoinPoint jp, String servletPath) throws Throwable {
        StopWatch stopWatch = new StopWatch("");
        stopWatch.start("");
        Object obj = jp.proceed();
        stopWatch.stop();
        long costTime = stopWatch.getTotalTimeMillis();
        MessageResult<Object> messageResult = MessageResult.success(obj);
        log.info("{} 处理完成,cost_time {} ms ,返回结果 - {} ", servletPath, costTime, objectMapper.writeValueAsString(messageResult));
        return obj;
    }
    //endregion

    //        @AfterThrowing(pointcut = "execution(* com.example.demo.controller.*.*(..))", throwing = "ex")
    @AfterThrowing(pointcut = "pointCut()", throwing = "ex")
    public void onExceptionThrow(Exception ex) {
        log.error("", ex);
    }

}
