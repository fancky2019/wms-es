package gs.com.gses.flink;

import com.fasterxml.jackson.core.JsonProcessingException;
import gs.com.gses.service.InventoryInfoService;
import gs.com.gses.service.impl.InventoryInfoServiceImpl;
import gs.com.gses.utility.ApplicationContextAwareImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
//    @Autowired
//    private transient InventoryInfoService inventoryInfoService;


    @Override
    public void invoke(DataChangeInfo value, Context context) throws JsonProcessingException {
        ApplicationContext applicationContext = ApplicationContextAwareImpl.getApplicationContext();
//        log.info("收到变更原始数据:{}", value);
//        Object obj = applicationContext.getBean(value.getTableName());

        InventoryInfoService inventoryInfoService = applicationContext.getBean(InventoryInfoService.class);

        switch (value.getTableName()) {
            case "Location_copy1":
                inventoryInfoService.updateByLocation(value);
                break;
            case "Laneway_copy1":
                inventoryInfoService.updateByLaneway(value);
                break;
            case "Inventory_copy1":
                inventoryInfoService.updateByInventory(value);
                break;
            case "InventoryItem_copy1":
                inventoryInfoService.updateByInventoryItem(value);
                break;
            case "InventoryItemDetail_copy1":
                inventoryInfoService.updateByInventoryItemDetail(value);
                break;
            default:
                break;
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
