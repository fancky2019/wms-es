package gs.com.gses.model.request.wms;

import lombok.Data;

import java.util.List;

@Data
public class ShipOrderPalletRequest {
    private String shipOrderCode;
    private Long shipOrderItemId;
    private List<InventoryItemDetailRequest> inventoryItemDetailDtoList;
}

