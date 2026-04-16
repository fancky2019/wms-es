package com.gs.gses.service;

import com.gs.gses.model.entity.ShipPickOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.request.wms.ShipPickOrderItemRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.wms.ShipPickOrderItemResponse;

/**
* @author lirui
* @description 针对表【ShipPickOrderItem】的数据库操作Service
* @createDate 2024-08-11 10:23:06
*/
public interface ShipPickOrderItemService extends IService<ShipPickOrderItem> {
    PageData<ShipPickOrderItemResponse> getShipPickOrderItemPage(ShipPickOrderItemRequest request) throws Exception;
}
