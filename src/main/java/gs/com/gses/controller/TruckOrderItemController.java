package gs.com.gses.controller;

import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/truckOrderItem")
public class TruckOrderItemController {

    @GetMapping("/checkAvailable")
    public MessageResult<Void> checkAvailable(TruckOrderItemRequest request) throws Exception {
//        shipOrderService.allocate();
        return MessageResult.success();
    }

}
