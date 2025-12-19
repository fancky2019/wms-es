package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.Warehouse;
import gs.com.gses.service.WarehouseService;
import gs.com.gses.mapper.wms.WarehouseMapper;
import org.springframework.stereotype.Service;

/**
* @author lirui
* @description 针对表【Warehouse】的数据库操作Service实现
* @createDate 2024-08-08 13:26:25
*/
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse>
    implements WarehouseService{

}




