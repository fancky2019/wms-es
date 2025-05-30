package gs.com.gses.model.request.wms;

import lombok.Data;

import java.util.List;

@Data
public class ShipOrderPalletRequest {
    private String shipOrderCode;
    private List<InventoryItemDetailRequest> inventoryItemDetailDtoList;
}

