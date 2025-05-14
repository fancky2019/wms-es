package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.ShipOrderItem;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.mapper.ShipOrderItemMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author lirui
* @description 针对表【ShipOrderItem】的数据库操作Service实现
* @createDate 2024-08-11 10:23:06
*/
@Service
public class ShipOrderItemServiceImpl extends ServiceImpl<ShipOrderItemMapper, ShipOrderItem>
    implements ShipOrderItemService{

    @Override
    public List<ShipOrderItem> getByShipOrderIds(List<Long> shipOrderIdList) {
        LambdaQueryWrapper<ShipOrderItem> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(ShipOrderItem::getShipOrderId,shipOrderIdList);
        List<ShipOrderItem> list=  this.list(queryWrapper);
        return list;
    }
}




