package gs.com.gses.model.response;

import org.slf4j.MDC;

import java.io.Serializable;


public class MessageResult<T> implements Serializable {
//    private static final long serialVersionUID = 1L;
    private Integer code;
    /**
     * 执行结果（true:成功，false:失败）
     */
    private Boolean success;
    private String message;
    //  MDC.put("traceId", traceId);//traceId在过滤器的destroy()中生成、清除
    private String traceId = MDC.get("traceId");
    private T data;

    public MessageResult() {
        this.success = true;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getTraceId() {
        return traceId;
    }

    public static <T> MessageResult<T> success() {
        MessageResult<T> messageResult = new MessageResult<>();
        messageResult.setSuccess(true);
        messageResult.setCode(200);
        return messageResult;
    }
    public static <T> MessageResult<T> success(T data) {
        MessageResult<T> messageResult = new MessageResult<>();
        messageResult.setSuccess(true);
        messageResult.setCode(200);
        messageResult.setData(data);
        return messageResult;
    }

    public static <T> MessageResult<T> faile(T data) {
        MessageResult<T> messageResult = new MessageResult<>();
        messageResult.setSuccess(false);
        messageResult.setCode(500);
        messageResult.setData(data);
        return messageResult;
    }

    public static <T> MessageResult<T> faile() {
        MessageResult<T> messageResult = new MessageResult<>();
        messageResult.setSuccess(false);
        messageResult.setCode(500);
        return messageResult;
    }

    public static <T> MessageResult<T> getMessageResult( T data,boolean success, int code) {
        MessageResult<T> messageResult = new MessageResult<>();
        messageResult.setSuccess(success);
        messageResult.setCode(code);
        messageResult.setData(data);
        return messageResult;
    }
}
