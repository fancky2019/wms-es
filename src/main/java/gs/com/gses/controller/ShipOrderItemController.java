package gs.com.gses.controller;

import gs.com.gses.model.bo.wms.OutByAssignedInfoBo;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.service.ShipOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


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

    @PostMapping("/outByAssignedInfo")
    public MessageResult<Void> outByAssignedInfo(@Validated @RequestBody OutByAssignedInfoBo requestList, @RequestHeader("Authorization") String token) throws Exception {
//        Assert.notNull(requestList, "Request parameter cannot be empty");
        shipOrderItemService.outByAssignedInfo(requestList,token);
        return MessageResult.success();
    }


}
