package gs.com.gses.rabbitMQ;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RabbitMqMessage {
    private static final long serialVersionUID = 1L;


//    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String msgId;
    private String businessKey;
    private String businessId;
    private String msgContent;
    private String exchange;

    private String routeKey;

    private String queue;

    private Integer retryCount;
    private DateTime nextRetryTime;
    /**
     * 0:未生成 1：已生产 2：已消费 3:消费失败
     */
    private Integer status;
    private Integer maxRetryCount;
    private String failureReason;
    private String errorStack;
    private String remark;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime modifyTime;
    public Integer version;
    public boolean deleted;
    public String traceId;

    public RabbitMqMessage() {

    }

    public RabbitMqMessage(String exchange, String routeKey, String queue, String msgContent) {
        this.msgId = UUID.randomUUID().toString();
        this.msgContent = msgContent;
        this.exchange = exchange;
        this.routeKey = routeKey;
        this.queue = queue;
        this.status=0;
        this.version=0;
        this.remark="";
        this.createTime = LocalDateTime.now();
        this.modifyTime = LocalDateTime.now();
    }
}
