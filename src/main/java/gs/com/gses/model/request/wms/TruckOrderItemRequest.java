package gs.com.gses.model.request.wms;

import com.fasterxml.jackson.annotation.JsonFormat;
import gs.com.gses.model.request.EsRequestPage;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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

    private List<Long> truckOrderIdList;
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
    private String shipOrderId;

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
    private String shipOrderItemId;

    /**
     *
     */
    private Long materialId;

    private Long inventoryItemDetailId;
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
    private Boolean ignoreDeviceNo = false;

    /**
     *
     */
    private BigDecimal quantity = BigDecimal.ONE;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于 Spring 接收前端传入的字符串日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 用于返回给前端时格式化 JSON 输出
    private LocalDateTime creationTime;

    /**
     *
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于 Spring 接收前端传入的字符串日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 用于返回给前端时格式化 JSON 输出
    private LocalDateTime lastModificationTime;

    private static final long serialVersionUID = 1L;
}
