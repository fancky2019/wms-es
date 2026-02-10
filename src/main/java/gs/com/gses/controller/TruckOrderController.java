package gs.com.gses.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.aspect.DuplicateSubmission;
import gs.com.gses.filter.UserInfoHolder;
import gs.com.gses.model.bo.ModifyMStr12Bo;
import gs.com.gses.model.enums.EnumClass;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.request.wms.*;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import gs.com.gses.service.TruckOrderService;
import gs.com.gses.sse.ISseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @Tag：接口分组（如 "用户管理"、"订单管理"）。
 *
 * @Operation：接口描述（summary、description）。
 *
 * @Schema：字段说明（description、example）。
 *  @group 发车单
 */
@Slf4j
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

//    /**
//     * updateTruckOrder
//     * @param request
//     * @return
//     * @throws Exception
//     */
//    @DuplicateSubmission
//    @PostMapping("/updateTruckOrder")
//    public MessageResult<Void> updateTruckOrder(@RequestBody TruckOrderRequest request) throws Exception {
//        truckOrderService.updateTruckOrder(request);
//        return MessageResult.success();
//    }


    @DuplicateSubmission
    @PostMapping("/updateTruckOrder")
    public MessageResult<Void> updateTruckOrder(@RequestPart(value = "files", required = false) MultipartFile[] files, @RequestPart("request") TruckOrderRequest request) throws Exception {
        truckOrderService.updateTruckOrder(files, request);
        return MessageResult.success();
    }

    @PostMapping("/expungeStaleAttachment/{id}")
    public MessageResult<Void> expungeStaleAttachment(@PathVariable("id") Integer id) throws Exception {

        // 1. 检查代理类型
        boolean isAopProxy = AopUtils.isAopProxy(truckOrderService);
        boolean isCglibProxy = AopUtils.isCglibProxy(truckOrderService);
        boolean isJdkProxy = AopUtils.isJdkDynamicProxy(truckOrderService);
        log.info("class = {}", truckOrderService.getClass());
        log.info("isJdkProxy = {}", AopUtils.isJdkDynamicProxy(truckOrderService));
        log.info("isCglibProxy = {}", AopUtils.isCglibProxy(truckOrderService));

        truckOrderService.expungeStaleAttachment(id);
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
    public void exportTrunkOrderExcel(@PathVariable("id") Long id, HttpServletResponse httpServletResponse) throws Exception {
        this.truckOrderService.exportTrunkOrderExcel(id, httpServletResponse);
    }

    @GetMapping(value = "/printTest")
    public MessageResult<Void> printTest() throws JsonProcessingException {
        truckOrderService.printTest();
        return MessageResult.success();
    }


    @GetMapping(value = "/getStatusEnum")
    public MessageResult<Map<Integer, String>> getStatusEnum() {
        return MessageResult.success(truckOrderService.getStatusEnum());
    }

    @GetMapping(value = "/getTruckOrderStausEnumClass")
    public MessageResult<List<EnumClass>> getTruckOrderStausEnumClass() {
        return MessageResult.success(truckOrderService.getTruckOrderStausEnumClass());
    }

    //region sse (sever-sent event)
    @Autowired
    private ISseEmitterService sseEmitterService;

    /**
     * 前段页面 user /index.html
     *http://127.0.0.1:8081/sbp/utility/sseConnect/1
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/sseConnect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseConnect() throws Exception {
        //SseEmitter
        LoginUserTokenDto userTokenDto = UserInfoHolder.getUser();
        //此处返回SseEmitter类型，全局异常返回MessageResult，类型冲突
//        Assert.notNull(userTokenDto, "Not logged in");
        if (userTokenDto == null) {
            log.info("sseConnectFail:Not logged in");
            return null;
        }
        log.info("userTokenDto {}", userTokenDto);
        SseEmitter sseEmitter = sseEmitterService.createSseConnect(userTokenDto.getId());
//        return MessageResult.success(sseEmitter);
        return sseEmitter;
    }

    @GetMapping(value = "/pushTest")
    public MessageResult<Void> pushTest() {
        LoginUserTokenDto userTokenDto = UserInfoHolder.getUser();
        sseEmitterService.pushTest(userTokenDto.getId());
        return MessageResult.success();
    }

    @GetMapping(value = "/sseSendMsg")
    public MessageResult<Void> sendMsg(String userId) {
        sseEmitterService.sendMsgToClient(userId, "test");
        return MessageResult.success();
    }

    @GetMapping(value = "/close/{userId}")
    public MessageResult<Void> close(@PathVariable("userId") String userId) {
        sseEmitterService.closeSseConnect(userId);
        return MessageResult.success();
    }
    //endregion
}
