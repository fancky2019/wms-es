package gs.com.gses.service;

import gs.com.gses.model.entity.ApplyReceiptOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.entity.Material;
import gs.com.gses.model.request.wms.ApplyReceiptOrderItemRequest;
import gs.com.gses.model.request.wms.ApplyReceiptOrderRequest;
import gs.com.gses.model.request.wms.MaterialRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ApplyReceiptOrderResponse;
import gs.com.gses.model.response.wms.MaterialResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lirui
 * @description 针对表【ApplyReceiptOrder】的数据库操作Service
 * @createDate 2025-09-03 16:25:44
 */
public interface ApplyReceiptOrderService extends IService<ApplyReceiptOrder> {
        PageData<ApplyReceiptOrderResponse> getApplyReceiptOrderPage(ApplyReceiptOrderRequest request);
    ApplyReceiptOrder getByCode(String applyReceiptOrderCode) throws Exception;


}
