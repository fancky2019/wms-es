package gs.com.gses.service;

import gs.com.gses.model.entity.ApplyShipOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.entity.Material;
import gs.com.gses.model.entity.erp.ErpWorkOrderInfoView;
import gs.com.gses.model.request.wms.ApplyShipOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.erp.ErpWorkOrderInfoViewResponse;
import gs.com.gses.model.response.wms.ApplyShipOrderItemResponse;
import gs.com.gses.model.response.wms.ApplyShipOrderResponse;
import org.elasticsearch.client.license.LicensesStatus;

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
