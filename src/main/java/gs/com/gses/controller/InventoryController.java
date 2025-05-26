package gs.com.gses.controller;

import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.elasticsearch.ShipOrderInfo;
import gs.com.gses.model.entity.Inventory;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.InventoryInfoRequest;
import gs.com.gses.model.request.ShipOrderInfoRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.service.InventoryInfoService;
import gs.com.gses.service.OutBoundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {


    @Autowired
    private InventoryInfoService inventoryInfoService;


    @GetMapping("/initInventoryInfoFromDb")
    public MessageResult<Void> initInventoryInfoFromDb() throws Exception {
        inventoryInfoService.initInventoryInfoFromDb();
        return MessageResult.success();
    }

    @PostMapping("/getInventoryInfoList")
    public MessageResult<PageData<InventoryInfo>> getInventoryInfoList(@RequestBody InventoryInfoRequest request) throws Exception {
        return MessageResult.success(inventoryInfoService.getInventoryInfoDefaultList(request));


    }

    @PostMapping("/getAllocatedInventoryInfoList")
    public MessageResult<HashMap<Long, List<InventoryInfo>>> getAllocatedInventoryInfoList(@RequestBody InventoryInfoRequest request) throws Exception {
//        return MessageResult.success(inventoryInfoService.getInventoryInfoDefaultList(request));
        return MessageResult.success(inventoryInfoService.getAllocatedInventoryInfoList(request));
    }


    @PostMapping("/test")
    public MessageResult<PageData<Void>> test(@RequestBody InventoryInfoRequest request) throws Exception {
        inventoryInfoService.test();
        return MessageResult.success();
    }


    @GetMapping("/updateByInventoryItemDetailDb/{id}")
    public MessageResult<Void> updateByInventoryItemDetailDb(@PathVariable Long id) {
        inventoryInfoService.updateByInventoryItemDetailDb(id);
        return MessageResult.success();
    }

    @GetMapping("/updateByInventoryItemDb/{id}")
    public MessageResult<Void> updateByInventoryItemDb(@PathVariable Long id) {
        inventoryInfoService.updateByInventoryItemDb(id);
        return MessageResult.success();
    }

    @GetMapping("/updateByInventoryDb/{id}")
    public MessageResult<Void> updateByInventoryDb(@PathVariable Long id) {
        inventoryInfoService.updateByInventoryDb(id);
        return MessageResult.success();
    }
}
