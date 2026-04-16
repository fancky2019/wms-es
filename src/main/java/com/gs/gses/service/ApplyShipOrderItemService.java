package com.gs.gses.service;

import com.gs.gses.model.entity.ApplyShipOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.entity.Material;
import com.gs.gses.model.request.wms.ApplyShipOrderItemRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.erp.ErpWorkOrderInfoViewResponse;
import com.gs.gses.model.response.wms.ApplyShipOrderItemResponse;
import com.gs.gses.model.response.wms.ApplyShipOrderResponse;

import java.util.List;
import java.util.Map;

/**
 * @author lirui
 * @description 针对表【ApplyShipOrderItem】的数据库操作Service
 * @createDate 2024-08-11 10:19:07
 */
public interface ApplyShipOrderItemService extends IService<ApplyShipOrderItem> {
    PageData<ApplyShipOrderItemResponse> getApplyShipOrderItemPage(ApplyShipOrderItemRequest request) throws Exception;

    List<ApplyShipOrderItem> getByApplyMaterialIdBatch(List<ErpWorkOrderInfoViewResponse> erpWorkOrderInfoViewList,
                                                       List<ApplyShipOrderResponse> applyShipOrderResponseList,
                                                       Map<String, Material> materialCodeMap,
                                                       Map<String, List<Long>> workOrderApplyCodeMap);
}
