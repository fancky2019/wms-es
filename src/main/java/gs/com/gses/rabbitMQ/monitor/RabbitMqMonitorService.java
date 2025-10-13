package gs.com.gses.rabbitMQ.monitor;

public interface RabbitMqMonitorService {
    QueueStats getQueueStatsByHttpApi(String vhost, String queueName) ;
    QueueStats getQueueStatsRabbitTemplate(String queueName);
}
