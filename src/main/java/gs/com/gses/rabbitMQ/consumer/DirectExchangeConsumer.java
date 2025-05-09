package gs.com.gses.rabbitMQ.consumer;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import gs.com.gses.rabbitMQ.BaseRabbitMqHandler;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
//@RabbitListener(queues = "DirectExchangeQueueSpringBoot")//参数为队列名称
public class DirectExchangeConsumer extends BaseRabbitMqHandler {
    @Autowired
    private ObjectMapper objectMapper;
    private static Logger logger = LogManager.getLogger(DirectExchangeConsumer.class);



    //发送什么类型就用什么类型接收
    //sac:x-single-active-consumer=true (默认false)多个服务监听一个队列，只会有一个服务接受到消息，
    //只有当这个服务挂了，其他服务才能接受到消息，可用于主备模式
    //rabbitmq 默认轮训向所有服务中的一个发送消息
    //多个方法绑定同一个队列MQ会轮训发送给各个方法消费
    //string 接收
    @RabbitHandler
    @RabbitListener(queues = RabbitMQConfig.DIRECT_QUEUE_NAME)//参数为队列名称
    public void receivedMsg(Message message, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                            @Header(AmqpHeaders.CONSUMER_QUEUE) String queueName) throws Exception {
        try {

            String msgContent = new String(message.getBody());

            int m = 0;

        } catch (Exception e) {


        }

    }


}
