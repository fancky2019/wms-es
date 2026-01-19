package gs.com.gses.model.response.mqtt;

import gs.com.gses.model.elasticsearch.ShipOrderInfo;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.request.wms.TruckOrderRequest;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import lombok.Data;

import java.util.List;

@Data
public class OrderMaterialMq {
    private String msgId;
    /**
     * 顶部对象：要设置list 不然打印控件报错
     */
    private List<TruckOrderResponse> shipOrderInfo;
    /**
     * 列表
     */
    private List<OrderMaterial> shipOrderItemList;
}
