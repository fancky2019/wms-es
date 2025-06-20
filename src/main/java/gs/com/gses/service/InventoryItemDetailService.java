package gs.com.gses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author lirui
 * @description 针对表【InventoryItemDetail】的数据库操作Service
 * @createDate 2024-08-08 13:44:55
 */
public interface InventoryItemDetailService extends IService<InventoryItemDetail> {
    PageData<InventoryItemDetailResponse> getInventoryItemDetailPage(InventoryItemDetailRequest request) throws Exception;

    Boolean checkDetailExist(InventoryItemDetailRequest request) throws Exception;

    void importExcelModifyMStr12(HttpServletResponse httpServletResponse, MultipartFile file) throws IOException;

    <T> void exportExcelModifyMStrTemplate(HttpServletResponse httpServletResponse, Class<T> cla) throws IOException;

    void downloadErrorData(HttpServletResponse response) throws IOException;



}



