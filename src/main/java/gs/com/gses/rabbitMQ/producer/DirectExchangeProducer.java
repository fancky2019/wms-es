package gs.com.gses.rabbitMQ.producer;


import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.rabbitMQ.CorrelationDataTag;
import gs.com.gses.rabbitMQ.RabbitMqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * rabbitmq默认消息、队列、交换机都是持久化：
 * 发送时候指定消息持久化（deliveryMode=2）、
 * 声明队列时持久化（durable字段设置为true）、
 * 声明交换机时持久化（durable字段设置为true）
 */
@Component
@Slf4j
public class DirectExchangeProducer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplateManualConfirmCallback;

    @Autowired
    private BatchingRabbitTemplate batchingRabbitTemplate;


    public void produce(RabbitMqMessage mqMessage) {
        String exchange = mqMessage.getExchange();
        String routingKey = mqMessage.getRouteKey();
//        String msgId = UUID.randomUUID().toString().replaceAll("-", "");
        MessageProperties messageProperties = new MessageProperties();
        String msgId = mqMessage.getMsgId();
        //设置优先级
        messageProperties.setPriority(9);
        messageProperties.setMessageId(msgId);
        messageProperties.setHeader("businessId", mqMessage.getBusinessId());
        messageProperties.setHeader("businessKey", mqMessage.getBusinessKey());
        messageProperties.setHeader("traceId", mqMessage.getTraceId());
        messageProperties.setHeader("retry", mqMessage.getRetry());
        //发送时候带上 CorrelationData(UUID.randomUUID().toString()),不然生产确认的回调中CorrelationData为空
        Message message = new Message(mqMessage.getMsgContent().getBytes(), messageProperties);
        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//        CorrelationData correlationData = new CorrelationData(msgId);
        CorrelationDataTag correlationData = new CorrelationDataTag(msgId, message);
//        //设置消息内容
//        ReturnedMessage returnedMessage = new ReturnedMessage(message, 0, "", "", "");
//        correlationData.setReturned(returnedMessage);
        log.info("BeforeRabbitTemplateSend msgId - {},businessKey - {} ,businessId - {}", mqMessage.getMsgId(), mqMessage.getBusinessKey(), mqMessage.getBusinessId());
        rabbitTemplate.send(exchange, routingKey, message, correlationData);
        log.info("AfterRabbitTemplateSend msgId - {},businessKey - {} ,businessId - {}", mqMessage.getMsgId(), mqMessage.getBusinessKey(), mqMessage.getBusinessId());

    }

    public void produce(RabbitMqMessage mqMessage, MessageProperties messageProperties) {
        String exchange = mqMessage.getExchange();
        String routingKey = mqMessage.getRouteKey();
//        String msgId = UUID.randomUUID().toString().replaceAll("-", "");
        if (messageProperties == null) {
            messageProperties = new MessageProperties();
        }

        String msgId = mqMessage.getMsgId();
        //设置优先级
        messageProperties.setPriority(9);
        messageProperties.setMessageId(msgId);
        messageProperties.setHeader("businessId", mqMessage.getBusinessId());
        messageProperties.setHeader("businessKey", mqMessage.getBusinessKey());
        messageProperties.setHeader("traceId", mqMessage.getTraceId());
        messageProperties.setHeader("retry", mqMessage.getRetry());
        messageProperties.setHeader("queueName", mqMessage.getQueue());
        //发送时候带上 CorrelationData(UUID.randomUUID().toString()),不然生产确认的回调中CorrelationData为空
        Message message = new Message(mqMessage.getMsgContent().getBytes(), messageProperties);
        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//        CorrelationData correlationData = new CorrelationData(msgId);
        CorrelationDataTag correlationData = new CorrelationDataTag(msgId, message);
//        //设置消息内容
//        ReturnedMessage returnedMessage = new ReturnedMessage(message, 0, "", "", "");
//        correlationData.setReturned(returnedMessage);
        log.info("BeforeRabbitTemplateSend msgId - {},businessKey - {} ,businessId - {}", mqMessage.getMsgId(), mqMessage.getBusinessKey(), mqMessage.getBusinessId());
        rabbitTemplate.send(exchange, routingKey, message, correlationData);
        log.info("AfterRabbitTemplateSend msgId - {},businessKey - {} ,businessId - {}", mqMessage.getMsgId(), mqMessage.getBusinessKey(), mqMessage.getBusinessId());

    }

    public void produceMqMessage(MqMessage mqMessage, MessageProperties messageProperties) {
        String exchange = mqMessage.getExchange();
        String routingKey = mqMessage.getRouteKey();
        if (messageProperties == null) {
            messageProperties = new MessageProperties();
        }

        String msgId = mqMessage.getMsgId();
        //设置优先级
        messageProperties.setPriority(9);
        messageProperties.setMessageId(msgId);
        messageProperties.setTimestamp(new Date());
        messageProperties.setHeader("businessId", mqMessage.getBusinessId().toString());
        messageProperties.setHeader("businessKey", mqMessage.getBusinessKey());
        messageProperties.setHeader("traceId", mqMessage.getTraceId());
        messageProperties.setHeader("retry", mqMessage.getRetry());
        messageProperties.setHeader("queueName", mqMessage.getQueue());
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        //发送时候带上 CorrelationData(UUID.randomUUID().toString()),不然生产确认的回调中CorrelationData为空
        Message message = new Message(mqMessage.getMsgContent().getBytes(), messageProperties);
        CorrelationDataTag correlationData = new CorrelationDataTag(msgId, message);
        //设置消息内容
//        ReturnedMessage returnedMessage = new ReturnedMessage(message, 0, "", "", "");
//        correlationData.setMessage(returnedMessage);
        log.info("BeforeRabbitTemplateSend msgId - {},businessKey - {} ,businessId - {}", mqMessage.getMsgId(), mqMessage.getBusinessKey(), mqMessage.getBusinessId());
        rabbitTemplate.send(exchange, routingKey, message, correlationData);
        log.info("AfterRabbitTemplateSend msgId - {},businessKey - {} ,businessId - {}", mqMessage.getMsgId(), mqMessage.getBusinessKey(), mqMessage.getBusinessId());


    }

    /**
     * 同步发送消息（使用 CorrelationData.getFuture() 方式）
     * @param
     * @return 是否发送成功
     * @throws Exception 发送异常
     */
    public boolean sendOrderedMessageSync(MqMessage mqMessage) {

        String exchange = mqMessage.getExchange();
        String routingKey = mqMessage.getRouteKey();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        String msgId = mqMessage.getMsgId();
        //设置优先级
        messageProperties.setPriority(9);
        messageProperties.setMessageId(msgId);
        //发送时间戳
        messageProperties.setTimestamp(new Date());

        //发送时候带上 CorrelationData(UUID.randomUUID().toString()),不然生产确认的回调中CorrelationData为空
        Message message = new Message(mqMessage.getMsgContent().getBytes(), messageProperties);
        //messageId
        message.getMessageProperties().setMessageId(mqMessage.getMsgId());

        // 1. 创建 CorrelationData，使用业务ID作为标识
//        CorrelationData correlationData = new CorrelationData(msgId);
        CorrelationDataTag correlationData = new CorrelationDataTag(msgId, message);
//        //设置消息内容。手动设置的 ReturnedMessage 会被实际回调时的返回值覆盖
//        ReturnedMessage returnedMessage = new ReturnedMessage(message, 0, "", "", "");
//        correlationData.setReturned(returnedMessage);

        // 2. 发送消息
        rabbitTemplateManualConfirmCallback.send(exchange, routingKey, message, correlationData);
        boolean success = false;

        // 3. 同步等待 Broker 的 confirm 确认（最多等待5秒）
        try {
            // getFuture() 返回 CompletableFuture<CorrelationData.Confirm>
            // 调用 get() 方法阻塞等待确认结果
            //等待最多 5 秒 来接收 broker 的确认（ACK/NACK）。
            //如果 5 秒内没有收到确认 → 抛出 TimeoutException
            CorrelationData.Confirm confirm = correlationData.getFuture().get(5, TimeUnit.SECONDS);
            //5 秒内没收到确认，不能说明消息没发送成功，实际消息最终发送到队列，造成消息重复投递。业务层做幂等处理。
            if (confirm.isAck()) {
                // 4. 发送成功，更新消息状态
//                updateSuccess(orderMessage);
                log.info("消息发送成功: " + msgId);
                success = true;
            } else {
                // 发送失败，Broker 返回 nack
                log.info("消息发送失败: " + confirm.getReason());
//                updateFailed(orderMessage, confirm.getReason());
                success = false;
            }

        } catch (TimeoutException e) {
            // 超时未收到确认
            log.info("消息发送超时: " + msgId);
//            updateTimeout(orderMessage);
            success = false;
        } catch (Exception e) {
            // 其他异常
            log.error("消息发送异常: ", e);
//            updateFailed(orderMessage, e.getMessage());
            success = false;
        }

        if (success) {


        } else {

        }
        return success;
    }


}
