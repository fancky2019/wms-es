package gs.com.gses.model.request.wms;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddTruckOrderRequest {
//    private final String msgId = UUID.randomUUID().toString().replaceAll("-", "");
    private TruckOrderRequest truckOrderRequest;
    private List<TruckOrderItemRequest> truckOrderItemRequestList;
}
