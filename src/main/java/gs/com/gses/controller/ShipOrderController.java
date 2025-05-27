package gs.com.gses.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.request.InventoryInfoRequest;
import gs.com.gses.model.request.ShipOrderRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.ShipOrderResponse;
import gs.com.gses.rabbitMQ.mqtt.MqttProduce;
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
    @Autowired
    private ObjectMapper upperObjectMapper;

    @Autowired
    private MqttProduce mqttProduce;

    @GetMapping("/test/{id}")
    public MessageResult<ShipOrder> test(Long id) throws JsonProcessingException {

//        InventoryItemDetail changedInventoryItemDetail = null;
//        String str = null;
//
//        changedInventoryItemDetail = upperObjectMapper.readValue(upperObjectMapper.writeValueAsString(str), InventoryItemDetail.class);

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


    @GetMapping(value = "/mqttTest")
    public void mqttTest(String msg) {
        /*
        QoS 0（最多一次）：消息发布完全依赖底层 TCP/IP 网络。会发生消息丢失或重复。这个级别可用于如下情况，环境传感器数据，丢失一次数据无所谓，因为不久后还会有第二次发送。
        QoS 1（至少一次）：确保消息到达，但消息重复可能会发生。
        QoS 2（只有一次）：确保消息到达一次。这个级别可用于如下情况，在计费系统中，消息重复或丢失会导致不正确的结果。
         */
        int qos = 1;
        //retained = true 只会保留最后一条消息
        boolean retained = false;
        String topic = "topic1";

        mqttProduce.publish(qos, retained, topic, msg);
    }

}
