package gs.com.gses.model.request.wms;

import gs.com.gses.model.request.EsRequestPage;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TruckOrderItemRequest extends EsRequestPage {
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
    private String driverPhone;

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
    private Date creationTime;

    /**
     *
     */
    private Date lastModificationTime;

    private static final long serialVersionUID = 1L;
}
