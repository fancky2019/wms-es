package gs.com.gses.controller;

import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.service.TruckOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/truckOrderItem")
public class TruckOrderItemController {

    @Autowired
    private TruckOrderItemService truckOrderItemService;

    @GetMapping("/checkAvailable")
    public MessageResult<Boolean> checkAvailable(TruckOrderItemRequest request) throws Exception {
//        shipOrderService.allocate();
        truckOrderItemService.checkAvailable(request);
        return MessageResult.success();
    }

}
