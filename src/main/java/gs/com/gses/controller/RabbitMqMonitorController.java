package gs.com.gses.controller;

import gs.com.gses.model.response.MessageResult;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
import gs.com.gses.rabbitMQ.RabbitMqMessage;
import gs.com.gses.rabbitMQ.monitor.QueueStatus;
import gs.com.gses.rabbitMQ.monitor.RabbitMqMonitorService;
import gs.com.gses.rabbitMQ.producer.DirectExchangeProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbitMqMonitor")
public class RabbitMqMonitorController {


    @Autowired
    private RabbitMqMonitorService rabbitMqMonitorService;

    @Autowired
    private DirectExchangeProducer directExchangeProducer;


//    // 作为查询参数时需要编码
//    @GetMapping("/queues/stats")
//    QueueStats getQueueStats(
//            @RequestParam(value = "vhost", defaultValue = "/") String vhost,
//            @RequestParam("queueName") String queueName
//    ) {
//        // 前端需要编码：encodeURIComponent('/') -> '%2F'
//        // 访问：/queues/stats?vhost=%2F&queueName=my-queue
//    }

//    /**
//     *  / 被丢失 url  解析异常。最终解析成 /rabbitMqMonitor/getQueueStatsByHttpApi/DirectExchangeQueueSpringBootES
//     *  / 转义 %2F
//     *客户端发送：/queues/%2F/my-queue/stats        Spring 自动解码后：vhost = "/"
//     *
//     * {{host}}/rabbitMqMonitor/getQueueStatsByHttpApi/%2F/queueName=DirectExchangeQueueSpringBootES
//      */
//
//    @GetMapping(value = "/getQueueStatsByHttpApi/{vhost}/{queueName}")
//    public MessageResult<QueueStats> getQueueStatsByHttpApi(@PathVariable(value = "vhost", required = false) Optional<String> vhost, @PathVariable("queueName") String queueName) throws Exception {
//        String actualVhost = vhost.orElse("/");  // 默认值为 "/"
//        QueueStats queueStats=  this.rabbitMqMonitorService.getQueueStatsByHttpApi(actualVhost, queueName);
//        return MessageResult.success(queueStats);
//    }


    @GetMapping(value = "/getQueueStatsByHttpApi")
    public MessageResult<QueueStatus> getQueueStatsByHttpApi(
            @RequestParam(value = "vhost", defaultValue = "/") String vhost,
            @RequestParam("queueName") String queueName) throws Exception {
//        String actualVhost = vhost.orElse("/");  // 默认值为 "/"
        QueueStatus queueStatus = this.rabbitMqMonitorService.getQueueStatsByHttpApi(vhost, queueName);
        return MessageResult.success(queueStatus);
    }

    @GetMapping(value = "/getQueueStatsRabbitTemplate/{queueName}")
    public MessageResult<QueueStatus> getQueueStatsRabbitTemplate(@PathVariable("queueName") String queueName) throws Exception {
        QueueStatus queueStatus = this.rabbitMqMonitorService.getQueueStatsRabbitTemplate(queueName);
        return MessageResult.success(queueStatus);
    }


    /**
     * rabbitMqTest
     * @return
     * @throws Exception
     */
    @PostMapping("/rabbitMqTest")
    public MessageResult<Void> rabbitMqTest() throws Exception {

        for (int i = 0; i < 200; i++) {
            RabbitMqMessage mqMessage = new RabbitMqMessage();
            mqMessage.setMsgId("1111111");
            mqMessage.setMsgContent(System.currentTimeMillis() + "");
            mqMessage.setExchange(RabbitMQConfig.DIRECT_EXCHANGE);
            mqMessage.setQueue(RabbitMQConfig.DIRECT_QUEUE_NAME);
            mqMessage.setRouteKey(RabbitMQConfig.DIRECT_ROUTING_KEY);
            mqMessage.setRetry(false);
            directExchangeProducer.produce(mqMessage);
            Thread.sleep(5);
        }
        return MessageResult.success();
    }

    @PostMapping("/deleteQueueAndExchange")
    public MessageResult<Void> deleteQueueAndExchange() {
        this.rabbitMqMonitorService.deleteQueueAndExchange();
        return MessageResult.success();
    }

}
