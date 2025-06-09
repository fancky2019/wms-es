package gs.com.gses.model.response.mqtt;

import lombok.Data;

import java.util.List;

@Data
public class MqttWrapper<T> {
    private Integer count;
    private List<T> data;
}
