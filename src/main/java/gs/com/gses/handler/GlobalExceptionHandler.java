package gs.com.gses.handler;


import feign.FeignException;
import gs.com.gses.model.response.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 该类放在单独一个文件夹
 * 只能捕捉进入controller里异常的代码。
 * <p>
 * extends ResponseEntityExceptionHandler
 *
 * @ControllerAdvice :注解定义全局异常处理类
 * @ExceptionHandler :注解声明异常处理方法
 */
@ControllerAdvice
@Slf4j
//@RestControllerAdvice
public class GlobalExceptionHandler {

    private static Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private HttpServletResponse httpServletResponse;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private HttpServletRequest request;
    /**
     * 处理404 Not Found异常
     * 需配合ErrorController使用
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public MessageResult<String> handle404(NoHandlerFoundException ex) {
        log.error("404异常 - 请求URL: {}", request.getRequestURL());
        String  errorMsg=  "资源不存在: " + request.getRequestURI();
        MessageResult<Void> result = new MessageResult<>();
        result.setCode(HttpStatus.NOT_FOUND.value());
        result.setMessage(errorMsg);
        return MessageResult.faile();

    }


    /**
     * 404无法进入此方法。404被tomcat拦截了
     * 自定义返回数据格式
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public MessageResult<Void> exceptionHandler(Exception ex, WebRequest request) {


        //        response.setStatus(HttpServletResponse.SC_ACCEPTED); // 202
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
//
//        if (ex instanceof ResourceNotFoundException) {
//            status = HttpStatus.NOT_FOUND;
//        } else if (ex instanceof BadRequestException) {
//            status = HttpStatus.BAD_REQUEST;
//        }
//

        MessageResult<Void> messageResult = new MessageResult<>();
        messageResult.setCode(500);
        String msg = "";

//        if (ex instanceof UndeclaredThrowableException) {
//            UndeclaredThrowableException undeclaredThrowableException = (UndeclaredThrowableException) ex;
//            msg = undeclaredThrowableException.getUndeclaredThrowable().getMessage();
//        } else {
//            msg = ex.getMessage();
//        }
        msg = ex.getMessage();
        messageResult.setMessage(ex.getMessage());
        messageResult.setSuccess(false);
//        Void.class
        //     MDC.put("traceId", traceId);//traceId在过滤器的destroy()中清除
        //   messageResult.setTraceId(MDC.get("traceId"));
//        return ResponseEntity.ok(messageResult);
//        logger.error(ex.toString());// 不会打出异常的堆栈信息
        logger.error("", ex);//用此重载，打印异常的所有信息
        return messageResult;
    }


    @ExceptionHandler({
            FeignException.class,
            FeignException.Unauthorized.class,
            FeignException.Forbidden.class,
            FeignException.NotFound.class  // 新增404处理
    })
    public ResponseEntity<MessageResult<Void>> handleFeignException(FeignException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMsg = "服务调用异常:"+ex.getMessage();

        // 根据具体异常类型细化处理
        if (ex instanceof FeignException.Unauthorized) {
            status = HttpStatus.UNAUTHORIZED;
            errorMsg = "认证失败，请检查访问凭证";
        }
        else if (ex instanceof FeignException.Forbidden) {
            status = HttpStatus.FORBIDDEN;
            errorMsg = "权限不足，禁止访问";
        }
        else if (ex instanceof FeignException.NotFound) {
            status = HttpStatus.NOT_FOUND;
            errorMsg = "请求资源不存在: " + ex.request().url();
        }

        // 统一响应构造
        MessageResult<Void> result = new MessageResult<>();
        result.setCode(status.value());
        result.setMessage(errorMsg);
//        return MessageResult.faile();
        return ResponseEntity
                .status(status)
                .body(result);


//        ResponseEntity 是Spring提供的完整响应封装器，可以精确控制：
//        return ResponseEntity
//                .status(404)                // HTTP状态码
//                .header("X-Custom", "123")  // 自定义头
//                .body(result);              // 响应体

//        直接返回MessageResult时，Spring会自动包装为200 OK响应，状态码不可控
    }


}
