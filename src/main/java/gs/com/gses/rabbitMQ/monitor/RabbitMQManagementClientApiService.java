package gs.com.gses.rabbitMQ.monitor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

//@FeignClient(name = "rabbitmq-management-client", url = "http://localhost:15672", configuration = FeignClientConfig.class)

@FeignClient(name = "RabbitMQManagementClientApiService", url = "http://localhost:15672")
public interface RabbitMQManagementClientApiService {

//    /**
//     * 获取指定虚拟主机和队列的详细信息:cite[2]。
//     *
//     * @param vhost 虚拟主机名，默认通常是"/"，需要URL编码。注意：在路径中传递"/"需要编码为"%2F"
//     * @param queueName 队列名称
//     * @return 包含队列各种状态（如消息数量、消费者数量等）的响应实体
//     */
//    @GetMapping("/api/queues/{vhost}/{queueName}")
//    QueueStats getQueueStats(@PathVariable(value = "vhost") String vhost, @PathVariable("queueName") String queueName, @RequestHeader("Authorization") String token);


    /**
     *
     *
     * openfeign 调用 http://localhost:15672/api/queues/%2F/DirectExchangeQueueSpringBootES  变成
     * http://localhost:15672/api/queues///DirectExchangeQueueSpringBootES
     *
     *
     * //@param vhost 虚拟主机名，默认通常是"/"，需要URL编码。注意：在路径中传递"/"需要编码为"%2F"
     * @param queueName 队列名称
     * @return 包含队列各种状态（如消息数量、消费者数量等）的响应实体
     */
    @GetMapping("/api/queues/%2F/{queueName}")
    QueueStats getQueueStats(@PathVariable("queueName") String queueName, @RequestHeader("Authorization") String token);



}
