package gs.com.gses.model.response.wms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.util.Date;

public class MqMessageResponse {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String msgId;

    /**
     *
     */
    private String msgContent;

    /**
     *
     */
    private String exchange;

    /**
     *
     */
    private String routeKey;

    /**
     *
     */
    private String queue;

    /**
     *
     */
    private String topic;

    /**
     *
     */
    private String tag;

    /**
     *
     */
    private String remark;

    /**
     * 创建人ID
     */
    private Object creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 最新修改人ID
     */
    private Object lastModifierId;

    /**
     * 最新修改人名称
     */
    private String lastModifierName;

    /**
     * 创建时间戳13位   代码赋值，没有走MetaObjectHandlerImp
     */
    private Long creationTime;

    /**
     * 修改时间戳13位  代码赋值，没有走MetaObjectHandlerImp
     */
    private Long lastModificationTime;

    /**
     *
     */
    private String businessKey;

    /**
     *
     */
    private Integer deleted;

    /**
     *
     */
    private String errorStack;

    /**
     *
     */
    private String failureReason;

    /**
     *
     */
    private Integer maxRetryCount;

    /**
     *
     */
    private Date nextRetryTime;

    /**
     *
     */
    private Integer retryCount;

    /**
     *  0:未生产 1：已生产 2：已消费 3:消费失败
     */
    private Integer status;

    /**
     *
     */
    private Integer version;

    /**
     *
     */
    private Long businessId;

    /**
     *
     */
    private Boolean retry;

    /**
     *
     */
    private String traceId;

    private static final long serialVersionUID = 1L;
}
