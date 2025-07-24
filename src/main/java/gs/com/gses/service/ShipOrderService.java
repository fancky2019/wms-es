package gs.com.gses.service;

import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.model.entity.ShipOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.ShipOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.ShipOrderResponse;

import java.util.HashMap;
import java.util.List;

/**
 * @author lirui
 * @description 针对表【ShipOrder】的数据库操作Service
 * @createDate 2024-08-11 10:23:06
 */
public interface ShipOrderService extends IService<ShipOrder> {

    ShipOrder test(Long id);

    PageData<ShipOrderResponse> getShipOrderPage(ShipOrderRequest request);

    List<ShipOrderResponse> getShipOrderList(ShipOrderRequest request);

    void allocate() throws Exception;

    HashMap<String, String> allocateDesignatedShipOrders(ShipOrderRequest request) throws Exception;

    void copyShipOrder(long shipOrderId) throws Exception;

    void sink(DataChangeInfo dataChangeInfo) throws Exception;

    List<Long> nextId(List<Long> idList) throws Exception;
}
