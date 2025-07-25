package gs.com.gses.rabbitMQ.mqtt;


import com.esotericsoftware.minlog.Log;
import gs.com.gses.service.impl.UtilityConst;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Paho 没有暴露 ack 控制的 API，也没有设计延迟 ACK 或手动 ACK 的机制。
 *
 * Spring Integration 的 MqttPahoMessageDrivenChannelAdapter 封装了 Paho，但自己做了消息投递的“延迟确认”处理机制：
 * 拦截 messageArrived()；
 * 将消息封装成 Spring 的 Message 对象；
 * 附加一个 MqttAckCallback；
 * 直到你调用 ack() 才真正从内部确认消息（或标记为失败重试）。
 * 换句话说，Spring Integration 封装了一层“手动 ACK 管理器”。
 *
 */
@Slf4j
@Configuration
public class MqttConsume {
    @Value("${spring.mqtt.username}")
    private String username;

    @Value("${spring.mqtt.password}")
    private String password;

    @Value("${spring.mqtt.url}")
    private String hostUrl;

    @Value("${spring.mqtt.consumerid}")
    private String clientId = "consumer-id";

    @Value("${spring.mqtt.default.topic}")
    private String defaultTopic;

//    @Value("${sbp.topics}")
//    private List<String> topics;

    @Autowired
    private MqttConsumerCallBack mqttConsumerCallBack;
    /**
     * 客户端对象
     */
    private MqttClient client;

    /**
     * 在bean初始化后连接到服务器
     */
    @PostConstruct
    public void init() throws Exception {
//        //创建MQTT客户端对象
//        client = new MqttClient(hostUrl, clientId, new MemoryPersistence());
//        //设置回调
////        client.setCallback(new MqttConsumerCallBack());
//        client.setCallback(mqttConsumerCallBack);
//        connect();
//        subscribe();

        log.info("start init  MqttConsumer");
        try {
            //创建MQTT客户端对象
            client = new MqttClient(hostUrl, clientId, new MemoryPersistence());
            //设置回调
//        client.setCallback(new MqttConsumerCallBack());
            client.setCallback(mqttConsumerCallBack);
            connect();
            subscribe();
            log.info("mqttConsume init complete");
        } catch (Exception e) {
            log.error("mqttConsume init exception", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 客户端连接服务端
     */
    public void connect() throws MqttException {
        //连接设置
        MqttConnectOptions options = new MqttConnectOptions();
        //是否清空session，设置为false表示服务器会保留客户端的连接记录，客户端重连之后能获取到服务器在客户端断开连接期间推送的消息
        //设置为true表示每次连接到服务端都是以新的身份
        options.setCleanSession(false);


        //设置连接用户名
        options.setUserName(username);
//        设置连接密码
        options.setPassword(password.toCharArray());


        //设置超时时间，单位为秒
        options.setConnectionTimeout(100);
        //设置心跳时间 单位为秒，表示服务器每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线
        options.setKeepAliveInterval(20);
        //设置遗嘱消息的话题，若客户端和服务器之间的连接意外断开，服务器将发布客户端的遗嘱信息
        options.setWill("willTopic", (clientId + "与服务器断开连接").getBytes(), 0, false);

        if (!client.isConnected()) {
            client.connect(options);
        }


    }


    public void subscribe() throws MqttException {

        //订阅主题:生产和消费都要指定qos 为1
        //消息等级，和主题数组一一对应，服务端将按照指定等级给订阅了主题的客户端推送消息
        int[] qos = {1, 1, 1, 1};
        String[] topics = {"topic1", "topic2", "topic3", "OutBoundTaskComplete"};


//        int[] qos = {1, 1};
//        String[] topics = {"topic1", UtilityConst.TRUCK_ORDER_COMPLETE_TOPIC};


        //订阅主题
        client.subscribe(topics, qos);
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            log.error("", e);
        }
    }

    /**
     * 订阅主题
     */
    public void subscribe(String topic, int qos) {
        try {
            client.subscribe(topic, qos);
        } catch (MqttException e) {
            log.error("", e);
        }
    }
}
