package com.gs.gses.service;

import com.gs.gses.model.entity.Material;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.request.wms.MaterialRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.wms.MaterialResponse;
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
