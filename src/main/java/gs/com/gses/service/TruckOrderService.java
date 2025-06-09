package gs.com.gses.service;

import gs.com.gses.model.entity.TruckOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.AddTruckOrderRequest;
import gs.com.gses.model.request.wms.TruckOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.mqtt.TrunkOderMq;
import gs.com.gses.model.response.wms.TruckOrderResponse;

/**
 * @author lirui
 * @description 针对表【TruckOrder】的数据库操作Service
 * @createDate 2025-05-28 13:18:54
 */
public interface TruckOrderService extends IService<TruckOrder> {
    void addTruckOrderAndItem(AddTruckOrderRequest request, String token) throws Throwable;

    void addTruckOrderAndItemOnly(AddTruckOrderRequest request, String token) throws Throwable;

    TruckOrder add(TruckOrderRequest truckOrderRequest);

    PageData<TruckOrderResponse> getTruckOrderPage(TruckOrderRequest request);

    void trunkOrderMq(Integer id) throws Exception;
}
