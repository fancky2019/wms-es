package gs.com.gses.aspect;


import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.aspect.duplicatesubmission.CachedBodyHttpServletRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.utility.RedisKeyConfigConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
@Slf4j
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
        //上传文件报错
//        String json=objectMapper.writeValueAsString(args);

        // 处理参数序列化，特别处理文件上传情况
        List<Object> loggableArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof MultipartFile) {
                loggableArgs.add("File[" + ((MultipartFile) arg).getOriginalFilename() + "]");
            } else if (arg instanceof MultipartFile[]) {
                loggableArgs.add("Files[" + Arrays.stream((MultipartFile[]) arg)
                        .map(MultipartFile::getOriginalFilename)
                        .collect(Collectors.joining(",")) + "]");
            } else if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                // 忽略这些参数
            } else {
                loggableArgs.add(arg);
            }
        }

        String argsJson = objectMapper.writeValueAsString(loggableArgs);
        log.info("{} : {} - {} 开始处理,参数列表 - {}", uri, className, methodName, argsJson);

        DuplicateSubmission duplicateSubmission = method.getDeclaredAnnotation(DuplicateSubmission.class);
        Object result = null;
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        if (duplicateSubmission != null) {
            String submissionToken = "";
            BigInteger userId = new BigInteger("1");
            String key = "repeat:" + userId + ":";
            if (duplicateSubmission.checkType().equals(DuplicateSubmissionCheckType.TOKEN)) {
                String repeatToken = httpServletRequest.getHeader("repeat_token");
                if (StringUtils.isEmpty(repeatToken)) {
                    // 抛出让ControllerAdvice全局异常处理
                    throw new Exception("can not find token!");
                }
                key = "repeat:" + userId + ":" + repeatToken;
                //UtilityController getRepeatToken 时候向redis 插入一个token
                Object tokenObj = valueOperations.get(key);
                if (tokenObj == null) {
                    return MessageResult.faile("token is not exist!");
                }
                if (!repeatToken.equals(tokenObj.toString())) {
                    return MessageResult.faile("token is incorrect!");
                }

            } else {
                String fingerprintBase = method + ":" + uri + ":" + argsJson;
                String requestFingerprint = DigestUtils.md5DigestAsHex(fingerprintBase.getBytes(StandardCharsets.UTF_8));
                submissionToken = requestFingerprint;
                key = "repeat:" + userId + ":" + uri + ":" + submissionToken;
                Object tokenObj = valueOperations.get(key);
                if (tokenObj == null) {

//                    boolean setSuccess = valueOperations.setIfAbsent(key, submissionToken, 3600, TimeUnit.SECONDS);
                    Boolean setSuccess = valueOperations.setIfAbsent(key, submissionToken);
                    if (Boolean.TRUE.equals(setSuccess)) {
                        log.info("DuplicateSubmissionSetKey {} success", key);
                    } else {
                        log.info("DuplicateSubmissionSetKey {} fail", key);
                    }
                }
            }

            Long expireTime = redisTemplate.getExpire(key);
            //有过期时间
            if (expireTime != null && !expireTime.equals(-1L)) {
                return MessageResult.faile("DuplicateSubmission!");
            }

            String operationLockKey = key + RedisKeyConfigConst.KEY_LOCK_SUFFIX;
            //并发访问，加锁控制
            RLock lock = redissonClient.getLock(operationLockKey);
            boolean lockSuccessfully = false;
            try {
                //tryLock(long waitTime, long leaseTime, TimeUnit unit)
                //获取锁等待时间
                long waitTime = 1;
                //持有所超时释放锁时间  24 * 60 * 60;
                // 注意：锁超时自动释放，另外一个线程就会获取锁继续执行，代码版本号处理
                long leaseTime = 600;
                lockSuccessfully = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
                if (lockSuccessfully) {
                    Object obj = monitor(jp, servletPath);
                    int timeOut = duplicateSubmission.timeOut();
                    if (timeOut > 0) {

                        Boolean re = redisTemplate.expire(key, timeOut, TimeUnit.SECONDS);
                        if (re) {
                            log.info("DuplicateSubmissionSetKey {} expire success", key);
                        } else {
                            log.info("DuplicateSubmissionSetKey {} expire fail", key);
                        }
                    } else {
                        Boolean re = redisTemplate.delete(key);
                        if (re) {
                            log.info("DuplicateSubmissionSetKey {} delete success", key);
                        } else {
                            log.info("DuplicateSubmissionSetKey {} delete fail", key);
                        }
                    }


                    return obj;
                } else {
                    return MessageResult.faile("DuplicateSubmission:获取锁失败");
                }
            } catch (Exception e) {
                throw  e;
                //不要吞了异常，到不了全局异常
//                return MessageResult.faile(e.getMessage());
            } finally {
                //解锁，如果业务执行完成，就不会继续续期，即使没有手动释放锁，在30秒过后，也会释放锁
                //unlock 删除key
                if (lockSuccessfully && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }

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


    private String generateRequestFingerprint(HttpServletRequest request) throws IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();

        // 处理URL参数
        String urlParams = request.getParameterMap().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
                .collect(Collectors.joining("&"));

        // 处理POST请求体
        String bodyParams = "";
        if ("POST".equalsIgnoreCase(method)) {
            // 缓存请求体以便多次读取
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
            bodyParams = IOUtils.toString(cachedRequest.getReader());

            // 如果是表单提交，需要特殊处理
            if (isFormPost(request)) {
                bodyParams = cachedRequest.getParameterMap().entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
                        .collect(Collectors.joining("&"));
            }
            request = cachedRequest; // 替换原始request
        }

        String fingerprintBase = method + ":" + uri + ":" + urlParams + ":" + bodyParams + ":" + ip;
        return DigestUtils.md5DigestAsHex(fingerprintBase.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isFormPost(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
    }
}
