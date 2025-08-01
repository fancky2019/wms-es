package gs.com.gses.service;

import gs.com.gses.model.entity.ReceiptOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.ReceiptOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ReceiptOrderResponse;

/**
 * @author lirui
 * @description 针对表【ReceiptOrder】的数据库操作Service
 * @createDate 2025-07-29 11:07:16
 */
public interface ReceiptOrderService extends IService<ReceiptOrder> {

    PageData<ReceiptOrderResponse> getReceiptOrderPage(ReceiptOrderRequest request);
    void printProcessInBoundCode(ReceiptOrderRequest request) throws Exception;
}
