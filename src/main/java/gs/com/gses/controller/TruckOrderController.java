package gs.com.gses.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.aspect.DuplicateSubmission;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @Tag：接口分组（如 "用户管理"、"订单管理"）。
 *
 * @Operation：接口描述（summary、description）。
 *
 * @Schema：字段说明（description、example）。
 *  @group 发车单
 */
@RestController
@Tag(name = "发车单", description = "发车单管理")
@RequestMapping("/api/truckOrder")
public class TruckOrderController {

    @Autowired
    private TruckOrderService truckOrderService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * addTruckOrder
     * @param request
     * @param token
     * @return
     * @throws Throwable
     */
    @DuplicateSubmission
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

    /**
     * addTruckOrderOnly
     * @param request
     * @param token
     * @return
     * @throws Throwable
     */
    @DuplicateSubmission
    @PostMapping("/addTruckOrderOnly")
    public MessageResult<Boolean> addTruckOrderOnly(@RequestBody AddTruckOrderRequest request, @RequestHeader("Authorization") String token) throws Throwable {
        truckOrderService.addTruckOrderAndItemOnly(request, token);
        return MessageResult.success();
    }

    /**
     * updateTruckOrder
     * @param request
     * @return
     * @throws Exception
     */
    @DuplicateSubmission
    @PostMapping("/updateTruckOrder")
    public MessageResult<Void> updateTruckOrder(@RequestBody TruckOrderRequest request) throws Exception {
        truckOrderService.updateTruckOrder(request);
        return MessageResult.success();
    }


    /**
     * getTruckOrderPage
     * @param request
     * @param token
     * @return
     * @throws Exception
     */
//    @DuplicateSubmission(timeOut = 30)
    @Operation(summary = "TruckOrder 分页查询", description = "获取分页列表的详细说明")
    @PostMapping("/getTruckOrderPage")
    public MessageResult<PageData<TruckOrderResponse>> getTruckOrderPage(@RequestBody TruckOrderRequest request, @RequestHeader("Authorization") String token) throws Exception {
        LoginUserTokenDto userTokenDto = UserInfoHolder.getUser();
        PageData<TruckOrderResponse> page = truckOrderService.getTruckOrderPage(request);
        return MessageResult.success(page);
    }

    /**
     * trunkOrderMq
     * @param id
     * @return
     * @throws Exception
     */
    @GetMapping("/trunkOrderMq/{id}")
    public MessageResult<Void> trunkOrderMq(@PathVariable("id") Integer id) throws Exception {
        truckOrderService.trunkOrderMq(id);
        return MessageResult.success();
    }

    /**
     * exportTrunkOrderExcel
     * @param id
     * @param httpServletResponse
     * @throws Exception
     */
    @GetMapping(value = "/exportTrunkOrderExcel/{id}")
    public void exportTrunkOrderExcel(@PathVariable("id") Long id,HttpServletResponse httpServletResponse) throws Exception {
        this.truckOrderService.exportTrunkOrderExcel(id,httpServletResponse);
    }

}
