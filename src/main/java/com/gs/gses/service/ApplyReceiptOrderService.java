package com.gs.gses.service;

import com.gs.gses.model.entity.ApplyReceiptOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.request.wms.ApplyReceiptOrderRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.wms.ApplyReceiptOrderResponse;

/**
 * @author lirui
 * @description 针对表【ApplyReceiptOrder】的数据库操作Service
 * @createDate 2025-09-03 16:25:44
 */
public interface ApplyReceiptOrderService extends IService<ApplyReceiptOrder> {
        PageData<ApplyReceiptOrderResponse> getApplyReceiptOrderPage(ApplyReceiptOrderRequest request);
    ApplyReceiptOrder getByCode(String applyReceiptOrderCode) throws Exception;


}
