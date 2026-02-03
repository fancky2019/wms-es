package gs.com.gses.service;

import gs.com.gses.model.entity.ApplyReceiptOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.ApplyReceiptOrderItemRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lirui
 * @description 针对表【ApplyReceiptOrderItem】的数据库操作Service
 * @createDate 2025-09-03 16:25:44
 */
public interface ApplyReceiptOrderItemService extends IService<ApplyReceiptOrderItem> {
    String inspection(MultipartFile[] files, ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception;

    String inspectionForm(MultipartFile[] files, ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception;

    String inspectionOptimization(MultipartFile[] files, ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception;


    void specificCellWriteExample() throws Exception;
}
