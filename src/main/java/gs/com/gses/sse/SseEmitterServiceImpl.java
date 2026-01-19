package gs.com.gses.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SSE	                      WebScoket
 * http 协议	独立的             websocket 协议
 * 轻量，使用简单	              相对复杂
 * 默认支持断线重连	          需要自己实现断线重连
 * 文本传输	                  二进制传输
 * 支持自定义发送的消息类型
 *
 * 1.连接数控制：通过Nginx限制最大并发连接数
 * 2.内存泄漏防护：强制设置Emitter超时时间
 *
 *
 * 简单易用：SSE使用简单的API，只需要创建EventSource对象并监听事件即可实现实时通信。
 * 单向通信：SSE是一种单向通信方式，只允许服务器向客户端推送数据，而不支持客户端向服务器发送请求。
 * 实时性：SSE建立了持久连接，服务器可以随时向客户端发送更新的数据，实现实时的数据推送。
 * 自动重连：如果连接中断，SSE会自动尝试重新建立连接，确保持久连接的稳定性。
 * 支持事件流：SSE使用事件流（event stream）的格式来传输数据，可以发送不同类型的事件，方便客户端进行处理
 *
 *
 * websocket 双工，sse 单工
 *
 *
 *
 *
 * ResponseBodyEmitter、SseEmitter、StreamingResponseBody
 *
 * ResponseBodyEmitter:支持分块发送数据。适用于需要逐步生成响应的场景。
 * SseEmitter：派生自ResponseBodyEmitter.实时通知或更新
 * StreamingResponseBody：直接发送stream 不需要消息转成byte.适用于需要流式传输大文件的场景。
 *
 * ResponseBodyEmitter：适用于逐步发送数据的通用异步响应处理器。
 * SseEmitter：专门用于服务器推送事件（SSE），适合实时更新场景。
 * StreamingResponseBody：适用于流式传输二进制数据，如大文件下载。
 *
 * 访问 http://localhost:8081/sbp/user
 */
@Slf4j
@Service
public class SseEmitterServiceImpl implements ISseEmitterService {
//    StreamingResponseBody
    /**
     * 容器，保存连接，用于输出返回
     */
    private static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();


    /**
     * 当后台服务重启，http会和后台重连
     */
    @Override
    public SseEmitter createSseConnect(String userId) throws Exception {
        log.info("createSseConnect userId {}",userId);
        // 设置超时时间，0表示不过期。默认30秒，超过时间未完成会抛出异常：AsyncRequestTimeoutException
        SseEmitter sseEmitter = new SseEmitter(0L);//建议和会话时长保持一致
        // 是否需要给客户端推送ID

        // 注册回调
        sseEmitter.onCompletion(completionCallBack(userId));
        sseEmitter.onError(errorCallBack(userId));
        sseEmitter.onTimeout(timeoutCallBack(userId));
        //        emitter.onTimeout(() -> emitter.complete());

        sseCache.put(userId, sseEmitter);
        log.info("创建新的sse连接，当前用户：{}", userId);
        //SSE协议特性：客户端（Postman）在收到第一个 data: 格式的响应后，才会认为SSE连接真正建立
        //发送一个连接成功消息SSE_CONNECTED给前端。
        sseEmitter.send(SseEmitter.event().id("USER_ID").data("SSE_CONNECTED"));
        return sseEmitter;
    }

    @Override
    public void pushTest(String userId) {
        CompletableFuture.runAsync(() ->
        {
            while (true) {
                String msg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                sendMsgToClient(userId, msg);
            }
        });
    }

    /**
     * 群发所有人
     *
     * @param msg
     */
    @Override
    public void batchSendMessage(String msg) {
        sseCache.forEach((k, v) -> {
            SseEmitter sseEmitter = sseCache.get(k);
            if (sseEmitter != null) {
                sendMsgToClientByUserId(k, msg, sseEmitter);
            }
        });
        // return null;
    }

    /**
     * 根据客户端id关闭SseEmitter对象
     *
     * @param userId
     */
    @Override
    public void closeSseConnect(String userId) {
        SseEmitter sseEmitter = sseCache.get(userId);
        if (sseEmitter != null) {
            //设置完成状态，触发completionCallBack。建立连接时候注册sseEmitter.onCompletion(completionCallBack(userId));
            sseEmitter.complete();
//            removeUser(userId);
        }
    }


    /**
     * 根据客户端id获取SseEmitter对象
     *
     * @param userId
     * @return
     */
    @Override
    public SseEmitter getSseEmitterByUserId(String userId) {
        return sseCache.get(userId);
    }


    /**
     * 推送消息到客户端，此处结合业务代码，业务中需要推送消息处调用即可向客户端主动推送消息
     *
     * @param
     * @param
     * @return
     */
    @Override
    public SseEmitter sendMsgToClient(String userId, String msg) {
        SseEmitter sseEmitter = sseCache.get(userId);
        if (sseEmitter == null) {
            log.info("userId - {} : sse  is not connected ", userId);
        } else {
            sendMsgToClientByUserId(userId, msg, sseEmitter);
            return sseEmitter;
        }
        return null;
    }


    /**
     * 推送消息到客户端
     * 此处做了推送失败后，重试推送机制，可根据自己业务进行修改
     *
     * @param userId 客户端ID
     * @param
     * @author re
     * @date 2022/3/30
     **/
    private void sendMsgToClientByUserId(String userId, String msg, SseEmitter sseEmitter) {
        if (sseEmitter == null) {
            log.error("[sendMsgToClient]: 推送消息失败：客户端{}未创建长链接,失败消息:{}", userId, msg);
            return;
        }

        SseEmitter.SseEventBuilder sendData = SseEmitter.event().id("TASK_RESULT").data(msg, MediaType.APPLICATION_JSON);
        try {
            sseEmitter.send(sendData);
        } catch (IOException e) {
            // 推送消息失败，记录错误日志，进行重推
            log.warn("[sendMsgToClient]: 推送消息失败：{},尝试进行重推", msg);
            removeUser(userId);

            // 出现异常时结束响应并传递错误信息
            sseEmitter.completeWithError(e);
        }
    }

    /**
     * 长链接完成后回调接口(即关闭连接时调用)
     *
     * @param userId 客户端ID
     * @return java.lang.Runnable
     * @author re
     * @date 2021/12/14
     **/
    private Runnable completionCallBack(String userId) {
        return () -> {
            log.info("结束连接：{}", userId);
            removeUser(userId);
        };
    }

    /**
     * 连接超时时调用
     *
     * 连接关闭：务必确保在任务结束时调用complete()或completeWithError()方法，
     * 否则可能导致连接无法正常关闭，造成资源浪费。
     *
     *
     *
     * @param userId 客户端ID
     * @return java.lang.Runnable
     * @author re
     * @date 2021/12/14
     **/
    private Runnable timeoutCallBack(String userId) {
        return () -> {
            log.info("连接超时：{}", userId);
            removeUser(userId);
        };
    }

    /**
     * 推送消息异常时，回调方法
     *
     * @param userId 客户端ID
     * @return java.util.function.Consumer<java.lang.Throwable>
     * @author re
     * @date 2021/12/14
     **/
    private Consumer<Throwable> errorCallBack(String userId) {
        return throwable -> {
            log.error("SseEmitterServiceImpl[errorCallBack]：连接异常,客户端ID:{}", userId);
            removeUser(userId);
        };
    }


    /**
     * 移除用户连接
     *
     * @param userId 客户端ID
     * @author re
     * @date 2021/12/14
     **/
    private void removeUser(String userId) {
        SseEmitter sseEmitter = sseCache.get(userId);
        if (sseEmitter != null) {
            //设置完成状态，触发completionCallBack。建立连接时候注册sseEmitter.onCompletion(completionCallBack(userId));
            sseEmitter.complete();
//            removeUser(userId);
        }
        sseCache.remove(userId);
        log.info("SseEmitterServiceImpl[removeUser]:移除用户：{}", userId);
    }
}
