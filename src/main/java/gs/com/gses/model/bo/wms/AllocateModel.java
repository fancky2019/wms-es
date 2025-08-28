package gs.com.gses.model.bo.wms;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllocateModel {
    private String pallet;
    private Long shipOrderItemId;
    private Long inventoryId;
    private Long inventoryItemId;
    private Long inventoryItemDetailId;
    private BigDecimal allocateQuantity;
//    private String mStr7;
//    private String mStr12;
//    private Long materialId;

}
