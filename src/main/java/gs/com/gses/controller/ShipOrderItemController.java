package gs.com.gses.controller;

import gs.com.gses.model.entity.ShipOrderItem;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.service.TruckOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipOrderItem")
public class ShipOrderItemController {
    @Autowired
    private ShipOrderItemService shipOrderItemService;

    @PostMapping("/getShipOrderItemPage")
    public MessageResult<PageData<ShipOrderItemResponse>> getShipOrderItemPage(@RequestBody ShipOrderItemRequest request) throws Exception {
        PageData<ShipOrderItemResponse> page = shipOrderItemService.getShipOrderItemPage(request);
        return MessageResult.success(page);
    }
}
