package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.TruckOrderItem;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.service.TruckOrderItemService;
import gs.com.gses.mapper.TruckOrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author lirui
* @description 针对表【TruckOrderItem】的数据库操作Service实现
* @createDate 2025-05-28 13:18:54
*/
@Service
public class TruckOrderItemServiceImpl extends ServiceImpl<TruckOrderItemMapper, TruckOrderItem>
    implements TruckOrderItemService{

  @Autowired
  private ShipOrderItemService shipOrderItemService;

  @Override
  public Boolean checkAvailable(TruckOrderItemRequest request) throws Exception {
    ShipOrderItemRequest shipOrderItemRequest=new ShipOrderItemRequest();
    shipOrderItemRequest.setM_Str7(request.getProjectNo());
    shipOrderItemRequest.setM_Str12(request.getDeviceNo());
    shipOrderItemRequest.setMaterialCode(request.getMaterialCode());
    return shipOrderItemService.checkItemExist(shipOrderItemRequest);
  }
}




