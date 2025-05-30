package gs.com.gses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;

/**
 * @author lirui
 * @description 针对表【InventoryItemDetail】的数据库操作Service
 * @createDate 2024-08-08 13:44:55
 */
public interface InventoryItemDetailService extends IService<InventoryItemDetail> {
    PageData<InventoryItemDetailResponse> getInventoryItemDetailPage(InventoryItemDetailRequest request) throws Exception;

    Boolean checkDetailExist(InventoryItemDetailRequest request) throws Exception;
}



