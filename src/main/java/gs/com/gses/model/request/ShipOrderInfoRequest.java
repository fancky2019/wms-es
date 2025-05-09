package gs.com.gses.model.request;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipOrderInfoRequest  extends EsRequestPage{
    private Long applyShipOrderId;
    private String applyShipOrderCode;
    private Long applyShipOrderItemId;
    private BigDecimal applyShipOrderItemRequiredPkgQuantity;
    private BigDecimal applyShipOrderItemAllocatedPkgQuantity;
    private Long id;
    private String shipOrderCode;
    private Long shipOrderItemId;
    private BigDecimal shipOrderItemRequiredPkgQuantity;
    private BigDecimal shipOrderItemAllocatedPkgQuantity;
    private Long shipPickOrderId;
    private Long shipPickOrderItemId;
    private BigDecimal shipPickOrderItemRequiredPkgQuantity;
    private BigDecimal shipPickOrderItemAllocatedPkgQuantity;
    private Long wmsTaskId;
    private String taskNo;
    private Long inventoryId;
    private Long inventoryItemId;
    private Long inventoryItemDetailId;
    private String pallet;
    private BigDecimal movedPkgQuantity;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private String serialNo;
    private Long workOrderId;
    private Long locationId;
    @Field(name = "task_completed_time",index = true, store = true, type = FieldType.Date,format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime taskCompletedTime;
}
