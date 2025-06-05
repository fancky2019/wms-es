package gs.com.gses.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.listener.event.EwmsEvent;
import gs.com.gses.listener.event.EwmsEventTopic;
import gs.com.gses.service.TruckOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class EwmsEventListener {

    @Autowired
    private TruckOrderService truckOrderService;

    @Autowired
    private ObjectMapper objectMapper;


    @Async("threadPoolExecutor")
    @TransactionalEventListener
    public void handleMyEvent(EwmsEvent event) {
        try {
            log.info("ThreadId - {}",Thread.currentThread().getId());
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("Received EwmsEvent: {}", eventJson);
            switch (event.getMsgTopic())
            {
                case EwmsEventTopic.TRUCK_ORDER_COMPLETE:
                    truckOrderService.trunkOrderMq(Integer.parseInt(event.getData()));
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            //此处简单设计，失败了落表重试处理。或者重新设计本地消息表

            log.error("Error processing EwmsEvent", ex);
        }
    }
}
