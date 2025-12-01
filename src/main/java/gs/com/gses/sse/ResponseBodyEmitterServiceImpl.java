package gs.com.gses.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 传统的 HTTP 请求 - 响应模式中，服务器通常需要等待整个响应数据生成完成后，才会将其一次性发送给客户端
 *
 *
 * 将数据分成多个独立的块，每个块都有自己的长度标识。
 *当有部分数据准备好时，就可以立即调用 send()方法将这些数据推送给客户端,ai 就是这种模式
 *
 *
 * ResponseBodyEmitter采用了 HTTP 的分块编码（Chunked Encoding）方式来传输数据。
 * 在传统的 HTTP 响应中，通常需要在响应头中明确指定Content-Length，表示整个响应数据的长度。
 * 但在分块传输中，服务器不会提前设置Content-Length，而是将数据分成多个独立的块，每个块都有自己的长度标识。
 *
 *
 *
 *
 *
 * 需要调用complete()方法来明确告知客户端响应结束，关闭连接。结束响应并向客户端传递错误信息。
 * 这样可以避免连接长时间保持开放，造成资源浪费。
 * 如果在数据传输过程中出现异常，可以调用completeWithError()方法，
 *
 *
 *
 *
 * Streaming：直接通过OutputStream向客户端写入数据，灵活性较高，但需要手动处理流的关闭，增加了开发的复杂度。
 * Server-Sent Events (SSE)：基于text/event-stream协议，适用于服务端事件推送场景，但要求客户端支持 SSE 协议。
 * ResponseBodyEmitter：通用性更强，适用于任何支持 HTTP 的客户端，并且易于与 Spring 框架集成，
 *                     是一种更为便捷的流式传输解决方案。
 *
 *测试：
 * tabby 控制台执行  curl http://localhost:8081/sbp/utility/createResponseBodyEmitterConnect/1
 *                  会收到后台发送的数据
 *
 *
 * 前段获取后台推送的数据 访问 http://localhost:8081/sbp/utility/createResponseBodyEmitterConnect/2
 * EventSource：适用于服务器推送（text/event-stream），简单易用。 sse
 * Fetch API：适用于流式数据，灵活性高。ResponseBodyEmitter
 *
 *
 * EventSource 仅适用于服务器推送事件（SSE），且要求服务器返回的响应格式符合 SSE 规范（如 Content-Type: text/event-stream）。
 * 使用 fetch 的流式 API：
 */
@Slf4j
@Service
public class ResponseBodyEmitterServiceImpl implements IResponseBodyEmitterService {

    /**
     * 容器，保存连接，用于输出返回
     */
    private static Map<String, ResponseBodyEmitter> sseCache = new ConcurrentHashMap<>();


    @Override
    public ResponseBodyEmitter createResponseBodyEmitterConnect(String userId) throws Exception {
        // 设置超时时间，0表示不过期。默认30秒，超过时间未完成会抛出异常：AsyncRequestTimeoutException
        ResponseBodyEmitter sseEmitter = new ResponseBodyEmitter(0L);//建议和会话时长保持一致
        //-1不过期
//        ResponseBodyEmitter sseEmitter = new ResponseBodyEmitter(-1L);
        // 是否需要给客户端推送ID

        // 注册回调
        sseEmitter.onCompletion(completionCallBack(userId));
        sseEmitter.onError(errorCallBack(userId));
        sseEmitter.onTimeout(timeoutCallBack(userId));
//        emitter.onTimeout(() -> emitter.complete());
        sseCache.put(userId, sseEmitter);
        log.info("创建新的sse连接，当前用户：{}", userId);

        try {
            sseEmitter.send(userId);
        } catch (IOException e) {
            log.error("SseEmitterServiceImpl[createSseConnect]: 创建长链接异常，客户端ID:{}", userId, e);
            throw new Exception("创建连接异常！", e);
        }


        //20s后推送数据给前段
        CompletableFuture.runAsync(() ->
        {
            int i = 0;
            while (i < 10) {
                String msg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                batchSendMessage(msg);
                ++i;
            }
        });

        return sseEmitter;
    }

    @Override
    public void closeResponseBodyEmitterConnect(String userId) {
        ResponseBodyEmitter sseEmitter = sseCache.get(userId);
        if (sseEmitter != null) {
            //设置完成状态，触发completionCallBack。建立连接时候注册sseEmitter.onCompletion(completionCallBack(userId));
            sseEmitter.complete();
//            removeUser(userId);
        }
    }

    @Override
    public void batchSendMessage(String msg) {
        sseCache.forEach((k, v) -> {
            ResponseBodyEmitter sseEmitter = sseCache.get(k);
            if (sseEmitter != null) {
                sendMsgToClientByUserId(k, msg, sseEmitter);
            }
        });
    }

    @Override
    public ResponseBodyEmitter getResponseBodyEmitterByUserId(String userId) {
        return sseCache.get(userId);
    }

    @Override
    public ResponseBodyEmitter sendMsgToClient(String userId, String msg) {
        ResponseBodyEmitter sseEmitter = sseCache.get(userId);
        if (sseEmitter != null) {
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
    private void sendMsgToClientByUserId(String userId, String msg, ResponseBodyEmitter sseEmitter) {
        if (sseEmitter == null) {
            log.error("ResponseBodyEmitterServiceImpl[sendMsgToClient]: 推送消息失败：客户端{}未创建长链接,失败消息:{}",
                    userId, msg);
            return;
        }

        try {
            sseEmitter.send(msg);
        } catch (IOException e) {
            // 推送消息失败，记录错误日志，进行重推
            log.warn("SseEmitterServiceImpl[sendMsgToClient]: 推送消息失败：{},尝试进行重推", msg);
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
        sseCache.remove(userId);
        log.info("SseEmitterServiceImpl[removeUser]:移除用户：{}", userId);
    }
}
