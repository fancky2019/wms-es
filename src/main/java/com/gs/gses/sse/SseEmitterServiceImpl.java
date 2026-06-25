package com.gs.gses.sse;

import com.gs.gses.model.response.wms.TruckOrderResponse;
import com.gs.gses.model.utility.RedisKeyConfigConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
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
 * websocket 双工，sse 单工。sse 基于http :HTTP/1.1 或 HTTP/2 (单向流)
 *
 *
 * SignalR :HTTP (握手) + WebSocket (通信)
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
 *
 *
 * 使用此实现
 *
 *
 * // 在浏览器 Console 中执行，在network中这条http://localhost:8088/api/truckOrder/sseConnect/?token
 * 请求的连接中EventStream可以看到后台发送的sse消息
 *
 * EventSource 默认重连间隔是 3 秒
 *es.readyState：0=连接中，1=已连接，2=已关闭
 *
 *
 *
 *
 *
 const testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI4NDBhZWQ1ZS0xZTM2LTViYTctNWY2NS0zYTBkYjljMDNkN2MiLCJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1laWRlbnRpZmllciI6Ijg0MGFlZDVlLTFlMzYtNWJhNy01ZjY1LTNhMGRiOWMwM2Q3YyIsInByZWZlcnJlZF91c2VybmFtZSI6ImFkbWluIiwiZ2l2ZW5fbmFtZSI6Iuezu-e7n-euoeeQhuWRmCIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6WyJhZG1pbmlzdHJhdG9yIiwidGVzdCJdLCJyb2xlIjpbImFkbWluaXN0cmF0b3IiLCJ0ZXN0Il0sIm5iZiI6MTc3ODQ2NDk1NCwiZXhwIjoxNzc4NTUxMzU0LCJpc3MiOiJBdXRob3JpemVTU08iLCJhdWQiOiJBdXRob3JpemVTU08ifQ.7hlsnFs8S07s86ftKQeYlxa70mW27KMJ1ri5YH_OWeQ";
 const testUrl = 'http://localhost:8088/api/truckOrder/sseConnect/?token=' + encodeURIComponent(testToken);
 const es = new EventSource(testUrl);

 es.onopen = () => console.log('✅ 连接成功');
 es.onerror = (e) => console.log('❌ 错误, readyState:', es.readyState);

 //// 接收所有消息
 //es.onmessage = (e) => {
 //        //debugger
 //        console.log('📨 数据:', e.data);
 //// 解析JSON数据JSON.parse(e.data);
 //    const data = e.data;
 //    switch(data.type) {
 //        case 'connected':
 //        console.log('🔗 已连接:', data);
 //            break;
 //                    case 'heartbeat':
 //                    console.log('💓 心跳:', data);
 //            break;
 //                    case 'order':
 //                    console.log('📦 订单:', data);
 //            break;
 //default:
 //        console.log('📨 其他:', data);
 //    }
 //            };

 // 方式1：onmessage - 只能收到e.type=message的消息，后台不指定name 默认值message,指定空字符串 name("") 会被当作默认的 message 事件处理。 ，收不到心跳heartbeat
 es.onmessage = (e) => {
 //   debugger;
 console.log(`e.type: ${e.type}, onmessage 收到:`, e.data);
 };

 // 方式2：addEventListener - 能收到特定事件名的消息
 es.addEventListener('message', (e) => {
 console.log(`e.type: ${e.type}, message 事件收到:`, e.data);
 });

 es.addEventListener('heartbeat', (e) => {
 console.log(`e.type: ${e.type}, heartbeat 事件收到:`, e.data);
 });
 *
 *
 *
 *
 *  EventSource 不支持 Header,社区广泛接受用 URL 参数传 token
 *
 */


@Slf4j
@Service
public class SseEmitterServiceImpl implements ISseEmitterService {
//    StreamingResponseBody
    /**
     * 容器，保存连接，用于输出返回
     */
    public static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();
    @Autowired
    private RedisTemplate redisTemplate;

    //    @PostConstruct
    public void init() {


    }


    /**
     * 当后台服务重启，http会和后台重连
     */
    @Override
    public SseEmitter createSseConnect(String userId) throws Exception {
        log.info("createSseConnect userId {}", userId);
        // 设置超时时间，0表示不过期。默认30秒，超过时间未完成会抛出异常：AsyncRequestTimeoutException
        SseEmitter sseEmitter = new SseEmitter(0L);//建议和会话时长保持一致
        // 是否需要给客户端推送ID

        // 注册回调
        sseEmitter.onCompletion(completionCallBack(userId));
        sseEmitter.onError(errorCallBack(userId));
        sseEmitter.onTimeout(timeoutCallBack(userId));
        //        emitter.onTimeout(() -> emitter.complete());

        sseCache.put(userId, sseEmitter);
        log.info("CreateSseConnectSuccess：{} sseCache Size {}", userId, sseCache.size());
        //SSE协议特性：客户端（Postman）在收到第一个 data: 格式的响应后，才会认为SSE连接真正建立
        //发送一个连接成功消息CONNECTED给前端。
        // name 事件类型：业务消息类型，前端好做区分处理
        sseEmitter.send(SseEmitter.event().id("USER_ID").name(SseConst.CONNECTED).data("SSE_CONNECTED"));
        return sseEmitter;
    }

    @Override
    public void pushTest(String userId) {
        CompletableFuture.runAsync(() ->
        {
            int i=0;
            while (i<20) {
                String msg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                log.info("pushTest userId：{}", userId);
                sendMsgToClient(userId, "", msg);
                i++;
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
                sendMsgToClientByUserId(k, "", msg, sseEmitter);
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
    public SseEmitter sendMsgToClient(String userId, String eventName, String msg) {
        SseEmitter sseEmitter = sseCache.get(userId);
        if (sseEmitter == null) {
            log.info("sendMsgToClient userId - {} : sse  is not connected ", userId);
        } else {
            log.info("sendMsgToClient userId：{}", userId);
            sendMsgToClientByUserId(userId, eventName, msg, sseEmitter);
            return sseEmitter;
        }
        return null;
    }

    @Override
    public void sseHeartbeat() {
        for (Map.Entry<String, SseEmitter> entry : sseCache.entrySet()) {
            String userId = entry.getKey();
            SseEmitter sseEmitter = entry.getValue();

            if (sseEmitter != null) {
                sendHeartbeatToClient(userId, sseEmitter);
            }
        }
    }

    @Override
    public void clearConnectCache() {
        // 关闭所有 SSE 连接后再清空
        for (SseEmitter emitter : sseCache.values()) {
            try {
                // 正常完成, 或者 emitter.completeWithError(new RuntimeException("服务端重置连接"));
                emitter.complete();
            } catch (Exception e) {
                log.warn("关闭SSE连接失败", e);
            }
        }
        sseCache.clear();

        // 使用 StringRedisTemplate
        Set<String> keys = redisTemplate.keys(RedisKeyConfigConst.BLACKLIST_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

    }

    /**
     * 向单个客户端发送心跳
     *
     * @param userId 用户ID
     * @param sseEmitter SSE连接
     * @return 是否发送成功
     */
    private boolean sendHeartbeatToClient(String userId, SseEmitter sseEmitter) {
        try {
            // 构建心跳事件，使用特定的事件类型便于客户端识别
            SseEmitter.SseEventBuilder heartbeatEvent = SseEmitter.event()
                    .id(generateHeartbeatId())
                    .name(SseConst.HEARTBEAT)  //事件类型（event type）标签. 设置事件名称，客户端可以通过addEventListener监听
                    .data(SseConst.HEARTBEAT_MESSAGE, MediaType.TEXT_PLAIN);

            sseEmitter.send(heartbeatEvent);
            log.info("sendHeartbeatSuccess:userId={}", userId);
            return true;
        } catch (IOException e) {
            // 心跳发送失败，说明连接可能已经断开，移除该连接
            log.warn("sendHeartbeatFail! remove connection:userId={},Cause:{}", userId, e.getMessage());
            removeUser(userId);
            return false;
        }
    }

    /**
     * 生成心跳消息ID
     */
    private String generateHeartbeatId() {
        return "heartbeat_" + System.currentTimeMillis();
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
    private void sendMsgToClientByUserId(String userId, String eventName, String msg, SseEmitter sseEmitter) {
        if (sseEmitter == null) {
            log.error("sendMsgToClientByUserId: push fail：client - {} disconnect, cause:{}", userId, msg);
            return;
        }
        //默认 message .指定空字符串 name("") 会被当作默认的 message 事件处理。
        if (StringUtils.isEmpty(eventName)) {
            eventName = SseConst.MESSAGE;
        }
        log.info("sendMsgToClientByUserId：userId - {} eventName - {}", userId,eventName);
        SseEmitter.SseEventBuilder sendData = SseEmitter.event().id("TASK_RESULT")
                .name(eventName)  // 事件类型：业务消息类型，前端好做区分处理
                .data(msg, MediaType.APPLICATION_JSON);
        try {
            sseEmitter.send(sendData);
        } catch (IOException e) {
            // 推送消息失败，记录错误日志，进行重推
            log.warn("sendMsgToClientByUserId: push fail：{}", msg);
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
            log.info("Disconnect：client {}", userId);
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
            log.info("Connect timeout：{}", userId);
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
            log.warn("SseEmitterServiceImpl[errorCallBack]:Connection error,client:{}", userId);
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
        log.info("SseEmitterServiceImpl[removeUser]:remove client：{}", userId);
    }
}
