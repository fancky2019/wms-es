package com.gs.gses.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gs.gses.model.entity.InventoryItem;
import com.gs.gses.service.InventoryItemService;
import com.gs.gses.mapper.wms.InventoryItemMapper;
import org.springframework.stereotype.Service;

/**
* @author lirui
* @description 针对表【InventoryItem】的数据库操作Service实现
* @createDate 2024-08-11 10:11:10
*/
@Service
public class InventoryItemServiceImpl extends ServiceImpl<InventoryItemMapper, InventoryItem>
    implements InventoryItemService{

}




