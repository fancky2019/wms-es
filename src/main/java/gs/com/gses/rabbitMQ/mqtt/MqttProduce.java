package gs.com.gses.rabbitMQ.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class MqttProduce {

    @Value("${spring.mqtt.username}")
    private String username;

    @Value("${spring.mqtt.password}")
    private String password;

    @Value("${spring.mqtt.url}")
    private String hostUrl;

    @Value("${spring.mqtt.client.id}")
    private String clientId;

    @Value("${spring.mqtt.default.topic}")
    private String defaultTopic;

    @Value("${sbp.logMq}")
    private Boolean logMq;


    /**
     * 客户端对象
     */
    private MqttClient client;

    /**
     * 在bean初始化后连接到服务器
     */
    @PostConstruct
    public void init() {
        connect();
    }


    /**
     * 客户端连接服务端
     */
    public void connect() {
        try {
            //创建MQTT客户端对象
            client = new MqttClient(hostUrl, clientId, new MemoryPersistence());
            //连接设置
            MqttConnectOptions options = new MqttConnectOptions();
            //是否清空session，设置false表示服务器会保留客户端的连接记录（订阅主题，qos）,客户端重连之后能获取到服务器在客户端断开连接期间推送的消息
            //设置为true表示每次连接服务器都是以新的身份
            options.setCleanSession(true);
            //设置连接用户名
            options.setUserName(username);
            //设置连接密码
            options.setPassword(password.toCharArray());
            //设置超时时间，单位为秒
            options.setConnectionTimeout(100);
            //设置心跳时间 单位为秒，表示服务器每隔 1.5*20秒的时间向客户端发送心跳判断客户端是否在线
            options.setKeepAliveInterval(20);
            //设置遗嘱消息的话题，若客户端和服务器之间的连接意外断开，服务器将发布客户端的遗嘱信息
            options.setWill("willTopic", (clientId + "与服务器断开连接").getBytes(), 0, false);
            //设置回调
            client.setCallback(new MqttProduceCallBack());
            client.connect(options);


        } catch (MqttException e) {
            log.error("", e);
        }
    }

    //    @Async("threadPoolExecutor")
    public void publish(String topic, String message, String msgId) {
        int qos = 1;
        //retained = true 只会保留最后一条消息
        boolean retained = false;
        publish(qos, retained, topic, message, msgId);
    }

    //    @Async("threadPoolExecutor")
    public void publish(int qos, boolean retained, String topic, String message, String msgId) {
       /*
        rabbitmq 的mqtt性能未做测试
        */

        /*
        QoS 0（最多一次）：消息发布完全依赖底层 TCP/IP 网络。会发生消息丢失或重复。这个级别可用于如下情况，环境传感器数据，丢失一次数据无所谓，因为不久后还会有第二次发送。
        QoS 1（至少一次）：确保消息到达，但消息重复可能会发生。
        QoS 2（只有一次）：确保消息到达一次。这个级别可用于如下情况，在计费系统中，消息重复或丢失会导致不正确的结果。
         */
        //retained = true 只会保留最后一条消息
        if (logMq) {
            log.info("msgId - {} message -{}", msgId, message);
        }
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        mqttMessage.setPayload(message.getBytes(StandardCharsets.UTF_8));
        //主题的目的地，用于发布/订阅信息
        MqttTopic mqttTopic = client.getTopic(topic);
        //提供一种机制来跟踪消息的传递进度
        //用于在以非阻塞方式（在后台运行）执行发布是跟踪消息的传递进度
        MqttDeliveryToken token;
        try {
            //将指定消息发布到主题，但不等待消息传递完成，返回的token可用于跟踪消息的传递状态
            //一旦此方法干净地返回，消息就已被客户端接受发布，当连接可用，将在后台完成消息传递。
            token = mqttTopic.publish(mqttMessage);
            token.waitForCompletion();
            log.info("publish success - {}", msgId);
        } catch (MqttException e) {
            log.info("publish fail - {}", msgId);
            log.error("", e);
        }
    }
}