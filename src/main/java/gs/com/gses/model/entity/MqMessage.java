package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName MqMessage
 */
@TableName(value ="MqMessage")
@Data
public class MqMessage implements Serializable {
    /**
     * 
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
     * 创建时间戳13位
     */
    @TableField(value = "CreationTime")
    private Long creationTime;

    /**
     * 修改时间戳13位
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
    private Date nextRetryTime;

    /**
     * 
     */
    @TableField(value = "RetryCount")
    private Integer retryCount;

    /**
     * 
     */
    @TableField(value = "Status")
    private Integer status;

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
    private Boolean retry;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}