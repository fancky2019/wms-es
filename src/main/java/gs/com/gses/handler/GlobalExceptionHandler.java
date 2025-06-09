package gs.com.gses.handler;


import gs.com.gses.model.response.MessageResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

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
//@RestControllerAdvice
public class GlobalExceptionHandler {

    private static Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);


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

//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
//        ErrorResponse error = new ErrorResponse("UNAUTHORIZED", ex.getMessage());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
//    }



}
