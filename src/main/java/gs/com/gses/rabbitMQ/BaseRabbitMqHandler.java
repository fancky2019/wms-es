package gs.com.gses.rabbitMQ;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.model.enums.MqMessageStatus;
import gs.com.gses.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * @author ruili
 */
@Slf4j
public class BaseRabbitMqHandler {

//    private static final Logger logger = LoggerFactory.getLogger(BaseRabbitMqHandler.class);

    private static final String RABBIT_MQ_MESSAGE_ID_PREFIX = "RabbitMQ:messageId:";
    //
    public static final int TOTAL_RETRY_COUNT = 4;
    /**
     * 一天
     */
    private static final int EXPIRE_TIME = 24 * 60 * 60;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Autowired
//    IMqFailLogService mqFailLogService;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    public <T> void onMessage(Class<T> tClass, Message message, Channel channel, Consumer<T> consumer) {

        MessageProperties messageProperties = message.getMessageProperties();
        String businessKey = messageProperties.getHeader("businessKey");
        String businessId = messageProperties.getHeader("businessId");
        String msgId = messageProperties.getMessageId();
        String traceId = messageProperties.getHeader("traceId");
        Boolean retry = messageProperties.getHeader("retry");

        int retryCount = 0;
        String msgContent = null;
        StopWatch stopWatch = new StopWatch("onMessage");
        stopWatch.start("onMessage");
        String mqMsgIdKey = RABBIT_MQ_MESSAGE_ID_PREFIX + msgId;
        try {
            MDC.put("traceId", traceId);
            log.info("StartConsumeMessage msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);

            msgContent = new String(message.getBody());


            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

            //添加重复消费redis 校验，不会存在并发同一个message
            Object retryCountObj = valueOperations.get(mqMsgIdKey);


            if (retryCountObj == null) {
                //value 重试次数
                valueOperations.set(mqMsgIdKey, 0);
            } else {
                //没有过期时间,说明没有消费成功
                if (redisTemplate.getExpire(mqMsgIdKey) == -1) {
                    retryCount = (int) retryCountObj;
                    //没有重试
                    if (retryCount == 0) {
                        long deliveryTag = message.getMessageProperties().getDeliveryTag();
                        //补偿 ack--消费了却没有ack 成功。
                        channel.basicAck(deliveryTag, false);
                        log.info("msgId - {} has been consumed,msg - {}", msgId, msgContent);
                        return;
                    }
                } else {
                    log.info("msgId - {} has been consumed,msg - {}", msgId, msgContent);
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    return;
                }
            }
            T t = objectMapper.readValue(msgContent, tClass);
            consumer.accept(t);
            log.info("ConsumeSuccess msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);
            //消费成功设置过期时间删除key.
            if (redisTemplate.expire(mqMsgIdKey, EXPIRE_TIME, TimeUnit.SECONDS)) {
                log.info("SetExpire mqMsgIdKey - {} success", mqMsgIdKey);
            } else {
                //会存在expire 失败，ack 失败情况
                log.info("SetExpire mqMsgIdKey - {} fail", mqMsgIdKey);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("AckSuccess msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);
            mqMessageService.updateByMsgId(msgId, MqMessageStatus.CONSUMED.getValue());
        } catch (Exception e) {
            log.error("ConsumerFail msgId - {},businessKey - {} ,businessId - {} retry - {}", msgId, businessKey, businessId,retry);
            log.error("", e);
            if (!retry) {
                try {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    //不重试，设置过期删除key
                    redisTemplate.expire(mqMsgIdKey, EXPIRE_TIME, TimeUnit.SECONDS);
                    log.error("ConsumerFailAckSuccess msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);

                } catch (Exception ex) {
                    log.error("ConsumerFailAckFail msgId - {},businessKey - {} ,businessId - {}", msgId, businessKey, businessId);
                }
            } else {
                try {
                    /**
                     * deliveryTag:该消息的index
                     * multiple：是否批量.true:将一次性拒绝所有小于deliveryTag的消息。
                     * requeue：被拒绝的是否重新入队列
                     */
                    //channel.basicNack(deliveryTag, false, true);
                    this.retry(channel, message, retryCount, e.getMessage());
                } catch (Exception ex) {
                    log.info("retryException", ex);
                }
            }
        } finally {
            try {
                String queueName = message.getMessageProperties().getConsumerQueue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
                String nowStr = LocalDateTime.now().format(formatter);
                String key = "RabbitMQ:Consume:" + queueName + ":" + nowStr;
                Long newValue = redisTemplate.opsForValue().increment(key);
                if (newValue == 1) {
                    redisTemplate.expire(key, 24 * 12 * 60, TimeUnit.SECONDS);
                }
            } catch (Exception ex) {
                log.error("", ex);
            }

            stopWatch.stop();
            long mills = stopWatch.getTotalTimeMillis();
            log.info("EndConsumeMessage msgId - {},businessKey - {} ,businessId - {} ,ConsumeMessageCostTime - {}", msgId, businessKey, businessId, mills);
            MDC.remove("traceId");

        }

    }

    private void retry(Channel channel, Message message, int retryCount, String exceptionMsg) throws Exception {
        //   String redisCountKey = "retry:" + RabbitMqConstants.TB_CUST_LIST_ERROR_QUEUE + t.getMessageId();

        String messageId = message.getMessageProperties().getMessageId();
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String mqMsgIdKey = RABBIT_MQ_MESSAGE_ID_PREFIX + messageId;
        boolean requeue = ++retryCount <= TOTAL_RETRY_COUNT;
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (requeue) {
            channel.basicNack(deliveryTag, false, false);
            valueOperations.set(mqMsgIdKey, retryCount);
            log.info("messageId {} requeue: deliveryTag {}  retryCount {}",messageId, deliveryTag, retryCount);
        } else {
            //ack 掉消息，把该消息插入数据库，批处理
            if (redisTemplate.expire(mqMsgIdKey, EXPIRE_TIME, TimeUnit.SECONDS)) {
                channel.basicAck(deliveryTag, false);
            }

            String msgContent = new String(message.getBody());

//            HashMap<String, String> map = objectMapper.readValue(msgContent, new TypeReference<HashMap<String, String>>() {});
//            String id = "";
//            for (Map.Entry<String, String> entry : map.entrySet()) {
//                if (entry.getKey().equalsIgnoreCase("id")) {
//                    id = entry.getValue();
//                    break;
//                }
//            }

//            // 尝试解析为JSON
//            if (isValidJson(msgContent)) {
//                HashMap<String, String> map =objectMapper.readValue(msgContent,
//                        new TypeReference<HashMap<String, String>>() {});
//            } else {
//                // 如果是纯字符串，创建包含该字符串的Map
//                Map<String, String> map = new HashMap<>();
//                map.put("content", msgContent); // 使用固定key
//                // 或者 map.put("value", msgContent);
//                // 或者 map.put("message", msgContent);
//
//            }


            MessageProperties messageProperties = message.getMessageProperties();
            String msgId = messageProperties.getMessageId();

            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            String exchange = message.getMessageProperties().getReceivedExchange();
            String queueName = message.getMessageProperties().getConsumerQueue();
            //不重试，设置过期删除key
            redisTemplate.expire(mqMsgIdKey, EXPIRE_TIME, TimeUnit.SECONDS);
//            //错误日志入库
//            MqFailLog mqFailLog = new MqFailLog();
//            mqFailLog.setMsgContentId(id);
//            mqFailLog.setExchange(exchange);
//            mqFailLog.setQueueName(queueName);
//            mqFailLog.setRoutingKey(routingKey);
//            mqFailLog.setMsgId(id);
//            mqFailLog.setMessage(msgContent);
//            mqFailLog.setCause(exceptionMsg);
//            mqFailLogService.save(mqFailLog);

            //region update mqMessage
            //重试仍然没有成功，标记为消费失败。走定时任务补偿
            mqMessageService.updateByMsgId(msgId, MqMessageStatus.CONSUME_FAIL.getValue());
            //endregion
        }

    }

    // JSON验证方法
    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
