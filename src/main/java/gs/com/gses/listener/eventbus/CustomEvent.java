package gs.com.gses.listener.eventbus;


import gs.com.gses.model.entity.MqMessage;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

import java.util.List;

/**
 *  添加依赖 spring-cloud-starter-bus-amqp
 *  自定义事件对象
 * RemoteApplicationEvent springCloud bus会落rabbitmq
 */
public class CustomEvent extends RemoteApplicationEvent {
    private List<MqMessage> msgList;
    //public class MyCustomEvent extends ApplicationEvent {
    // 必须提供无参构造
    public CustomEvent() {
        // 反序列化需要默认构造函数
//        super();
    }



    public CustomEvent(Object source, String originService, List<MqMessage> msg) {
        super(source, originService);
        this.msgList=msg;
    }


    //不能加@Data ，否则 必须添加 getter 和 setter（Jackson 序列化需要）.不实现getter 和 setter 无法发送到rabbitmq
    public  List<MqMessage> getMsg() {
        return msgList;
    }

    public void setMsg( List<MqMessage> msg) {
        this.msgList = msg;
    }
}
