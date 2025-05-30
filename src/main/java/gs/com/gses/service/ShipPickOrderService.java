package gs.com.gses.service;

import gs.com.gses.model.entity.ShipPickOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.ShipPickOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ShipPickOrderResponse;

/**
 * @author lirui
 * @description 针对表【ShipPickOrder】的数据库操作Service
 * @createDate 2024-08-11 10:23:06
 */
public interface ShipPickOrderService extends IService<ShipPickOrder> {
    PageData<ShipPickOrderResponse> getShipPickOrderPage(ShipPickOrderRequest request);
}
