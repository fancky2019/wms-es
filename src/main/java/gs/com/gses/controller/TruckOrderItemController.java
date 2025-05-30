package gs.com.gses.controller;

import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.service.TruckOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/truckOrderItem")
public class TruckOrderItemController {

    @Autowired
    private TruckOrderItemService truckOrderItemService;

    @GetMapping("/checkAvailable")
    public MessageResult<Boolean> checkAvailable(TruckOrderItemRequest request) throws Exception {
        truckOrderItemService.checkAvailable(request);
        return MessageResult.success();
    }

    @PostMapping("/getTruckOrderItemPage")
    public MessageResult<PageData<TruckOrderItemResponse>> getTruckOrderItemPage(TruckOrderItemRequest request) throws Exception {
        PageData<TruckOrderItemResponse> page = truckOrderItemService.getTruckOrderItemPage(request);
        return MessageResult.success(page);
    }

}
