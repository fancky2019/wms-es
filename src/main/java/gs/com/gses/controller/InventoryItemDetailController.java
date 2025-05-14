package gs.com.gses.controller;


import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.InventoryItemDetailRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.InventoryInfoService;
import gs.com.gses.service.InventoryItemDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventoryItemDetail")
public class InventoryItemDetailController {

    @Autowired
    private InventoryItemDetailService inventoryItemDetailService;


    @GetMapping("/getInventoryItemDetailPage")
    public MessageResult<List<InventoryItemDetail>> getInventoryItemDetailPage(InventoryItemDetailRequest request) {
        List<InventoryItemDetail> list = inventoryItemDetailService.getInventoryItemDetailPage(request);
        return MessageResult.success(list);
    }

}
