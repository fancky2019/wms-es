package gs.com.gses.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import gs.com.gses.model.bo.wms.AllocateModel;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.entity.TruckOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.enums.MqMessageSourceEnum;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【TruckOrderItem】的数据库操作Service
 * @createDate 2025-05-28 13:18:54
 */
public interface TruckOrderItemService extends IService<TruckOrderItem> {
    Boolean checkAvailable(TruckOrderItemRequest request, List<ShipOrderItemResponse> matchedShipOrderItemResponseList, List<AllocateModel> allocateModelList) throws Exception;

    Boolean checkAvailableBatch(List<TruckOrderItemRequest> requestList, List<ShipOrderItemResponse> matchedShipOrderItemResponseList, List<AllocateModel> allocateModelList) throws Exception;

    Boolean add(TruckOrderItemRequest request);
    void update(TruckOrderItem truckOrderItem) throws Exception;
    List<TruckOrderItem> addBatch(List<TruckOrderItemRequest> requestList);

    void trunkBarCodeMq(TruckOrderItemRequest truckOrderItemRequest) throws Exception;

    PageData<TruckOrderItemResponse> getTruckOrderItemPage(TruckOrderItemRequest request) throws Exception;

    void mergeTruckOrder(List<Long> truckOrderIdList) throws Exception;

    void auditFieldTest(Long id);

    void debit(MqMessage messagae) throws Exception;

}
