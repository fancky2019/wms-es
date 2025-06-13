package gs.com.gses.rabbitMQ;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * 1、确认模式（confirm）：可以监听消息是否从生产者成功传递到交换。
 * 2、退回模式（return）：可以监听消息是否从交换机成功传递到队列。
 * 3、消费者消息确认（Ack）：可以监听消费者是否成功处理消息。
 */


/**
 * 确保发送到交换机，不确定路由到队列
 */
@Component
@Slf4j
public class PushConfirmCallback implements RabbitTemplate.ConfirmCallback {
    @Autowired
    ApplicationContext applicationContext;

    //无法注入 通过容器获取
//    @Autowired
//    IMqMessageService mqMessageService;
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String s) {
        try {
// s:channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'UnBindDirectExchange' in vhost '/', class-id=60, method-id=40)
            String msgId = correlationData.getId();
            MessageProperties messageProperties = correlationData.getReturned().getMessage().getMessageProperties();
            String businessKey = messageProperties.getHeader("businessKey");
            String businessId = messageProperties.getHeader("businessId");
            String traceId = messageProperties.getHeader("traceId");
            MDC.put("traceId", traceId);
            if (ack) {
                //发送消息时候指定的消息的id，根据此id设置消息表的消息状态为已发送
                log.info("ProduceSuccess msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);

//                从容器中获取bean
//                ApplicationContext applicationContext = ApplicationContextAwareImpl.getApplicationContext();

//                MqMessageService mqMessageService = applicationContext.getBean(MqMessageService.class);
//                LambdaUpdateWrapper<MqMessage> updateWrapper = new LambdaUpdateWrapper<>();
//                updateWrapper.set(MqMessage::getPublishAck, true);
//                updateWrapper.eq(MqMessage::getMsgId, msgId);//条件
//                mqMessageService.update(updateWrapper);

                //更新本地消息表，消息已经发送到mq
//                log.info("消息 - {} 发送到交换机成功！", msgId);
//                log.info("消息 - {} 发送到交换机成功！{}", msgId,"123");
            } else {
//                log.info("消息 - {} 发送到交换机失败！ ", msgId);
                log.info("ProduceFail msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);

            }
        } catch (Exception e) {
            log.error("", e);
            throw e;
        } finally {
            MDC.remove("traceId");

        }
    }

}
