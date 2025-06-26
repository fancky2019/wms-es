package gs.com.gses.controller;

import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.service.TruckOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/truckOrderItem")
public class TruckOrderItemController {

    @Autowired
    private TruckOrderItemService truckOrderItemService;

    /**
     * checkAvailable
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/checkAvailable")
    public MessageResult<Boolean> checkAvailable(TruckOrderItemRequest request) throws Exception {
        truckOrderItemService.checkAvailable(request,null);
        return MessageResult.success();
    }

    /**
     * getTruckOrderItemPage
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/getTruckOrderItemPage")
    public MessageResult<PageData<TruckOrderItemResponse>> getTruckOrderItemPage(@RequestBody TruckOrderItemRequest request) throws Exception {
        PageData<TruckOrderItemResponse> page = truckOrderItemService.getTruckOrderItemPage(request);
        return MessageResult.success(page);
    }

}
