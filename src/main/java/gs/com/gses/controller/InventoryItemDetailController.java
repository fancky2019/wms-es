package gs.com.gses.controller;


import gs.com.gses.model.bo.ModifyMStr12Bo;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.service.InventoryItemDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/inventoryItemDetail")
public class InventoryItemDetailController {

    @Autowired
    private InventoryItemDetailService inventoryItemDetailService;

    @Autowired
    private HttpServletResponse httpServletResponse;


    /**
     * 分页
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/getInventoryItemDetailPage")
    public MessageResult<PageData<InventoryItemDetailResponse>> getInventoryItemDetailPage(@RequestBody InventoryItemDetailRequest request) throws Exception {
        PageData<InventoryItemDetailResponse> page = inventoryItemDetailService.getInventoryItemDetailPage(request);
        return MessageResult.success(page);
    }

    /**
     * 获取导出模板
     * @throws IOException
     */
    @GetMapping(value = "/exportExcelModifyMStrTemplate")
    public void exportExcelModifyMStrTemplate() throws IOException {
        this.inventoryItemDetailService.exportExcelModifyMStrTemplate(httpServletResponse, ModifyMStr12Bo.class);
    }

    /**
     * 导入
     * @param file
     * @throws IOException
     */
    @PostMapping(value = "/importExcelModifyMStr")
    public void importExcelModifyMStr(MultipartFile file) throws IOException {
        this.inventoryItemDetailService.importExcelModifyMStr12(httpServletResponse, file);
    }

    /**
     *     //downloadLocalExcel
     * 下载失败数据
     * @param response
     * @throws IOException
     */

    @GetMapping("/downloadErrorData")
    public void downloadErrorData(HttpServletResponse response) throws IOException {
        this.inventoryItemDetailService.downloadErrorData(httpServletResponse);
    }


    @PostMapping("/checkDetailExist")
    public MessageResult<Boolean> checkDetailExist(@RequestBody InventoryItemDetailRequest request) throws Exception {
        Boolean re = inventoryItemDetailService.checkDetailExist(request,null,null);
        return MessageResult.success(re);
    }
}
