package gs.com.gses.controller;

import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.service.TruckOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        truckOrderItemService.checkAvailable(request, null, null);
        return MessageResult.success();
    }

    /**
     * getTruckOrderItemPage
     * @param request
     * @return
     * @throws Exception
     */
//    @DuplicateSubmission(timeOut = 30)
    @PostMapping("/getTruckOrderItemPage")
    public MessageResult<PageData<TruckOrderItemResponse>> getTruckOrderItemPage(@RequestBody TruckOrderItemRequest request) throws Exception {
        PageData<TruckOrderItemResponse> page = truckOrderItemService.getTruckOrderItemPage(request);
        return MessageResult.success(page);
    }

    @GetMapping("/trunkBarCodeMq")
    public MessageResult<Void> trunkBarCodeMq(TruckOrderItemRequest truckOrderItemRequest) throws Exception {
        truckOrderItemService.trunkBarCodeMq(truckOrderItemRequest);
        return MessageResult.success();
    }

    @PostMapping("/mergeTruckOrder")
    public MessageResult<Void> mergeTruckOrder(@RequestBody List<Long> truckOrderIdList) throws Exception {
        truckOrderItemService.mergeTruckOrder(truckOrderIdList);
        return MessageResult.success();
    }

    @PostMapping("/auditFieldTest/{id}")
    public MessageResult<Void> auditFieldTest(@PathVariable("id") Long id) throws Exception {
        truckOrderItemService.auditFieldTest(id);
        return MessageResult.success();
    }

    @GetMapping("/failureReason/{id}")
    public MessageResult<String> failureReason(@PathVariable("id") Long id) throws Exception {
        return MessageResult.success(truckOrderItemService.failureReason(id));
    }
}
