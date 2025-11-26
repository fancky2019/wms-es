package gs.com.gses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.bo.wms.AllocateModel;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
                                 List<InventoryItemDetailResponse> detailList, Map<Long, String> palletMap) throws Exception;


    List<Map<String, String>> trunkBarCodePreview(long id) throws Exception;

    List<Long> getAllIdList();
}



