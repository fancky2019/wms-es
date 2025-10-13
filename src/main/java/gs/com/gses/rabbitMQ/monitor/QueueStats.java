package gs.com.gses.rabbitMQ.monitor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 用于映射RabbitMQ Management API返回的队列统计信息JSON:cite[2]。
 * 使用@JsonIgnoreProperties忽略未映射的字段，提高鲁棒性。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueueStats {

    /**
     * 队列名称
     */
    private String name;

    /**
     * 总消息数（Ready + Unacked）
     */
    private Integer messages;

    /**
     * 处于Ready状态的消息数（准备被消费但还未被消费）
     */
    @JsonProperty("messages_ready")
    private Integer messagesReady;

    /**
     * 处于Unacked状态的消息数（已交付给消费者但未收到确认）
     */
    @JsonProperty("messages_unacknowledged")
    private Integer messagesUnacknowledged;

    /**
     * 当前连接的消费者数量
     */
    private Integer consumers;
}