package gs.com.gses.model.response.wms;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import gs.com.gses.model.enums.TruckOrderStausEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TruckOrderItemResponse implements Serializable {
    /**
     *
     */
    private Long id;

    private Integer seqNo;
    /**
     *
     */
    private Long truckOrderId;
    private String truckOrderCode;
    /**
     *
     */
    private String projectNo;

    /**
     *
     */
    private String projectName;

    /**
     *
     */
    private String applyShipOrderCode;

    /**
     *
     */
    private String shipOrderId;

    /**
     *
     */
    private String shipOrderItemId;

    /**
     *
     */
    private Long materialId;

    /**
     *
     */
    private String materialCode;

    /**
     *
     */
    private String deviceName;
    private BigDecimal quantity;
    /**
     *
     */
    private String deviceNo;

    /**
     *
     */
    private String driverPhone;


    /**
     *
     */
    private String sendBatchNo;

    /**
     *
     */
    private String remark;

    /**
     *
     */
    private Integer deleted;

    /**
     *
     */
    private Integer version;

    /**
     *
     */
    private String creatorId;

    /**
     *
     */
    private String creatorName;

    /**
     *
     */
    private String lastModifierId;

    /**
     *
     */
    private String lastModifierName;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationTime;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModificationTime;
    /**
     *
     */
    private Integer status;

    /**
     *lombok不会生成Getter Setter
     * @data lombox 生成的.class (应idea打开)会先生成get,然后生成set hashcode toString
     *
     * Jackson 默认会序列化所有公共 getter 方法
     *
     * getStatusStr() 是 private 的，所以 Jackson 无法访问
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String statusStr;

    //    @JsonGetter("statusStr")  // 专门用于 Jackson 序列化的注解
    public String getStatusStr() {
        return status == null ? "" : TruckOrderStausEnum.getDescription(status);
    }

    /**
     *
     */
    private String processMsg;

    /**
     *
     */
    private LocalDateTime latestProcessTime;
    private static final long serialVersionUID = 1L;
}
