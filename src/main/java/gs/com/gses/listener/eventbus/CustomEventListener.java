package gs.com.gses.listener.eventbus;


import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.enums.MqMessageSourceEnum;
import gs.com.gses.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

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
    @Autowired
    private TransactionTemplate transactionTemplate;


    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @Async 线程中：当前线程没有任何事务同步器 → Spring 认为“无法开事务”
     *@Async 异步线程：
     * 切换了线程
     * ThreadLocal 丢失
     * 没有事务同步器
     * @Transactional 无法创建事务
     *
     *
     *
     *
     * @param event
     * @throws Exception
     */

    //multiplier 2 ,每次重试时间间隔翻倍
//    @Async("threadPoolExecutor")
//    @EventListener
//    Spring Retry 在最后一次重试失败后才会抛出异常
//    @Retryable(
//            value = {Exception.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1000, multiplier = 2)
//    )

    @Async("threadPoolExecutor") //使用异步和调用线程不在一个线程内
    //TransactionSynchronizationManager 事务成功之后发送
    @TransactionalEventListener //默认事务成功之后发送
//    @TransactionalEventListener  (phase = TransactionPhase.AFTER_COMMIT)
//    @EventListener  // 事务不成功也会检测到发送消息
//    @Transactional(propagation = Propagation.REQUIRED) // @Async+@TransactionalEventListener会使service 层方法的事务失效
    public void handleMyCustomEvent(CustomEvent event) throws Exception {
        //此处简单设计，失败了落表重试处理。或者重新设计本地消息表
        //ApplicationEventPublisher eventPublisher;
        //  eventPublisher.publishEvent(event);
        log.info("Received custom event: {}" ,objectMapper.writeValueAsString(event));
//        Spring Retry 在最后一次重试失败后才会抛出异常
//        int m = Integer.parseInt("m");
        //处理完更新本地消息表，处理完成
        List<MqMessage> messageList = event.getMsg();
        log.info("messageList size : {}" ,messageList.size());
        for (MqMessage message : messageList) {
            try {


//                因为 MqMessageEventHandler 是在 @Async 的线程池线程中执行的，而
//                @Transactional 依赖当前线程的事务同步器(TransactionSynchronizationManager.isSynchronizationActive())，
//                但异步线程没有事务同步器，因此事务不会被创建

//                mqMessageService.MqMessageEventHandler(message, MqMessageSourceEnum.EVENT);
//                int nn = 1;








                //@Async + @Transactional 事务不生效
                //        事务模板方法
                // 在这里执行事务性操作
                // 操作成功则事务提交，否则事务回滚
                Exception e = transactionTemplate.execute(transactionStatus -> {

                    // 事务性操作
                    // 如果操作成功，不抛出异常，事务将提交

                    try {

                        if (message.getSendMq()) {
                            log.info("messageId {} SendMq,", message.getId());
                            mqMessageService.rePublish(Arrays.asList(message));
                        } else {
                            mqMessageService.MqMessageEventHandler(message, MqMessageSourceEnum.EVENT);
                            int nn = 1;
                        }

                        return null;
                    } catch (Exception ex) {
                        log.info("executing consume message {} fail", message.getId());
                        log.error("", ex);
                        // 如果操作失败，抛出异常，事务将回滚
                        transactionStatus.setRollbackOnly();
                        return ex;
                        //此处是定时任务 ，处理异常不抛出
//                    transactionStatus.setRollbackOnly();
//                    throw  e;
                    }


//        TransactionCallbackWithoutResult

                });


//                if (e != null) {
//                    //将该条事务的异常保存
//                    transactionTemplate.execute(transactionStatus -> {
//                        try {
//                            //失败了就更新一下版本号和更新时间，根据更新时间的 索引 提高查询速度
//                            message.setRetryCount(message.getRetryCount() + 1);
//                            message.setFailureReason(e.getMessage());
//                            this.update(message);
//                            return true;
//                        } catch (Exception ex) {
//                            log.error("", ex);
//                            transactionStatus.setRollbackOnly();
//                            return false;
//                        }
//                    });
//                }


            } catch (Exception ex) {
               log.error("",ex);
            }
            int n = 0;

        }


    }

}




