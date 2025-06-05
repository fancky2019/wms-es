package gs.com.gses.listener.event;

import org.springframework.cloud.bus.event.RemoteApplicationEvent;


public class EwmsEvent extends RemoteApplicationEvent {
    private String msgTopic;
    private String data;
    // 必须提供无参构造
    public EwmsEvent() {}

    public EwmsEvent(Object source, String originService) {
        super(source, originService);
        this.msgTopic=msgTopic;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    public String getMsgTopic() {
        return msgTopic;
    }

    public void setMsgTopic(String msgTopic) {
        this.msgTopic = msgTopic;
    }

}

