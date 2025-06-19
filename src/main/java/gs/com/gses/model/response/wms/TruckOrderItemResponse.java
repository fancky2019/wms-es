package gs.com.gses.model.response.wms;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

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

    private static final long serialVersionUID = 1L;
}
