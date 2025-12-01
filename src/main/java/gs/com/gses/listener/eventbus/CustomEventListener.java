package gs.com.gses.listener.eventbus;


import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.enums.MqMessageSourceEnum;
import gs.com.gses.model.enums.MqMessageStatus;
import gs.com.gses.service.MqMessageService;
import gs.com.gses.service.ShipOrderService;
import gs.com.gses.service.TruckOrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import gs.com.gses.service.impl.UtilityConst;

import java.util.Arrays;
import java.util.List;

/**
 * 监听事件
 *默认使用 fanout 交换机，默认情况下，所有服务实例都会监听同一个 topic（springCloudBus），并且消息会广播给所有实例
 *
 *spring-cloud-starter-stream-rabbit:Spring Cloud Stream 默认使用 direct 交换机
 *
 * 队列
 *Queue springCloudBus.anonymous.fNkKbXtrREqNmqa0F9Y-hA in virtual host /
 *
 *
 * 如果mq 服务停止了，消息不会写入mq，可以监听到，分布式就监听不到了
 * 如果没有订阅：消息好像会自动ack 掉。在rabbitmq 管理页面没有看到消息
 *
 * binding key中可以存在两种特殊字符 * 与 # ，用于做模糊匹配，
 * 其中 * 用于匹配一个单词， # 用于匹配多个单词（可以是零个）
 */
@Slf4j
@Component
public class CustomEventListener {

    @Autowired
    MqMessageService mqMessageService;

    //multiplier 2 ,每次重试时间间隔翻倍
    @Async("threadPoolExecutor")
    @EventListener
//    Spring Retry 在最后一次重试失败后才会抛出异常
//    @Retryable(
//            value = {Exception.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1000, multiplier = 2)
//    )

    //    @Async("threadPoolExecutor") //使用异步和调用线程不在一个线程内
    //TransactionSynchronizationManager 事务成功之后发送
    @TransactionalEventListener //默认事务成功之后发送
//    @TransactionalEventListener  (phase = TransactionPhase.AFTER_COMMIT)
//    @EventListener  // 事务不成功也会检测到发送消息
    public void handleMyCustomEvent(CustomEvent event) throws Exception {
        //此处简单设计，失败了落表重试处理。或者重新设计本地消息表
        //ApplicationEventPublisher eventPublisher;
        //  eventPublisher.publishEvent(event);
        log.info("Received custom event: " + event);
//        Spring Retry 在最后一次重试失败后才会抛出异常
//        int m = Integer.parseInt("m");
        //处理完更新本地消息表，处理完成
        List<MqMessage> messageList = event.getMsg();

        for (MqMessage message : messageList) {
            try {


                if (message.getSendMq()) {
                    log.info("messageId {} SendMq,", message.getId());
                    mqMessageService.rePublish(Arrays.asList(message));
                } else {
                    mqMessageService.MqMessageEventHandler(message, MqMessageSourceEnum.EVENT);
                    int nn = 1;
                }
            } catch (Exception ex) {
                int n1 = 0;
            }
            int n = 0;

        }


    }

}




