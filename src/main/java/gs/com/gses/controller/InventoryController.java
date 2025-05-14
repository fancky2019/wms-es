package gs.com.gses.controller;

import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.elasticsearch.ShipOrderInfo;
import gs.com.gses.model.entity.Inventory;
import gs.com.gses.model.request.InventoryInfoRequest;
import gs.com.gses.model.request.ShipOrderInfoRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.service.InventoryInfoService;
import gs.com.gses.service.OutBoundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {


    @Autowired
    private InventoryInfoService inventoryInfoService;


    @PostMapping("/initInventoryInfoFromDb")
    public MessageResult<Void> initInventoryInfoFromDb() throws Exception {
        inventoryInfoService.initInventoryInfoFromDb();
        return MessageResult.success();
    }

    @PostMapping("/getInventoryInfoList")
    public MessageResult<PageData<InventoryInfo>> getInventoryInfoList(@RequestBody InventoryInfoRequest request) throws Exception {
        return MessageResult.success(inventoryInfoService.getInventoryInfoDefaultList(request));
    }
}
