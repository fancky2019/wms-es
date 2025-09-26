package gs.com.gses.service;

import gs.com.gses.model.entity.ShipPickOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.ShipPickOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ShipPickOrderItemResponse;

/**
* @author lirui
* @description 针对表【ShipPickOrderItem】的数据库操作Service
* @createDate 2024-08-11 10:23:06
*/
public interface ShipPickOrderItemService extends IService<ShipPickOrderItem> {
    PageData<ShipPickOrderItemResponse> getShipPickOrderItemPage(ShipPickOrderItemRequest request) throws Exception;
}
