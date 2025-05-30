package gs.com.gses.model.response.mqtt;

import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import lombok.Data;

import java.util.List;

@Data
public class TrunkOderMq {
    private List<TruckOrderResponse> truckOrderResponseList;
    private List<TruckOrderItemResponse> truckOrderItemResponseList;
}
