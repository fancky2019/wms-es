package gs.com.gses.controller;

import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.request.InventoryInfoRequest;
import gs.com.gses.model.request.ShipOrderRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.ShipOrderResponse;
import gs.com.gses.service.InventoryInfoService;
import gs.com.gses.service.ShipOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/shipOrder")
public class ShipOrderController {
//    org.apache.flink.util.InstantiationUtil

    //    org.apache.flink.api.common.ExecutionConfig
    @Autowired
    private ShipOrderService shipOrderService;

    @GetMapping("/test")
    public MessageResult<ShipOrder> test(Long id) {
        return MessageResult.success(shipOrderService.test(id));
    }

    @GetMapping("/getShipOrderPage")
    public MessageResult<PageData<ShipOrderResponse>> getShipOrderPage(ShipOrderRequest request) {
        return MessageResult.success(shipOrderService.getShipOrderPage(request));
    }

    @GetMapping("/getShipOrderList")
    public MessageResult<List<ShipOrderResponse>> getShipOrderList(ShipOrderRequest request) {
        return MessageResult.success(shipOrderService.getShipOrderList(request));
    }

    @GetMapping("/allocate")
    public MessageResult<Void> allocate() throws Exception {
        shipOrderService.allocate();
        return MessageResult.success();
    }

    @PostMapping("/allocateDesignatedShipOrders")
    public MessageResult<HashMap<String, String>> allocateDesignatedShipOrders(@RequestBody ShipOrderRequest request) throws Exception {
        return MessageResult.success(shipOrderService.allocateDesignatedShipOrders(request));
    }

}
