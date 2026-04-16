package com.gs.gses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.bo.wms.AllocateModel;
import com.gs.gses.model.entity.InventoryItemDetail;
import com.gs.gses.model.request.wms.InventoryItemDetailRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.wms.InventoryItemDetailResponse;
import com.gs.gses.model.response.wms.ShipOrderItemResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lirui
 * @description 针对表【InventoryItemDetail】的数据库操作Service
 * @createDate 2024-08-08 13:44:55
 */
public interface InventoryItemDetailService extends IService<InventoryItemDetail> {
    PageData<InventoryItemDetailResponse> getInventoryItemDetailPage(InventoryItemDetailRequest request) throws Exception;

    Boolean checkDetailExist(InventoryItemDetailRequest request, List<ShipOrderItemResponse> matchedShipOrderItemResponseList, List<AllocateModel> allocateModelList) throws Exception;

    Boolean checkDetailExistBatch(List<InventoryItemDetailRequest> requestList, List<ShipOrderItemResponse> matchedShipOrderItemResponseList, List<AllocateModel> allocateModelList) throws Exception;

    void importExcelModifyMStr12(HttpServletResponse httpServletResponse, MultipartFile file) throws IOException;

    <T> void exportExcelModifyMStrTemplate(HttpServletResponse httpServletResponse, Class<T> cla) throws IOException;

    void downloadErrorData(HttpServletResponse response) throws IOException;

    List<AllocateModel> allocate(List<ShipOrderItemResponse> shipOrderItemList,
                                 List<InventoryItemDetailResponse> detailList,
                                 Map<Long, String> palletMap,
                                 HashMap<Long, BigDecimal> usedDetailDic,
                                 List<AllocateModel> allocateModelList) throws Exception;


    List<Map<String, String>> trunkBarCodePreview(long id) throws Exception;

    List<Long> getAllIdList();
}



