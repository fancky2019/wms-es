package com.gs.gses.service;

import com.gs.gses.model.entity.ReceiptOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.request.wms.ReceiptOrderItemRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.wms.ReceiptOrderItemResponse;

/**
* @author lirui
* @description 针对表【ReceiptOrderItem】的数据库操作Service
* @createDate 2025-07-29 11:07:16
*/
public interface ReceiptOrderItemService extends IService<ReceiptOrderItem> {

    PageData<ReceiptOrderItemResponse> getReceiptOrderItemPage(ReceiptOrderItemRequest request);

}
