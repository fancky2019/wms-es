package com.gs.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gs.gses.model.entity.Inventory;
import com.gs.gses.service.InventoryService;
import com.gs.gses.mapper.wms.InventoryMapper;
import org.springframework.stereotype.Service;

/**
* @author lirui
* @description 针对表【Inventory】的数据库操作Service实现
* @createDate 2024-08-11 10:11:10
*/
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
    implements InventoryService{

}




