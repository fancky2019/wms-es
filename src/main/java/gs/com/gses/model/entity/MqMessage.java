package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @TableName MqMessage
 */
@TableName(value = "MqMessage")
@Data
public class MqMessage implements Serializable {
    /**
     *NONE	跟随全局配置	未设置策略时，默认跟随全局配置。全局配置默认值通常为 ASSIGN_ID（雪花算法）。
     */
    @TableId(value = "Id")
    private Long id;

    /**
     *
     */
    @TableField(value = "MsgId")
    private String msgId;

    /**
     *
     */
    @TableField(value = "MsgContent")
    private String msgContent;

    /**
     *
     */
    @TableField(value = "Exchange")
    private String exchange;

    /**
     *
     */
    @TableField(value = "RouteKey")
    private String routeKey;

    /**
     *
     */
    @TableField(value = "Queue")
    private String queue;

    /**
     *
     */
    @TableField(value = "Topic")
    private String topic;

    /**
     *
     */
    @TableField(value = "Tag")
    private String tag;

    /**
     *
     */
    @TableField(value = "Remark")
    private String remark;

    /**
     * 创建人ID
     */
    @TableField(value = "CreatorId")
    private Object creatorId;

    /**
     * 创建人名称
     */
    @TableField(value = "CreatorName")
    private String creatorName;

    /**
     * 最新修改人ID
     */
    @TableField(value = "LastModifierId")
    private Object lastModifierId;

    /**
     * 最新修改人名称
     */
    @TableField(value = "LastModifierName")
    private String lastModifierName;

    /**
     * 创建时间戳13位   代码赋值，没有走MetaObjectHandlerImp
     */
    @TableField(value = "CreationTime")
    private Long creationTime;

    /**
     * 修改时间戳13位  代码赋值，没有走MetaObjectHandlerImp
     */
    @TableField(value = "LastModificationTime")
    private Long lastModificationTime;

    /**
     *
     */
    @TableField(value = "BusinessKey")
    private String businessKey;

    /**
     *
     */
    @TableField(value = "Deleted")
    private Integer deleted;

    /**
     *
     */
    @TableField(value = "ErrorStack")
    private String errorStack;

    /**
     *
     */
    @TableField(value = "FailureReason")
    private String failureReason;

    /**
     *
     */
    @TableField(value = "MaxRetryCount")
    private Integer maxRetryCount;

    /**
     *
     */
    @TableField(value = "NextRetryTime")
    private LocalDateTime nextRetryTime;

    /**
     *
     */
    @TableField(value = "RetryCount")
    private Integer retryCount;

    /**
     *  0:未生产 1：已生产 2：已消费 3:消费失败
     */
    @TableField(value = "Status")
    private Integer status;

    @TableField(value = "SendMq")
    private Boolean sendMq;

    /**
     *
     */
    @TableField(value = "Version")
    private Integer version;

    /**
     *
     */
    @TableField(value = "BusinessId")
    private Long businessId;

    /**
     *
     */
    @TableField(value = "Retry")
    private Boolean retry = true;

    /**
     *
     */
    @TableField(value = "TraceId")
    private String traceId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}