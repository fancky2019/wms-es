package gs.com.gses.service;

import gs.com.gses.model.entity.TruckOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;

/**
* @author lirui
* @description 针对表【TruckOrderItem】的数据库操作Service
* @createDate 2025-05-28 13:18:54
*/
public interface TruckOrderItemService extends IService<TruckOrderItem> {
    Boolean  checkAvailable(TruckOrderItemRequest request) throws Exception;
}
