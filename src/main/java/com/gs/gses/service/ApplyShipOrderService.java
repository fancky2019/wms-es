package com.gs.gses.service;

import com.gs.gses.model.entity.ApplyShipOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.request.wms.ApplyShipOrderRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.wms.ApplyShipOrderResponse;

/**
 * @author lirui
 * @description 针对表【ApplyShipOrder】的数据库操作Service
 * @createDate 2024-08-11 10:19:07
 */
public interface ApplyShipOrderService extends IService<ApplyShipOrder> {
    PageData<ApplyShipOrderResponse> getApplyShipOrderPage(ApplyShipOrderRequest request) throws Exception;

}
