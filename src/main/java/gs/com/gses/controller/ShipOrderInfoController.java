/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gs.com.gses.controller;

import gs.com.gses.aspect.DuplicateSubmission;
import gs.com.gses.model.elasticsearch.DemoProduct;
import gs.com.gses.model.elasticsearch.ShipOrderInfo;
import gs.com.gses.model.entity.WmsTask;
import gs.com.gses.model.request.DemoProductRequest;
import gs.com.gses.model.request.wms.ShipOrderInfoRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
import gs.com.gses.rabbitMQ.RabbitMqMessage;
import gs.com.gses.rabbitMQ.producer.DirectExchangeProducer;
import gs.com.gses.service.OutBoundOrderService;
import gs.com.gses.service.WmsTaskService;
import gs.com.gses.service.elasticsearch.ESDemoProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

/**
 *
 */
@RestController
@RequestMapping("/shipOrderInfo")
public class ShipOrderInfoController {

    @Autowired
    private WmsTaskService wmsTaskService;
    @Autowired
    private ESDemoProductService esDemoProductService;
    @Autowired
    private OutBoundOrderService outBoundOrderService;


    @Autowired
    private DirectExchangeProducer directExchangeProducer;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;


    /**
     * hello
     * @param name
     * @return
     */
    @GetMapping("/hello")
    public String hello(@RequestParam(name = "name", defaultValue = "unknown user") String name) {
        return "Hello " + name;
    }

    /**
     * getWmsTask
     * @return
     */
    @GetMapping("/getWmsTask")
    public MessageResult<WmsTask> getWmsTask() {
        return MessageResult.success();
    }

    /**
     * esTest
     * @param request
     * @return
     */
    @DuplicateSubmission
    @GetMapping("/esTest")
    public MessageResult<PageData<DemoProduct>> esTest(DemoProductRequest request) {

        return MessageResult.success(esDemoProductService.search(request));
    }

    /**
     * taskComplete
     * @param wmsTaskId
     * @return
     * @throws Exception
     */
    @GetMapping("/taskComplete/{wmsTaskId}")
    public MessageResult<Void> taskComplete(@PathVariable("wmsTaskId") Long wmsTaskId) throws Exception {
        outBoundOrderService.taskComplete(wmsTaskId);
        return MessageResult.success();
    }

    /**
     * getShipOrderInfoList
     * @param request
     * @return
     * @throws Exception
     */
    //get 可以在body 内设置参数，但是通常用post 方法
    @GetMapping("/getShipOrderInfoList")
    public MessageResult<PageData<ShipOrderInfo>> getShipOrderInfoList(@RequestBody ShipOrderInfoRequest request) throws Exception {
        return MessageResult.success(outBoundOrderService.search(request));
    }

    /**
     * addBatch
     * @return
     * @throws Exception
     */
    @GetMapping("/addBatch")
    public MessageResult<Void> addBatch() throws Exception {
        outBoundOrderService.addBatch();
        return MessageResult.success();
    }

    /**
     * deleteShipOrderInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/deleteShipOrderInfo")
    public MessageResult<Void> deleteShipOrderInfo() throws Exception {
        outBoundOrderService.deleteShipOrderInfo();
        return MessageResult.success();
    }


    /**
     * aggregationTopBucketQuery
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/aggregationTopBucketQuery")
    public MessageResult<Void> aggregationTopBucketQuery(ShipOrderInfoRequest request) throws Exception {
        outBoundOrderService.aggregationTopBucketQuery(request);
        return MessageResult.success();
    }

    /**
     * scriptQuery
     * @return
     * @throws Exception
     */
    @GetMapping("/scriptQuery")
    public MessageResult<Void> scriptQuery() throws Exception {
        //   shipOrderInfoService.scriptQuery();
        return MessageResult.success();
    }

    /**
     * aggregationStatisticsQuery
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/aggregationStatisticsQuery")
    public MessageResult<LinkedHashMap<String, BigDecimal>> aggregationStatisticsQuery(@RequestBody ShipOrderInfoRequest request) throws Exception {
        LinkedHashMap<String, BigDecimal> map = outBoundOrderService.aggregationStatisticsQuery(request);
        return MessageResult.success(map);
    }

    /**
     * @ignore
     * dateHistogramStatisticsQuery
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/dateHistogramStatisticsQuery")
    public MessageResult<LinkedHashMap<Object, Double>> dateHistogramStatisticsQuery(ShipOrderInfoRequest request) throws Exception {
        LinkedHashMap<Object, Double> map = outBoundOrderService.dateHistogramStatisticsQuery(request);
        return MessageResult.success(map);
    }


    /**
     * rabbitMqTest
     * @return
     * @throws Exception
     */
    @PostMapping("/rabbitMqTest")
    public MessageResult<Void> rabbitMqTest() throws Exception {
        RabbitMqMessage mqMessage =new RabbitMqMessage();
        mqMessage.setMsgId("1111111");
        mqMessage.setMsgContent("124");
        mqMessage.setExchange(RabbitMQConfig.DIRECT_EXCHANGE);
        mqMessage.setQueue(RabbitMQConfig.DIRECT_QUEUE_NAME);
        mqMessage.setRouteKey(RabbitMQConfig.DIRECT_ROUTING_KEY);
        mqMessage.setRetry(false);
        directExchangeProducer.produce(mqMessage);
        return MessageResult.success();
    }

}
