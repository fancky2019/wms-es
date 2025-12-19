package gs.com.gses.rabbitMQ.mqtt;


import gs.com.gses.service.OutBoundOrderService;
import gs.com.gses.utility.ApplicationContextAwareImpl;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttConsumerCallBack implements MqttCallback {

    //    @Autowired
//    private MqttConsume mqttConsume;
//    @Autowired
//    private OutBoundOrderService outBoundOrderService;
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 客户端断开连接的回调
     */
    @Override
    public void connectionLost(Throwable throwable) {

        try {
            log.info("mqttConsumeDisconnected 与服务器断开连接，可重连");
            MqttConsume mqttConsume1 = (MqttConsume) applicationContext.getBean("mqttConsume");
            mqttConsume1.connect();
        } catch (Exception e) {
            log.error("", e);
        }

    }

    /**
     * 消息到达的回调
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        try {


            log.info(String.format("接收消息主题 : %s", topic));
            log.info(String.format("接收消息Qos : %d", message.getQos()));
            String msg = new String(message.getPayload());
            log.info(String.format("接收消息内容 : %s", msg));
            log.info(String.format("接收消息retained : %b", message.isRetained()));

        ApplicationContext applicationContext = ApplicationContextAwareImpl.getApplicationContext();
        OutBoundOrderService  outBoundOrderService =(OutBoundOrderService) applicationContext.getBean("outBoundOrderService");
            switch (topic) {
                case Topics.OUT_BOUND_TASK_COMPLETE:
                    long wmsTaskId = Long.parseLong(msg);
                    outBoundOrderService.taskComplete(wmsTaskId);
                    break;
                case Topics.TEST:
                    test(msg);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {

            log.error("", ex);
            //仍异常 会断开
//           throw  ex;
        }
    }

    private void test(String msg) {
        int m = Integer.parseInt("m");

    }

    /**
     * 消息发布成功的回调
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
