package gs.com.gses.model.request.wms;

import com.fasterxml.jackson.annotation.JsonFormat;
import gs.com.gses.model.request.EsRequestPage;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TruckOrderItemRequest extends RequestPage {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private Long truckOrderId;

    /**
     *
     */
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
    private Long shipOrderId;

    /**
     *
     */
    private String shipOrderCode;

    /**
     *
     */
    private String pallet;

    /**
     *
     */
    private Long shipOrderItemId;

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
    private BigDecimal quantity;

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
