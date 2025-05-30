package gs.com.gses.controller;


import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.service.InventoryItemDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventoryItemDetail")
public class InventoryItemDetailController {

    @Autowired
    private InventoryItemDetailService inventoryItemDetailService;


    @PostMapping("/getInventoryItemDetailPage")
    public MessageResult<PageData<InventoryItemDetailResponse>> getInventoryItemDetailPage(InventoryItemDetailRequest request) throws Exception {
        PageData<InventoryItemDetailResponse> page = inventoryItemDetailService.getInventoryItemDetailPage(request);
        return MessageResult.success(page);
    }

}
