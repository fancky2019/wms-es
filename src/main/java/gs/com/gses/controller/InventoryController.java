package gs.com.gses.controller;

import gs.com.gses.aspect.DuplicateSubmission;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.DemoProductRequest;
import gs.com.gses.model.request.wms.InventoryInfoRequest;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.service.InventoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {


    @Autowired
    private InventoryInfoService inventoryInfoService;


    /**
     *从数据库初始化数据到es
     * @return
     * @throws Exception
     */
    @DuplicateSubmission(timeOut = 5 * 60)
    @GetMapping("/initInventoryInfoFromDb")
    public MessageResult<Void> initInventoryInfoFromDb() throws Exception {
//        Thread.sleep(10000);
        inventoryInfoService.initInventoryInfoFromDb();
        return MessageResult.success();
    }

    /**
     *getInventoryInfoList
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/getInventoryInfoList")
    public MessageResult<PageData<InventoryInfo>> getInventoryInfoList(@RequestBody InventoryInfoRequest request) throws Exception {
        return MessageResult.success(inventoryInfoService.getInventoryInfoDefaultList(request));


    }

    /**
     *getInventoryInfoList
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/getInventoryInfoPage")
    public MessageResult<PageData<InventoryInfo>> getInventoryInfoPage(@RequestBody InventoryInfoRequest request) throws Exception {
        return MessageResult.success(inventoryInfoService.getInventoryInfoPage(request));
    }


    /**
     *@ignore
     * getAllocatedInventoryInfoList
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/getAllocatedInventoryInfoList")
    public MessageResult<HashMap<Long, List<InventoryInfo>>> getAllocatedInventoryInfoList(@RequestBody InventoryInfoRequest request) throws Exception {
//        return MessageResult.success(inventoryInfoService.getInventoryInfoDefaultList(request));
        return MessageResult.success(inventoryInfoService.getAllocatedInventoryInfoList(request));
    }


    /**
     * test
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/test")
    public MessageResult<PageData<Void>> test(@RequestBody InventoryInfoRequest request) throws Exception {
        inventoryInfoService.test();
        return MessageResult.success();
    }

    /**
     * updateByInventoryItemDetailDb
     * @param id
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/updateByInventoryItemDetailDb/{id}")
    public MessageResult<Void> updateByInventoryItemDetailDb(@PathVariable Long id) throws InterruptedException {
        inventoryInfoService.updateByInventoryItemDetailDb(id);
        return MessageResult.success();
    }

    /**
     * updateByInventoryItemDb
     * @param id
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/updateByInventoryItemDb/{id}")
    public MessageResult<Void> updateByInventoryItemDb(@PathVariable Long id) throws InterruptedException {
        inventoryInfoService.updateByInventoryItemDb(id);
        return MessageResult.success();
    }

    /**
     * updateByInventoryDb
     * @param id
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/updateByInventoryDb/{id}")
    public MessageResult<Void> updateByInventoryDb(@PathVariable Long id) throws InterruptedException {
        inventoryInfoService.updateByInventoryDb(id);
        return MessageResult.success();
    }

    /**
     * 下载失败数据
     * @param request
     * @throws Exception
     */
    @GetMapping("/allocatedReason")
    public MessageResult<String> allocatedReason(ShipOrderItemRequest request) throws Exception {
        return MessageResult.success(this.inventoryInfoService.allocatedReason(request));
    }


    /**
     *
     * @param inventoryItemDetailId
     * @throws Exception
     */
    @PostMapping("/addByInventoryItemDetailInfo")
    public MessageResult<Void> addByInventoryItemDetailInfo(Long inventoryItemDetailId) throws Exception {
        inventoryInfoService.updateByInventoryDb(inventoryItemDetailId);
        return MessageResult.success();
    }

    /**
     *easyexcel 分页导出
     * exportByPage 批量导出
     *importExcel 批量导出
     */
    @PostMapping(value = "/exportByPage")
    public void exportByPage(@RequestBody InventoryInfoRequest request, HttpServletResponse httpServletResponse) throws Exception {
        this.inventoryInfoService.exportByPage(httpServletResponse, request);
    }


    @GetMapping("/detailDifference")
    public MessageResult<List<Long>> detailDifference() throws Exception {
        return MessageResult.success(this.inventoryInfoService.detailDifference());
    }
}
