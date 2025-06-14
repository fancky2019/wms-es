package gs.com.gses.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.filter.UserInfoHolder;
import gs.com.gses.model.bo.ModifyMStr12Bo;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.request.wms.AddTruckOrderRequest;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.request.wms.TruckOrderRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import gs.com.gses.service.TruckOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/truckOrder")
public class TruckOrderController {

    @Autowired
    private TruckOrderService truckOrderService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/addTruckOrder")
    public MessageResult<Boolean> addTruckOrder(@RequestBody AddTruckOrderRequest request, @RequestHeader("Authorization") String token) throws Throwable {

        //region json
//        AddTruckOrderRequest request1 = new AddTruckOrderRequest();
//
//        TruckOrderRequest truckOrderRequest = new TruckOrderRequest();
//
//        List<TruckOrderItemRequest> truckOrderItemRequestList = new ArrayList<>();
//        TruckOrderItemRequest truckOrderItemRequest = new TruckOrderItemRequest();
//        truckOrderItemRequestList.add(truckOrderItemRequest);
//
//        request1.setTruckOrderRequest(truckOrderRequest);
//        request1.setTruckOrderItemRequestList(truckOrderItemRequestList);
//
//        String json = objectMapper.writeValueAsString(request1);
        //endregion

        truckOrderService.addTruckOrderAndItem(request, token);
        return MessageResult.success();
    }

    @PostMapping("/addTruckOrderOnly")
    public MessageResult<Boolean> addTruckOrderOnly(@RequestBody AddTruckOrderRequest request, @RequestHeader("Authorization") String token) throws Throwable {
        truckOrderService.addTruckOrderAndItemOnly(request, token);
        return MessageResult.success();
    }

    @PostMapping("/updateTruckOrder")
    public MessageResult<Void> updateTruckOrder(@RequestBody TruckOrderRequest request) throws Exception {
        truckOrderService.updateTruckOrder(request);
        return MessageResult.success();
    }

    @PostMapping("/getTruckOrderPage")
    public MessageResult<PageData<TruckOrderResponse>> getTruckOrderPage(@RequestBody TruckOrderRequest request, @RequestHeader("Authorization") String token) throws Exception {
        LoginUserTokenDto userTokenDto = UserInfoHolder.getUser();
        PageData<TruckOrderResponse> page = truckOrderService.getTruckOrderPage(request);
        return MessageResult.success(page);
    }

    @GetMapping("/trunkOrderMq/{id}")
    public MessageResult<Void> trunkOrderMq(@PathVariable("id") Integer id) throws Exception {
        truckOrderService.trunkOrderMq(id);
        return MessageResult.success();
    }

    @GetMapping(value = "/exportTrunkOrderExcel/{id}")
    public void exportTrunkOrderExcel(@PathVariable("id") Long id,HttpServletResponse httpServletResponse) throws Exception {
        this.truckOrderService.exportTrunkOrderExcel(id,httpServletResponse);
    }

}
