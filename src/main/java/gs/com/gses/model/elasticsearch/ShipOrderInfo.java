package gs.com.gses.model.elasticsearch;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "ship_order_info")
@Data
public class ShipOrderInfo {
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
    private String materialProperty1;
    private String materialProperty2;
    private String materialProperty3;
    private String materialProperty4;
    private String materialProperty5;
    private String materialProperty6;
    private String materialProperty7;
    private String materialProperty8;
    private String materialProperty9;
    private String materialProperty10;
    private String materialProperty11;
    private String materialProperty12;
    private String materialProperty13;
    private String materialProperty14;
    private String materialProperty15;
    private String materialProperty16;
    private String materialProperty17;
    private String materialProperty18;
    private String materialProperty19;
    private String materialProperty20;
    private String materialProperty21;
    private String materialProperty22;
    private String materialProperty23;
    private String materialProperty24;
    private String materialProperty25;
    private String materialProperty26;
    private String materialProperty27;
    private String materialProperty28;
    private String materialProperty29;
    private String materialProperty30;
    private String materialProperty31;
    private String materialProperty32;
    private String materialProperty33;
    private String materialProperty34;
    private String materialProperty35;
    private String materialProperty36;
    private String materialProperty37;
    private String materialProperty38;
    private String materialProperty39;
    private String materialProperty40;
    private String materialProperty41;
    private String materialProperty42;
    private String materialProperty43;
    private String materialProperty44;
    private String materialProperty45;
    private String materialProperty46;
    private String materialProperty47;
    private String materialProperty48;
    private String materialProperty49;
    private String materialProperty50;

    private String shipOrderItemProperty1;
    private String shipOrderItemProperty2;
    private String shipOrderItemProperty3;
    private String shipOrderItemProperty4;
    private String shipOrderItemProperty5;
    private String shipOrderItemProperty6;
    private String shipOrderItemProperty7;
    private String shipOrderItemProperty8;
    private String shipOrderItemProperty9;
    private String shipOrderItemProperty10;
    private String shipOrderItemProperty11;
    private String shipOrderItemProperty12;
    private String shipOrderItemProperty13;
    private String shipOrderItemProperty14;
    private String shipOrderItemProperty15;
    private String shipOrderItemProperty16;
    private String shipOrderItemProperty17;
    private String shipOrderItemProperty18;
    private String shipOrderItemProperty19;
    private String shipOrderItemProperty20;


    @Field( store = true, type = FieldType.Date,format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime taskCompletedTime;
}
