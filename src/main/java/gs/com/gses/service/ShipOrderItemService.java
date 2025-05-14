package gs.com.gses.service;

import gs.com.gses.model.entity.ShipOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author lirui
* @description 针对表【ShipOrderItem】的数据库操作Service
* @createDate 2024-08-11 10:23:06
*/
public interface ShipOrderItemService extends IService<ShipOrderItem> {
List<ShipOrderItem> getByShipOrderIds(List<Long> shipOrderIdList);
}
