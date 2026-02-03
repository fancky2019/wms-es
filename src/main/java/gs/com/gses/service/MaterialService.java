package gs.com.gses.service;

import gs.com.gses.model.entity.Material;
import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.request.wms.MaterialRequest;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.MaterialResponse;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【Material】的数据库操作Service
 * @createDate 2024-08-11 10:16:00
 */
public interface MaterialService extends IService<Material> {
//    PageData<ShipOrderItemResponse> getShipOrderItemPage(ShipOrderItemRequest request)

    PageData<MaterialResponse> getMaterialPage(MaterialRequest request);

    Material getByCode(String materialCode) throws Exception;

    List<Material> getByCodeList(List<String> materialCodeList) throws Exception;

    List<MaterialResponse> getByMatchedCode(String materialCode) throws Exception;

    void uploadInspectionTemple(MultipartFile[] files, MaterialRequest materialRequest) throws Exception;

    Material getByCodeCache(String materialCode) throws Exception;


}
