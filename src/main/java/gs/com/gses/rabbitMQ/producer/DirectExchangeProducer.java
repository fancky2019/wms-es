package gs.com.gses.rabbitMQ.producer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
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

import java.util.UUID;


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

        CorrelationData correlationData = new CorrelationData(msgId);
        //设置消息内容
        ReturnedMessage returnedMessage = new ReturnedMessage(message, 0, "", "", "");
        correlationData.setReturned(returnedMessage);
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
        //发送时候带上 CorrelationData(UUID.randomUUID().toString()),不然生产确认的回调中CorrelationData为空
        Message message = new Message(mqMessage.getMsgContent().getBytes(), messageProperties);

        CorrelationData correlationData = new CorrelationData(msgId);
        //设置消息内容
        ReturnedMessage returnedMessage = new ReturnedMessage(message, 0, "", "", "");
        correlationData.setReturned(returnedMessage);
        log.info("BeforeRabbitTemplateSend msgId - {},businessKey - {} ,businessId - {}", mqMessage.getMsgId(), mqMessage.getBusinessKey(), mqMessage.getBusinessId());
        rabbitTemplate.send(exchange, routingKey, message, correlationData);
        log.info("AfterRabbitTemplateSend msgId - {},businessKey - {} ,businessId - {}", mqMessage.getMsgId(), mqMessage.getBusinessKey(), mqMessage.getBusinessId());

    }

}
