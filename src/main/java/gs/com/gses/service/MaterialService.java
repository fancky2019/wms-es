package gs.com.gses.service;

import gs.com.gses.model.entity.Material;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;

/**
* @author lirui
* @description 针对表【Material】的数据库操作Service
* @createDate 2024-08-11 10:16:00
*/
public interface MaterialService extends IService<Material> {
//    PageData<ShipOrderItemResponse> getShipOrderItemPage(ShipOrderItemRequest request)


Material getByCode(String materialCode) throws Exception;

}
