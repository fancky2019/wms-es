package gs.com.gses.rabbitMQ.monitor;

import com.rabbitmq.client.AMQP;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RabbitMqMonitorServiceImpl implements RabbitMqMonitorService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private AmqpAdmin amqpAdmin;


    @Autowired
    private RabbitMQManagementClientApiService rabbitMQManagementClientApiService;

    /**
     * 使用Feign客户端通过HTTP API获取队列的详细信息。
     *
     * @param vhost 虚拟主机，例如 "/"
     * @param queueName 队列名称
     * @return QueueStats 队列统计信息对象
     */
    @Override
    public QueueStats getQueueStatsByHttpApi(String vhost, String queueName) {
        try {
//            if ("/".equals(vhost)) {
//                vhost = "%2F";
//            }


            //      URI uri = URI.create(String.format("%s/api/queues/%s/%s", rabbitmqApiUrl, encodedVhost, queueName));

            //在浏览器中访问：http://localhost:15672/api/queues/%2F/DirectExchangeQueueSpringBootES尅获取更多信息
            QueueStats stats = rabbitMQManagementClientApiService.getQueueStats(queueName, rabbitMQConfig.getToken());

            log.info("队列 '{}' 状态 - 总消息数: {}, Ready: {}, Unacked: {}, 消费者: {}", queueName, stats.getMessages(), stats.getMessagesReady(), stats.getMessagesUnacknowledged(), stats.getConsumers());
            return stats;
        } catch (Exception e) {
            log.error("通过HTTP API获取队列 {} 状态失败", queueName, e);
            throw new RuntimeException("HTTP API调用失败: " + e.getMessage(), e);
        }
    }


    /**
     * 获取队列消息总数（Ready + Unacked）
     */
    public int getQueueMessageCount(String queueName) {
        try {
            return rabbitTemplate.execute(channel -> {
                AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queueName);
                return declareOk.getMessageCount();
            });
        } catch (Exception e) {
            log.error("获取队列 {} 消息数失败", queueName, e);
            throw new RuntimeException("获取队列消息数失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取详细的队列统计信息
     */
    @Override
    public QueueStats getQueueStatsRabbitTemplate(String queueName) {
        return rabbitTemplate.execute(channel -> {
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queueName);

            QueueStats queueStats = new QueueStats();
            queueStats.setMessages(declareOk.getMessageCount());
            queueStats.setName(declareOk.getQueue());
            queueStats.setConsumers(declareOk.getConsumerCount());
            return queueStats;
        });
    }


    //    @Scheduled(fixedRate = 5000)
    public void checkQueueAndControl() {
        int messageCount = getQueueMessageCount("order.queue");
        log.info("订单队列当前消息数: {}", messageCount);

        if (messageCount > 1000) {
            triggerFlowControl();
        }
    }

    private void triggerFlowControl() {
        // 触发流量控制逻辑
        log.warn("订单队列消息堆积，触发流量控制");
    }


//    /**
//     * java 中没有静态类，只有静态内部类，可以 new        return new QueueStats(
//                    declareOk.getMessageCount(),
//                    declareOk.getMessageCount(), // Total
//                    0  // 注意：AMQP被动声明无法获取ready/unacked细分
//            );
//     */
//    @Data
//    @AllArgsConstructor
//    public static class QueueStats {
//        private int totalMessages;
//        private int messageCount;
//        private int unacknowledgedCount;
//    }


    @Override
    public void deleteQueueAndExchange() {
//        // 删除队列
//        boolean queueDeleted = amqpAdmin.deleteQueue("myQueue");
//        System.out.println("Queue deleted: " + queueDeleted);
//
//        // 删除交换机
//        boolean exchangeDeleted = amqpAdmin.deleteExchange("myExchange");
//        System.out.println("Exchange deleted: " + exchangeDeleted);

        //使用中的队列交换机删除不掉
        List<String> queues = getQueues();
        List<String> exchanges = getExchanges();

        for (String name : queues) {
            boolean deleted = amqpAdmin.deleteQueue(name);
            int m=0;
        }
        for (String name : exchanges) {
            //系统自带的删不掉
            boolean deleted = amqpAdmin.deleteExchange(name);
            int m=0;
        }
        int m = 0;
    }

    public List<String> getQueues() {
        List<Map<String, Object>> queues = rabbitMQManagementClientApiService.getQueues(rabbitMQConfig.getToken());
        List<String> queueNameList = queues.stream().map(q -> (String) q.get("name"))
                .collect(Collectors.toList());
        return queueNameList;
    }

    public List<String> getExchanges() {
        List<Map<String, Object>> exchanges = rabbitMQManagementClientApiService.getExchanges(rabbitMQConfig.getToken());

        List<String> exchangeNames = exchanges.stream()
                .map(ex -> (String) ex.get("name"))
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toList());
        return exchangeNames;
    }


}