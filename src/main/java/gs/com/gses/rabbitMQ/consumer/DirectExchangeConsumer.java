package gs.com.gses.rabbitMQ.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.utility.RedisKey;
import gs.com.gses.rabbitMQ.BaseRabbitMqHandler;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
import gs.com.gses.service.InventoryInfoService;
import gs.com.gses.service.ShipOrderService;
import gs.com.gses.service.TruckOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
//@RabbitListener(queues = "DirectExchangeQueueSpringBoot")//参数为队列名称
public class DirectExchangeConsumer extends BaseRabbitMqHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryInfoService inventoryInfoService;

    @Autowired
    private ShipOrderService shipOrderService;

    @Autowired
    private TruckOrderService truckOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

//    private static Logger logger = LogManager.getLogger(DirectExchangeConsumer.class);


    //发送什么类型就用什么类型接收
    //sac:x-single-active-consumer=true (默认false)多个服务监听一个队列，只会有一个服务接受到消息，
    //只有当这个服务挂了，其他服务才能接受到消息，可用于主备模式
    //rabbitmq 默认轮训向所有服务中的一个发送消息
    //多个方法绑定同一个队列MQ会轮训发送给各个方法消费
    //string 接收
    @RabbitHandler
    @RabbitListener(queues = RabbitMQConfig.DIRECT_QUEUE_NAME)
    public void receivedMsg(Message message, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                            @Header(AmqpHeaders.CONSUMER_QUEUE) String queueName) throws Exception {
//        MessageProperties messageProperties = message.getMessageProperties();
//        String businessKey = messageProperties.getHeader("businessKey");
//        String businessId = messageProperties.getHeader("businessId");
//        String msgId = messageProperties.getMessageId();
//        String traceId = messageProperties.getHeader("traceId");
//        try {
//            MDC.put("traceId", traceId);
//            log.info("receiveMessage msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);
//            String msgContent = new String(message.getBody());
//            DataChangeInfo dataChangeInfo = objectMapper.readValue(msgContent, DataChangeInfo.class);
//            inventoryInfoService.sink(dataChangeInfo);
//            log.info("ConsumeSuccess msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);
//
//
////            super.onMessage(DataChangeInfo.class,message, channel, (msg) -> {
////
////                try {
////                    inventoryInfoService.sink(msg);
////                } catch (JsonProcessingException e) {
////                    throw new RuntimeException(e);
////                } catch (InterruptedException e) {
////                    throw new RuntimeException(e);
////                }
////
////            });
//
//        } catch (Exception e) {
//            log.error("ConsumeFail msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);
//            log.error("", e);
//            throw e;
//        } finally {
//            //都ack
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            MDC.remove("traceId");
//
//        }


        super.onMessage(DataChangeInfo.class, message, channel, (msg) -> {
            try {
                switch (msg.getTableName()) {
                    case "ShipOrder":
                        shipOrderService.sink(msg);
                        break;
                    default:
                        //region DELETE
                        if ("DELETE".equals(msg.getEventType())) {
                            switch (msg.getTableName()) {

                                case "Inventory":
                                case "InventoryItem":
                                case "InventoryItemDetail":
                                   LocalDateTime changeTime = LocalDateTime.ofInstant(
                                            Instant.ofEpochMilli(msg.getChangeTime()),
                                            ZoneId.systemDefault()
                                    );
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                                    String changeTimeStr = changeTime.format(formatter);
                                    DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyyMM");
                                    String formatterDateStr = changeTime.format(formatterDate);
                                    String key = MessageFormat.format("{0}{1}:{2}:{3}", RedisKey.INVENTORY_DELETED, formatterDateStr,msg.getTableName(), msg.getId());

                                    redisTemplate.opsForValue().set(key, changeTimeStr, 365,  TimeUnit.DAYS);
                                    break;
                                default:
                                    break;
                            }
                        }
                        //endregion

                        inventoryInfoService.sink(msg);
                        break;
                }
            } catch (Exception e) {
                String json = null;
                try {
                    json = objectMapper.writeValueAsString(msg);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
                log.error("DataChangeInfo:{}", json);
                throw new RuntimeException(e);
            }

        });

    }

    @RabbitHandler
    @RabbitListener(queues = RabbitMQConfig.DIRECT_MQ_MESSAGE_NAME)
    public void mqMessage(Message message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                          @Header(AmqpHeaders.CONSUMER_QUEUE) String queueName) {


        super.onMessage(Long.class, message, channel, (msg) -> {
            try {
                truckOrderService.expungeStaleAttachment(msg);
            } catch (Exception e) {
                throw new RuntimeException("expungeStaleAttachment: " + msg, e);
            }
        });

    }

}
