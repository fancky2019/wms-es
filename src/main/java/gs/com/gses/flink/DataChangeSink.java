package gs.com.gses.flink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
import gs.com.gses.rabbitMQ.RabbitMqMessage;
import gs.com.gses.rabbitMQ.producer.DirectExchangeProducer;
import gs.com.gses.service.InventoryInfoService;
import gs.com.gses.service.impl.InventoryInfoServiceImpl;
import gs.com.gses.service.impl.MqMessageServiceImpl;
import gs.com.gses.utility.ApplicationContextAwareImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Slf4j
public class DataChangeSink extends RichSinkFunction<DataChangeInfo> {

//    //使用 transient 关键字标记它，这样序列化时会跳过它
//    @Autowired
//    private transient ObjectMapper objectMapper;
//
//    @Autowired
//    private transient ApplicationContext applicationContext;
//


    @Override
    public void invoke(DataChangeInfo dataChangeInfo, Context context) throws JsonProcessingException, InterruptedException {
        MDC.put("traceId", dataChangeInfo.getTraceId());
        ApplicationContext applicationContext = ApplicationContextAwareImpl.getApplicationContext();
        DirectExchangeProducer directExchangeProducer = applicationContext.getBean(DirectExchangeProducer.class);
        ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
        try {
            String jsonStr = objectMapper.writeValueAsString(dataChangeInfo);
            MessageProperties messageProperties=new MessageProperties();
            messageProperties.setHeader("changeTime", dataChangeInfo.getChangeTime());
            RabbitMqMessage mqMessage = new RabbitMqMessage();
            String msgId = UUID.randomUUID().toString().replaceAll("-", "");
            mqMessage.setMsgId(msgId);
            mqMessage.setBusinessId(dataChangeInfo.getId());
            mqMessage.setBusinessKey(dataChangeInfo.getTableName());
            mqMessage.setMsgContent(jsonStr);
            mqMessage.setExchange(RabbitMQConfig.DIRECT_EXCHANGE);
            mqMessage.setRouteKey(RabbitMQConfig.DIRECT_ROUTING_KEY);
            mqMessage.setQueue(RabbitMQConfig.DIRECT_QUEUE_NAME);
            mqMessage.setTraceId(dataChangeInfo.getTraceId());
            directExchangeProducer.produce(mqMessage);
        } catch (Exception ex) {
            log.error("", ex);
        } finally {
            MDC.remove("traceId");

        }


    }

    public void invoke1(DataChangeInfo dataChangeInfo, Context context) throws JsonProcessingException, InterruptedException {
        MDC.put("traceId", dataChangeInfo.getTraceId());
        ApplicationContext applicationContext = ApplicationContextAwareImpl.getApplicationContext();
//        log.info("收到变更原始数据:{}", dataChangeInfo);
//        Object obj = applicationContext.getBean(dataChangeInfo.getTableName());

        InventoryInfoService inventoryInfoService = applicationContext.getBean(InventoryInfoService.class);

//        switch (dataChangeInfo.getTableName()) {
//            case "Location_copy1":
//                inventoryInfoService.updateByLocation(dataChangeInfo);
//                break;
//            case "Laneway_copy1":
//                inventoryInfoService.updateByLaneway(dataChangeInfo);
//                break;
//            case "Inventory_copy1":
//                inventoryInfoService.updateByInventory(dataChangeInfo);
//                break;
//            case "InventoryItem_copy1":
//                inventoryInfoService.updateByInventoryItem(dataChangeInfo);
//                break;
//            case "InventoryItemDetail_copy1":
//                inventoryInfoService.updateByInventoryItemDetail(dataChangeInfo);
//                break;
//            default:
//                break;
//        }

        try {
            log.info("start sink - {}", dataChangeInfo.getId());
//            Integer.parseInt("m");
            if (StringUtils.isEmpty(dataChangeInfo.getAfterData()) || "READ".equals(dataChangeInfo.getEventType())) {
                log.info("read - {}", dataChangeInfo.getId());
                return;
            }
            switch (dataChangeInfo.getTableName()) {
                case "Location":
                    inventoryInfoService.updateByLocation(dataChangeInfo);
                    break;
                case "Laneway":
                    inventoryInfoService.updateByLaneway(dataChangeInfo);
                    break;
                case "Inventory":
                    inventoryInfoService.updateByInventory(dataChangeInfo);
                    break;
                case "InventoryItem":
                    inventoryInfoService.updateByInventoryItem(dataChangeInfo);
                    break;
                case "InventoryItemDetail":
                    inventoryInfoService.updateByInventoryItemDetail(dataChangeInfo);
                    break;
                default:
                    break;
            }
            log.info("Sink {} completed", dataChangeInfo.getId());
        } catch (Exception ex) {
            log.error("Sink {} exception ,dataChangeInfo.getEventType - {}, BeforeData {},AfterData {}", dataChangeInfo.getId(), dataChangeInfo.getEventType(), dataChangeInfo.getBeforeData(), dataChangeInfo.getAfterData());
            //待优化处理
            log.error("", ex);
//            throw ex;
        } finally {
            MDC.remove("traceId");

        }


    }

    /**
     * 在启动SpringBoot项目是加载了Spring容器，其他地方可以使用@Autowired获取Spring容器中的类；但是Flink启动的项目中，
     * 默认启动了多线程执行相关代码，导致在其他线程无法获取Spring容器，只有在Spring所在的线程才能使用@Autowired，
     * 故在Flink自定义的Sink的open()方法中初始化Spring容器
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
//        this.rabbitTemplate = ApplicationContextUtil.getBean(RabbitTemplate.class);
//        this.confirmService = ApplicationContextUtil.getBean(ConfirmService.class);
//        this.tableDataConvertService = ApplicationContextUtil.getBean(TableDataConvertService.class);
    }
}
