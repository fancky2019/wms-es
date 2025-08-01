package gs.com.gses.service;

import gs.com.gses.model.entity.ReceiptOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.ReceiptOrderItemRequest;
import gs.com.gses.model.request.wms.ReceiptOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ReceiptOrderItemResponse;
import gs.com.gses.model.response.wms.ReceiptOrderResponse;

/**
* @author lirui
* @description 针对表【ReceiptOrderItem】的数据库操作Service
* @createDate 2025-07-29 11:07:16
*/
public interface ReceiptOrderItemService extends IService<ReceiptOrderItem> {

    PageData<ReceiptOrderItemResponse> getReceiptOrderItemPage(ReceiptOrderItemRequest request);

}
