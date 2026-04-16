package com.gs.gses.rabbitMQ.monitor;

public interface RabbitMqMonitorService {
    QueueStatus getQueueStatsByHttpApi(String vhost, String queueName) ;
    QueueStatus getQueueStatsRabbitTemplate(String queueName);

   void deleteQueueAndExchange();
}
