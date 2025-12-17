package gs.com.gses.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.listener.eventbus.CustomEvent;
import gs.com.gses.filter.UserInfoHolder;
import gs.com.gses.listener.event.EwmsEvent;
import gs.com.gses.listener.event.EwmsEventTopic;
import gs.com.gses.mapper.TruckOrderMapper;
import gs.com.gses.model.bo.wms.AllocateModel;
import gs.com.gses.model.entity.MqMessage;
import gs.com.gses.model.entity.TruckOrder;
import gs.com.gses.model.entity.TruckOrderItem;
import gs.com.gses.model.enums.MqMessageStatus;
import gs.com.gses.model.enums.TruckOrderStausEnum;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.request.wms.*;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.mqtt.PrintWrapper;
import gs.com.gses.model.response.mqtt.TrunkOderMq;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.model.utility.RedisKey;
import gs.com.gses.rabbitMQ.RabbitMQConfig;
import gs.com.gses.rabbitMQ.mqtt.MqttProduce;
import gs.com.gses.service.MqMessageService;
import gs.com.gses.service.ShipPickOrderService;
import gs.com.gses.service.TruckOrderItemService;
import gs.com.gses.service.TruckOrderService;
import gs.com.gses.service.api.WmsService;
import gs.com.gses.sse.ISseEmitterService;
import gs.com.gses.utility.FileUtil;
import gs.com.gses.utility.LambdaFunctionHelper;
import gs.com.gses.utility.PathUtils;
import gs.com.gses.utility.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【TruckOrder】的数据库操作Service实现
 * @createDate 2025-05-28 13:18:54
 */
@Slf4j
@Service
public class TruckOrderServiceImpl extends ServiceImpl<TruckOrderMapper, TruckOrder> implements TruckOrderService {

    @Value("${sbp.upload.directory}")
    private String uploadDirectory;
    @Value("${sbp.upload.wms-front-server}")
    private String wmsFrontServer;


    @Autowired
    private TruckOrderItemService truckOrderItemService;

    @Autowired
    private WmsService wmsService;
    @Autowired
    private ShipPickOrderService shipPickOrderService;
    @Autowired
    private MqttProduce mqttProduce;
    @Autowired
    @Qualifier("upperObjectMapper")
    private ObjectMapper upperObjectMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private BusProperties busProperties;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Executor threadPoolExecutor;
    @Autowired
    private ISseEmitterService sseEmitterService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addTruckOrderAndItem(AddTruckOrderRequest request, String token) throws Throwable {
        if (request.getAsync()) {
            addTruckOrderAndItemAsync(request, token);
            return;
        }

        String currentTaskName = "validateParameter";
        StopWatch stopWatch = new StopWatch("addTruckOrderAndItem");
        stopWatch.start(currentTaskName);
        if (CollectionUtils.isEmpty(request.getTruckOrderItemRequestList())) {
            throw new Exception("装车单明细为空");
        }
        log.info("TruckOrderItemRequestList size {} ", request.getTruckOrderItemRequestList().size());
        for (TruckOrderItemRequest itemRequest : request.getTruckOrderItemRequestList()) {
            if (StringUtils.isNotEmpty(itemRequest.getDeviceNo())) {
                if (itemRequest.getIgnoreDeviceNo()) {
                    String msg = MessageFormat.format("DeviceNo - {0} is not empty,can not ignore DeviceNo ", itemRequest.getDeviceNo());
                    throw new Exception(msg);
                }
            }
            if (StringUtils.isEmpty(itemRequest.getMaterialCode())) {
                throw new Exception("MaterialCode fields contain blank values");
            }
            if (StringUtils.isEmpty(itemRequest.getProjectNo())) {
                throw new Exception("ProjectNo fields contain blank values");
            }
        }

        //多个字段分组
        MultiKeyMap<MultiKey, List<TruckOrderItemRequest>> multiKeyMap = new MultiKeyMap<>();
        for (TruckOrderItemRequest p : request.getTruckOrderItemRequestList()) {
            MultiKey key = new MultiKey<>(p.getProjectNo(), p.getDeviceNo(), p.getMaterialCode());
            List<TruckOrderItemRequest> group = multiKeyMap.get(key);
            if (group == null) {
                group = new ArrayList<>();
                multiKeyMap.put(key, group);
            }
            group.add(p);
        }

        for (MultiKey multiKey : multiKeyMap.keySet()) {
            List<TruckOrderItemRequest> itemList = multiKeyMap.get(multiKey);
            if (itemList.size() > 1) {
                throw new Exception("项目号设备号物料号重复 - " + StringUtils.join(multiKey.getKeys(), ","));
            }
        }
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "checkAvailable";
        stopWatch.start(currentTaskName);
        List<ShipOrderPalletRequest> shipOrderPalletRequestList = new ArrayList<>();
        HashSet<String> shipOrderCodeSet = new HashSet<>();
        List<ShipOrderItemResponse> allMatchedShipOrderItemResponseList = new ArrayList<>();
        List<AllocateModel> allAllocateModelList = new ArrayList<>();

//            for (TruckOrderItemRequest itemRequest : request.getTruckOrderItemRequestList()) {
//                List<ShipOrderItemResponse> matchedShipOrderItemResponseList = new ArrayList<>();
//                List<AllocateModel> allocateModelList = new ArrayList<>();
//                Boolean result = truckOrderItemService.checkAvailable(itemRequest, matchedShipOrderItemResponseList, allocateModelList);
//                if (!result) {
//                    String str = MessageFormat.format("CheckFail : 项目号 - {0} 设备号 - {1} 物料 - {2} 校验失败.", itemRequest.getProjectNo(), itemRequest.getDeviceNo(), itemRequest.getMaterialCode());
//                    throw new Exception(str);
//                }
//                List<String> shipOrderCodeList = matchedShipOrderItemResponseList.stream().map(p -> p.getShipOrderCode()).distinct().collect(Collectors.toList());
////            shipOrderCodeSet.add(itemRequest.getShipOrderCode());
//                shipOrderCodeSet.addAll(shipOrderCodeList);
//                allMatchedShipOrderItemResponseList.addAll(matchedShipOrderItemResponseList);
//                allAllocateModelList.addAll(allocateModelList);
//            }


        //region 多线程校验
        //使用多线程校验
//            当使用 CompletableFuture.runAsync() 时，每个任务会在不同的线程中执行，
//            而 MDC 是基于 ThreadLocal 实现的，不同线程之间无法自动共享 MDC 上下文，导致 traceId 丢失。
        // 在主线程中捕获当前 MDC 上下文  contextMap:  traceId -> ddb27e1d921e6f97  spanId -> ddb27e1d921e6f97
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        List<CompletableFuture<Void>> futures = request.getTruckOrderItemRequestList()
                .parallelStream()
                .map(itemRequest -> CompletableFuture.runAsync(() -> {
                    // 在异步线程中恢复 MDC 上下文
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    log.info("ThreadId:" + Thread.currentThread().getId());

                    List<ShipOrderItemResponse> matchedShipOrderItemResponseList = new ArrayList<>();
                    List<AllocateModel> allocateModelList = new ArrayList<>();

                    try {
                        Boolean result = truckOrderItemService.checkAvailable(itemRequest,
                                matchedShipOrderItemResponseList, allocateModelList);

                        // 统一异常处理逻辑
                        if (!result) {
                            String str = MessageFormat.format("CheckFail : 项目号 - {0} 设备号 - {1} 物料 - {2} 校验失败.",
                                    itemRequest.getProjectNo(), itemRequest.getDeviceNo(), itemRequest.getMaterialCode());
                            throw new RuntimeException(str);
                        }

                        // 处理成功逻辑
                        List<String> shipOrderCodeList = matchedShipOrderItemResponseList.stream()
                                .map(p -> p.getShipOrderCode())
                                .distinct()
                                .collect(Collectors.toList());
                        shipOrderCodeSet.addAll(shipOrderCodeList);

                        synchronized (allMatchedShipOrderItemResponseList) {
                            allMatchedShipOrderItemResponseList.addAll(matchedShipOrderItemResponseList);
                        }
                        synchronized (allAllocateModelList) {
                            allAllocateModelList.addAll(allocateModelList);
                        }

                    } catch (Exception e) {
                        // 统一捕获所有异常（包括RuntimeException）
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                }, threadPoolExecutor))
                .collect(Collectors.toList());

        // 等待所有任务完成并收集异常
        try {
            //第一个异常就会中断主线程。这是 CompletableFuture.allOf().join() 的默认行为。
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            // 这里会捕获到第一个失败的异常
            Throwable realException = e.getCause();
            throw new RuntimeException("批量处理失败: " + realException.getMessage(), realException);
        }

//endregion

        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
//        if (request.getTruckOrderRequest().getSenderPhone().contains("11")) {
//            throw new Exception(stopWatch.getLastTaskTimeMillis() / 1000 + "");
//        }
        currentTaskName = "prepareParameter";
        stopWatch.start(currentTaskName);

        String allMatchedShipOrderItemResponseListJson = objectMapper.writeValueAsString(allMatchedShipOrderItemResponseList);
        log.info("allMatchedShipOrderItemResponseList -:{}", allMatchedShipOrderItemResponseListJson);

        String allAllocateModelListJson = objectMapper.writeValueAsString(allAllocateModelList);
        log.info("allAllocateModelList -:{}", allAllocateModelListJson);
        List<TruckOrderItemRequest> splitTruckOrderItemRequestList = new ArrayList<>();
        TruckOrderItemRequest newTruckOrderItemRequest = null;
        //分组成map
//        Map<Long, List<AllocateModel>> allocateModelMap = allAllocateModelList.stream()
//                .collect(Collectors.groupingBy(AllocateModel::getShipOrderItemId));

        for (TruckOrderItemRequest truckOrderItemRequest : request.getTruckOrderItemRequestList()) {

            List<ShipOrderItemResponse> currentShipOrderItemResponseList = allMatchedShipOrderItemResponseList.stream().filter(p -> p.getM_Str7().equals(truckOrderItemRequest.getProjectNo()) && p.getMaterialId().equals(truckOrderItemRequest.getMaterialId())).collect(Collectors.toList());

            if (StringUtils.isNotEmpty(truckOrderItemRequest.getDeviceNo())) {
                currentShipOrderItemResponseList = currentShipOrderItemResponseList.stream().filter(p -> p.getM_Str12().equals(truckOrderItemRequest.getDeviceNo())).collect(Collectors.toList());
            }

            List<String> currentShipOrderCodeList = currentShipOrderItemResponseList.stream().map(p -> p.getShipOrderCode()).distinct().collect(Collectors.toList());

            for (String shipOrderCode : currentShipOrderCodeList) {

                //当前装车明细对应发货单的明细
                List<ShipOrderItemResponse> orderItemList = currentShipOrderItemResponseList.stream().filter(p -> p.getShipOrderCode().equals(shipOrderCode)).distinct().collect(Collectors.toList());
                //发货单明细的分配
                List<Long> orderItemIdList = orderItemList.stream().map(p -> p.getId()).collect(Collectors.toList());
                List<AllocateModel> shipOrderAllocateModelList = allAllocateModelList.stream().filter(p -> orderItemIdList.contains(p.getShipOrderItemId())).collect(Collectors.toList());

                //根据分配库存拆分装车明细
                for (ShipOrderItemResponse itemResponse : orderItemList) {
                    List<AllocateModel> currentShipOrderItemAllocateModelList = shipOrderAllocateModelList.stream().filter(p -> p.getShipOrderItemId().equals(itemResponse.getId())).collect(Collectors.toList());
                    for (AllocateModel allocateModel : currentShipOrderItemAllocateModelList) {
                        newTruckOrderItemRequest = new TruckOrderItemRequest();
                        BeanUtils.copyProperties(truckOrderItemRequest, newTruckOrderItemRequest);
                        newTruckOrderItemRequest.setProjectName(itemResponse.getM_Str8());
                        newTruckOrderItemRequest.setQuantity(allocateModel.getAllocateQuantity());
                        newTruckOrderItemRequest.setApplyShipOrderCode(itemResponse.getApplyShipOrderCode());
                        newTruckOrderItemRequest.setShipOrderId(itemResponse.getShipOrderId().toString());
                        newTruckOrderItemRequest.setShipOrderCode(itemResponse.getShipOrderCode());
                        newTruckOrderItemRequest.setShipOrderItemId(itemResponse.getId().toString());
                        newTruckOrderItemRequest.setInventoryItemDetailId(allocateModel.getInventoryItemDetailId());
                        newTruckOrderItemRequest.setPallet(allocateModel.getPallet());
                        splitTruckOrderItemRequestList.add(newTruckOrderItemRequest);
                    }
                }
                if (CollectionUtils.isEmpty(splitTruckOrderItemRequestList)) {
                    throw new Exception("AllocateException:splitTruckOrderItemRequestList is empty");
                }

                //分配的托盘
                List<String> itemAllocatedPalletList = shipOrderAllocateModelList.stream().map(AllocateModel::getPallet).distinct().collect(Collectors.toList());
                //根据托盘拆分
                for (String pallet : itemAllocatedPalletList) {
                    BigDecimal currentPalletQuantity = shipOrderAllocateModelList.stream().filter(p -> p.getPallet().equals(pallet)).map(AllocateModel::getAllocateQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    List<ShipOrderPalletRequest> added = shipOrderPalletRequestList.stream().filter(p -> p.getShipOrderCode().equals(shipOrderCode)).collect(Collectors.toList());

                    if (CollectionUtils.isNotEmpty(added)) {
                        ShipOrderPalletRequest shipOrderPalletRequest = added.get(0);
                        List<InventoryItemDetailRequest> inventoryItemDetailRequestList = shipOrderPalletRequest.getInventoryItemDetailDtoList();
                        InventoryItemDetailRequest detailRequest = getInventoryItemDetailRequest(truckOrderItemRequest, currentPalletQuantity, pallet);
                        inventoryItemDetailRequestList.add(detailRequest);
                    } else {
                        ShipOrderPalletRequest shipOrderPalletRequest = new ShipOrderPalletRequest();
                        shipOrderPalletRequest.setShipOrderCode(shipOrderCode);
                        List<InventoryItemDetailRequest> inventoryItemDetailRequestList = new ArrayList<>();
                        InventoryItemDetailRequest detailRequest = getInventoryItemDetailRequest(truckOrderItemRequest, currentPalletQuantity, pallet);
                        inventoryItemDetailRequestList.add(detailRequest);
                        shipOrderPalletRequest.setInventoryItemDetailDtoList(inventoryItemDetailRequestList);
                        shipOrderPalletRequestList.add(shipOrderPalletRequest);
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(shipOrderPalletRequestList)) {
            throw new Exception("AllocateException:shipOrderPalletRequestList is empty");
        }
//        if (true) {
//            return;
//        }
        //校验
        for (ShipOrderPalletRequest shipOrderPalletRequest : shipOrderPalletRequestList) {
            if (StringUtils.isEmpty(shipOrderPalletRequest.getShipOrderCode())) {
                throw new Exception("AllocateException:ShipOrderPalletRequest ShipOrderCode is empty");
            }
            if (CollectionUtils.isEmpty(shipOrderPalletRequest.getInventoryItemDetailDtoList())) {
                throw new Exception("AllocateException:ShipOrderPalletRequest InventoryItemDetailDtoList is empty");

            }
            for (InventoryItemDetailRequest detailRequest : shipOrderPalletRequest.getInventoryItemDetailDtoList()) {
                if (StringUtils.isEmpty(detailRequest.getPallet())) {
                    throw new Exception("AllocateException:InventoryItemDetailRequest Pallet is empty");
                }
                if (StringUtils.isEmpty(detailRequest.getMaterialCode())) {
                    throw new Exception("AllocateException:InventoryItemDetailRequest MaterialCode is empty");
                }
                if (StringUtils.isEmpty(detailRequest.getM_Str7())) {
                    throw new Exception("AllocateException:InventoryItemDetailRequest M_Str7 is empty");
                }

            }
        }

        AddTruckOrderRequest splitRequest = new AddTruckOrderRequest();
        splitRequest.setTruckOrderItemRequestList(null);
        BeanUtils.copyProperties(request, splitRequest);
        splitRequest.setTruckOrderItemRequestList(splitTruckOrderItemRequestList);

//        long createTime = LocalDateTime.now().toInstant(ZoneOffset.of("+08:00")).toEpochMilli();
        long createTime = Instant.now().toEpochMilli();
        splitRequest.getTruckOrderRequest().setCreationTime(LocalDateTime.now());
        String addTruckOrderRequestJson = objectMapper.writeValueAsString(splitRequest);
        log.info("addTruckOrderOnly-addTruckOrderRequestJson -:{}", addTruckOrderRequestJson);
        //未登录会得到全局异常
        String jsonParam = objectMapper.writeValueAsString(shipOrderPalletRequestList);
        log.info("Before request WmsService subAssignPalletsByShipOrderBatch - json:{}", jsonParam);
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "callWmsService";
        stopWatch.start(currentTaskName);

        //        Integer.parseInt("m");
        WmsResponse wmsResponse = wmsService.subAssignPalletsByShipOrderBatch(shipOrderPalletRequestList, token);
        String jsonResponse = objectMapper.writeValueAsString(wmsResponse);
        log.info("After request WmsService subAssignPalletsByShipOrderBatch - json:{}", jsonResponse);
        if (wmsResponse.getResult()) {

            stopWatch.stop();
            log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
            currentTaskName = "saveTruckOrderAndItem";
            stopWatch.start(currentTaskName);

            TruckOrder truckOrder = saveTruckOrderAndItem(splitRequest, createTime);
            stopWatch.stop();
            log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
            currentTaskName = "TruckOrderCompleteMqtt";
            stopWatch.start(currentTaskName);
            try {
                log.info("ThreadId - {}", Thread.currentThread().getId());
                EwmsEvent event = new EwmsEvent(this, busProperties.getId());
                event.setData(truckOrder.getId().toString());
                event.setMsgTopic(EwmsEventTopic.TRUCK_ORDER_COMPLETE);
                eventPublisher.publishEvent(event);
            } catch (Exception ex) {
                log.error("Publish event error", ex);
            }
            stopWatch.stop();
            log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
            log.info("currentTaskName stopWatch {} cost {}", stopWatch.getId(), stopWatch.getTotalTimeMillis());

        } else {
            throw new Exception(" WmsApiException - " + wmsResponse.getExplain());
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addTruckOrderAndItemAsync(AddTruckOrderRequest request, String token) throws Throwable {
        log.info("addTruckOrderAndItemAsync");
        String currentTaskName = "validateParameter";
        StopWatch stopWatch = new StopWatch("addTruckOrderAndItem");
        stopWatch.start(currentTaskName);
        if (CollectionUtils.isEmpty(request.getTruckOrderItemRequestList())) {
            throw new Exception("装车单明细为空");
        }
        log.info("TruckOrderItemRequestList size {} ", request.getTruckOrderItemRequestList().size());
        for (TruckOrderItemRequest itemRequest : request.getTruckOrderItemRequestList()) {
            if (StringUtils.isNotEmpty(itemRequest.getDeviceNo())) {
                if (itemRequest.getIgnoreDeviceNo()) {
                    String msg = MessageFormat.format("DeviceNo - {0} is not empty,can not ignore DeviceNo ", itemRequest.getDeviceNo());
                    throw new Exception(msg);
                }
            }
            if (StringUtils.isEmpty(itemRequest.getMaterialCode())) {
                throw new Exception("MaterialCode fields contain blank values");
            }
            if (StringUtils.isEmpty(itemRequest.getProjectNo())) {
                throw new Exception("ProjectNo fields contain blank values");
            }
        }

        //多个字段分组
        MultiKeyMap<MultiKey, List<TruckOrderItemRequest>> multiKeyMap = new MultiKeyMap<>();
        for (TruckOrderItemRequest p : request.getTruckOrderItemRequestList()) {
            MultiKey key = new MultiKey<>(p.getProjectNo(), p.getDeviceNo(), p.getMaterialCode());
            List<TruckOrderItemRequest> group = multiKeyMap.get(key);
            if (group == null) {
                group = new ArrayList<>();
                multiKeyMap.put(key, group);
            }
            group.add(p);
        }

        for (MultiKey multiKey : multiKeyMap.keySet()) {
            List<TruckOrderItemRequest> itemList = multiKeyMap.get(multiKey);
            if (itemList.size() > 1) {
                throw new Exception("项目号设备号物料号重复 - " + StringUtils.join(multiKey.getKeys(), ","));
            }
        }
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "checkAvailable";
        stopWatch.start(currentTaskName);
        List<ShipOrderPalletRequest> shipOrderPalletRequestList = new ArrayList<>();
        HashSet<String> shipOrderCodeSet = new HashSet<>();
        List<ShipOrderItemResponse> allMatchedShipOrderItemResponseList = new ArrayList<>();
        List<AllocateModel> allAllocateModelList = new ArrayList<>();
        this.truckOrderItemService.checkAvailableBatch(request.getTruckOrderItemRequestList(), allMatchedShipOrderItemResponseList, allAllocateModelList);
        int n = 0;
//        if (true) {
//            return;
//        }

        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "prepareParameter";
        stopWatch.start(currentTaskName);

        String allMatchedShipOrderItemResponseListJson = objectMapper.writeValueAsString(allMatchedShipOrderItemResponseList);
        log.info("allMatchedShipOrderItemResponseList -:{}", allMatchedShipOrderItemResponseListJson);

        String allAllocateModelListJson = objectMapper.writeValueAsString(allAllocateModelList);
        log.info("allAllocateModelList -:{}", allAllocateModelListJson);
        List<TruckOrderItemRequest> splitTruckOrderItemRequestList = new ArrayList<>();
        TruckOrderItemRequest newTruckOrderItemRequest = null;
        //分组成map
//        Map<Long, List<AllocateModel>> allocateModelMap = allAllocateModelList.stream()
//                .collect(Collectors.groupingBy(AllocateModel::getShipOrderItemId));

        for (TruckOrderItemRequest truckOrderItemRequest : request.getTruckOrderItemRequestList()) {

            List<ShipOrderItemResponse> currentShipOrderItemResponseList = allMatchedShipOrderItemResponseList.stream().filter(p -> p.getM_Str7().equals(truckOrderItemRequest.getProjectNo()) && p.getMaterialId().equals(truckOrderItemRequest.getMaterialId())).collect(Collectors.toList());

            if (StringUtils.isNotEmpty(truckOrderItemRequest.getDeviceNo())) {
                currentShipOrderItemResponseList = currentShipOrderItemResponseList.stream().filter(p -> p.getM_Str12().equals(truckOrderItemRequest.getDeviceNo())).collect(Collectors.toList());
            }

            List<String> currentShipOrderCodeList = currentShipOrderItemResponseList.stream().map(p -> p.getShipOrderCode()).distinct().collect(Collectors.toList());

            for (String shipOrderCode : currentShipOrderCodeList) {

                //当前装车明细对应发货单的明细
                List<ShipOrderItemResponse> orderItemList = currentShipOrderItemResponseList.stream().filter(p -> p.getShipOrderCode().equals(shipOrderCode)).distinct().collect(Collectors.toList());
                //发货单明细的分配
                List<Long> orderItemIdList = orderItemList.stream().map(p -> p.getId()).collect(Collectors.toList());
                List<AllocateModel> shipOrderAllocateModelList = allAllocateModelList.stream().filter(p -> orderItemIdList.contains(p.getShipOrderItemId())).collect(Collectors.toList());

                //根据分配库存拆分装车明细
                for (ShipOrderItemResponse itemResponse : orderItemList) {
                    List<AllocateModel> currentShipOrderItemAllocateModelList = shipOrderAllocateModelList.stream().filter(p -> p.getShipOrderItemId().equals(itemResponse.getId())).collect(Collectors.toList());
                    for (AllocateModel allocateModel : currentShipOrderItemAllocateModelList) {
                        newTruckOrderItemRequest = new TruckOrderItemRequest();
                        BeanUtils.copyProperties(truckOrderItemRequest, newTruckOrderItemRequest);
                        newTruckOrderItemRequest.setProjectName(itemResponse.getM_Str8());
                        newTruckOrderItemRequest.setQuantity(allocateModel.getAllocateQuantity());
                        newTruckOrderItemRequest.setApplyShipOrderCode(itemResponse.getApplyShipOrderCode());
                        newTruckOrderItemRequest.setShipOrderId(itemResponse.getShipOrderId().toString());
                        newTruckOrderItemRequest.setShipOrderCode(itemResponse.getShipOrderCode());
                        newTruckOrderItemRequest.setShipOrderItemId(itemResponse.getId().toString());
                        newTruckOrderItemRequest.setInventoryItemDetailId(allocateModel.getInventoryItemDetailId());
                        newTruckOrderItemRequest.setPallet(allocateModel.getPallet());
                        splitTruckOrderItemRequestList.add(newTruckOrderItemRequest);
                    }
                }
                if (CollectionUtils.isEmpty(splitTruckOrderItemRequestList)) {
                    throw new Exception("AllocateException:splitTruckOrderItemRequestList is empty");
                }

                //分配的托盘
                List<String> itemAllocatedPalletList = shipOrderAllocateModelList.stream().map(AllocateModel::getPallet).distinct().collect(Collectors.toList());
                //根据托盘拆分
                for (String pallet : itemAllocatedPalletList) {
                    BigDecimal currentPalletQuantity = shipOrderAllocateModelList.stream().filter(p -> p.getPallet().equals(pallet)).map(AllocateModel::getAllocateQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    List<ShipOrderPalletRequest> added = shipOrderPalletRequestList.stream().filter(p -> p.getShipOrderCode().equals(shipOrderCode)).collect(Collectors.toList());

                    if (CollectionUtils.isNotEmpty(added)) {
                        ShipOrderPalletRequest shipOrderPalletRequest = added.get(0);
                        List<InventoryItemDetailRequest> inventoryItemDetailRequestList = shipOrderPalletRequest.getInventoryItemDetailDtoList();
                        InventoryItemDetailRequest detailRequest = getInventoryItemDetailRequest(truckOrderItemRequest, currentPalletQuantity, pallet);
                        inventoryItemDetailRequestList.add(detailRequest);
                    } else {
                        ShipOrderPalletRequest shipOrderPalletRequest = new ShipOrderPalletRequest();
                        shipOrderPalletRequest.setShipOrderCode(shipOrderCode);
                        List<InventoryItemDetailRequest> inventoryItemDetailRequestList = new ArrayList<>();
                        InventoryItemDetailRequest detailRequest = getInventoryItemDetailRequest(truckOrderItemRequest, currentPalletQuantity, pallet);
                        inventoryItemDetailRequestList.add(detailRequest);
                        shipOrderPalletRequest.setInventoryItemDetailDtoList(inventoryItemDetailRequestList);
                        shipOrderPalletRequestList.add(shipOrderPalletRequest);
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(shipOrderPalletRequestList)) {
            throw new Exception("AllocateException:shipOrderPalletRequestList is empty");
        }
//        if (true) {
//            return;
//        }
        //校验
        for (ShipOrderPalletRequest shipOrderPalletRequest : shipOrderPalletRequestList) {
            if (StringUtils.isEmpty(shipOrderPalletRequest.getShipOrderCode())) {
                throw new Exception("AllocateException:ShipOrderPalletRequest ShipOrderCode is empty");
            }
            if (CollectionUtils.isEmpty(shipOrderPalletRequest.getInventoryItemDetailDtoList())) {
                throw new Exception("AllocateException:ShipOrderPalletRequest InventoryItemDetailDtoList is empty");

            }
            for (InventoryItemDetailRequest detailRequest : shipOrderPalletRequest.getInventoryItemDetailDtoList()) {
                if (StringUtils.isEmpty(detailRequest.getPallet())) {
                    throw new Exception("AllocateException:InventoryItemDetailRequest Pallet is empty");
                }
                if (StringUtils.isEmpty(detailRequest.getMaterialCode())) {
                    throw new Exception("AllocateException:InventoryItemDetailRequest MaterialCode is empty");
                }
                if (StringUtils.isEmpty(detailRequest.getM_Str7())) {
                    throw new Exception("AllocateException:InventoryItemDetailRequest M_Str7 is empty");
                }

            }
        }

        AddTruckOrderRequest splitRequest = new AddTruckOrderRequest();
        splitRequest.setTruckOrderItemRequestList(null);
        BeanUtils.copyProperties(request, splitRequest);
        splitRequest.setTruckOrderItemRequestList(splitTruckOrderItemRequestList);

//        long createTime = LocalDateTime.now().toInstant(ZoneOffset.of("+08:00")).toEpochMilli();
        long createTime = Instant.now().toEpochMilli();
        splitRequest.getTruckOrderRequest().setCreationTime(LocalDateTime.now());
        String addTruckOrderRequestJson = objectMapper.writeValueAsString(splitRequest);
        log.info("addTruckOrderRequestJson -:{}", addTruckOrderRequestJson);


        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "saveTruckOrderAndItem";
        stopWatch.start(currentTaskName);
        TruckOrder truckOrder = saveTruckOrderAndItem(splitRequest, createTime);
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        log.info("currentTaskName stopWatch {} cost {}", stopWatch.getId(), stopWatch.getTotalTimeMillis());
    }

    private InventoryItemDetailRequest getInventoryItemDetailRequest(TruckOrderItemRequest truckOrderItemRequest, BigDecimal movedPkgQuantity, String pallet) {
        InventoryItemDetailRequest detailRequest = new InventoryItemDetailRequest();
        detailRequest.setPallet(pallet);
        detailRequest.setUpdateMStr12(true);
        detailRequest.setM_Str7(truckOrderItemRequest.getProjectNo());
        detailRequest.setM_Str12(truckOrderItemRequest.getDeviceNo());
        detailRequest.setMovedPkgQuantity(movedPkgQuantity);
//        detailRequest.setId(truckOrderItemRequest.getInventoryItemDetailId());
        detailRequest.setMaterialCode(truckOrderItemRequest.getMaterialCode());
        return detailRequest;
    }

    private TruckOrder saveTruckOrderAndItem(AddTruckOrderRequest request, long createTime) throws Exception {

        if (CollectionUtils.isEmpty(request.getTruckOrderItemRequestList())) {
            throw new Exception("TruckOrderItem is empty");
        }

//        String shipOrderIds = request.getTruckOrderItemRequestList().get(0).getShipOrderId();
//        Long shipOrderId = Long.valueOf(shipOrderIds.split(",")[0]);
//        ShipPickOrderRequest shipPickOrderRequest = new ShipPickOrderRequest();
//        shipPickOrderRequest.setShipOrderId(shipOrderId);
//        if (createTime == 0) {
//            createTime = System.currentTimeMillis();
//        }
//        shipPickOrderRequest.setStartCreationTime(createTime);
//        Sort sort = new Sort();
//        sort.setSortType("desc");
//        sort.setSortField("Id");
//        List<Sort> sortList = new ArrayList<>();
//        sortList.add(sort);
//        shipPickOrderRequest.setSortFieldList(sortList);
//        shipPickOrderRequest.setPageSize(1);
//        shipPickOrderRequest.setPageIndex(1);
//        shipPickOrderRequest.setSearchCount(false);
//        PageData<ShipPickOrderResponse> shipPickOrderPage = shipPickOrderService.getShipPickOrderPage(shipPickOrderRequest);
//        List<ShipPickOrderResponse> shipPickOrderResponseList = shipPickOrderPage.getData();
//        if (shipPickOrderResponseList.size() != 1) {
//            String str = MessageFormat.format("Get ShipPickOrder by shipOrderId - {0} fail", shipOrderId);
////            throw new Exception(str);
//        }
//        ShipPickOrderResponse shipPickOrderResponse = shipPickOrderResponseList.get(0);


        LoginUserTokenDto user = UserInfoHolder.getUser();
        TruckOrderRequest truckOrderRequest = request.getTruckOrderRequest();
        LocalDateTime creationTime = LocalDateTime.now();
        if (request.getTruckOrderRequest().getCreationTime() != null) {
            creationTime = request.getTruckOrderRequest().getCreationTime();
        }
        truckOrderRequest.setStatus(TruckOrderStausEnum.NOT_DEBITED.getValue());
        truckOrderRequest.setCreationTime(creationTime);
        truckOrderRequest.setLastModificationTime(creationTime);
        truckOrderRequest.setCreatorId(user.getId());
        truckOrderRequest.setCreatorName(user.getAccountName());
//        truckOrderRequest.setCreatorId(shipPickOrderResponse.getCreatorId().toString());
//        truckOrderRequest.setCreatorName(shipPickOrderResponse.getCreatorName());

        TruckOrder truckOrder = add(request.getTruckOrderRequest());
        for (TruckOrderItemRequest truckOrderItemRequest : request.getTruckOrderItemRequestList()) {
            truckOrderItemRequest.setStatus(TruckOrderStausEnum.NOT_DEBITED.getValue());
            truckOrderItemRequest.setTruckOrderId(truckOrder.getId());
            truckOrderItemRequest.setCreationTime(LocalDateTime.now());
            truckOrderItemRequest.setLastModificationTime(LocalDateTime.now());
            truckOrderItemRequest.setCreatorId(user.getId());
            truckOrderItemRequest.setCreatorName(user.getAccountName());
//            truckOrderItemRequest.setCreatorId(shipPickOrderResponse.getCreatorId().toString());
//            truckOrderItemRequest.setCreatorName(shipPickOrderResponse.getCreatorName());

        }
        List<TruckOrderItem> truckOrderItemList = truckOrderItemService.addBatch(request.getTruckOrderItemRequestList());

        if (request.getAsync()) {
            List<MqMessageRequest> mqMessageRequestList = new ArrayList<>();
            for (TruckOrderItem truckOrderItem : truckOrderItemList) {
                String content = objectMapper.writeValueAsString(truckOrderItem.getId());
                MqMessageRequest mqMessage = new MqMessageRequest();
                mqMessage.setBusinessId(truckOrderItem.getId());
                mqMessage.setBusinessKey(UtilityConst.TRUCK_ORDER_ITEM_DEBIT);
                mqMessage.setMsgContent(content);
                mqMessage.setQueue(UtilityConst.TRUCK_ORDER_ITEM_DEBIT);
                mqMessage.setTopic(UtilityConst.TRUCK_ORDER_ITEM_DEBIT);
                mqMessage.setSendMq(false);
                mqMessageRequestList.add(mqMessage);
            }
            List<MqMessage> mqMessageList = mqMessageService.addMessageBatch(mqMessageRequestList);
            publishMsg(mqMessageList);
        }
        return truckOrder;
    }

    private void publishMsg(List<MqMessage> mqMessageList) {
        // 获取当前服务的ID springBootProject
        String serviceId = applicationContext.getId();
        // 或使用 busProperties.getId()
        // String serviceId = busProperties.getId();

        // 获取当前服务实例ID（通常与busProperties.getId()相同）  springBootProject:8088:f89b296d4ca5589865e70da7de918722
        String originService = busProperties.getId();

        //            EwmsEvent event = new EwmsEvent(this, "TruckOrderComplete");
        //  busProperties.getId():  contextId, // 通常是 spring.application.name
        CustomEvent event = new CustomEvent(this, originService, mqMessageList);
        log.info("ThreadId {} ,eventPublisher event", Thread.currentThread().getId());
        //最好使用本地消息表
        //发送消息的时候可能崩溃，不能保证消息被消费。如果发送成功了，还要设计消息表兜底失败的消息
//        MyCustomEvent event = new MyCustomEvent(busProperties.getId());
        eventPublisher.publishEvent(event);
    }


    private void trunkOrderMqtt(TruckOrder truckOrder) throws Exception {
        TrunkOderMq trunkOderMq = new TrunkOderMq();
        TruckOrderResponse truckOrderResponse = new TruckOrderResponse();
        BeanUtils.copyProperties(truckOrder, truckOrderResponse);
        List<TruckOrderResponse> truckOrderResponseList = new ArrayList<>();
        truckOrderResponseList.add(truckOrderResponse);

        List<TruckOrderItemResponse> truckOrderItemResponseList = new ArrayList<>();
        TruckOrderItemRequest truckOrderItemRequest = new TruckOrderItemRequest();
        truckOrderItemRequest.setPageIndex(1);
        truckOrderItemRequest.setPageSize(Integer.MAX_VALUE);
        truckOrderItemRequest.setSearchCount(false);
        truckOrderItemRequest.setTruckOrderId(truckOrder.getId());
        PageData<TruckOrderItemResponse> truckOrderItemResponsePageData = this.truckOrderItemService.getTruckOrderItemPage(truckOrderItemRequest);
        List<TruckOrderItemResponse> itemList = truckOrderItemResponsePageData.getData();

        if (itemList.size() <= 0) {
            throw new Exception("save TruckOrderItem exception");
        }
        String msgId = UUID.randomUUID().toString().replaceAll("-", "");
        trunkOderMq.setMsgId(msgId);
        trunkOderMq.setTruckOrderResponseList(truckOrderResponseList);
        trunkOderMq.setTruckOrderItemResponseList(itemList);


        PrintWrapper<TrunkOderMq> mqttWrapper = new PrintWrapper();
        mqttWrapper.setCount(1);
        mqttWrapper.setData(Arrays.asList(trunkOderMq));
        String jsonStr = upperObjectMapper.writeValueAsString(mqttWrapper);
        log.info("start publish msgId:{}", msgId);
        mqttProduce.publish(UtilityConst.TRUCK_ORDER_COMPLETE_TOPIC, jsonStr, msgId);
    }

    @Override
    public TruckOrder add(TruckOrderRequest truckOrderRequest) {
        TruckOrder truckOrder = new TruckOrder();
        BeanUtils.copyProperties(truckOrderRequest, truckOrder);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String currentTime = LocalDateTime.now().format(formatter);
        truckOrder.setTruckOrderCode(currentTime);
        this.save(truckOrder);
        return truckOrder;
    }

    @Override
    public void addTruckOrderAndItemOnly(AddTruckOrderRequest request, String token) throws Throwable {

        if (request.getTruckOrderRequest().getCreationTime() == null) {
            throw new Exception("creationTime is null");
        }
        long createTime = request.getTruckOrderRequest().getCreationTime().toInstant(ZoneOffset.of("+08:00")).toEpochMilli();
        saveTruckOrderAndItem(request, createTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTruckOrder(MultipartFile[] files, TruckOrderRequest request) throws Exception {
        if (request.getId() == null) {
            throw new Exception("id is null");
        }

        String lockKey = RedisKey.UPDATE_TRUCK_ORDER_INFO + ":" + request.getId();
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {

//            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, RedisKey.INIT_INVENTORY_INFO_FROM_DB_LEASE_TIME, TimeUnit.SECONDS);
            //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                throw new Exception(msg);
            }
            log.info("updateTruckOrder get lock {}", lockKey);


            TruckOrder truckOrder = this.getById(request.getId());
            if (truckOrder == null) {
                throw new Exception("Can not get truckOrder by id - " + request.getId());
            }
            if (truckOrder.getDeleted() == 1) {
                String msg = MessageFormat.format("truckOrder id - {0} is deleted", request.getId());
                throw new Exception(msg);
            }
            List<String> allFilePahList = new ArrayList<>();
            if (StringUtils.isNotEmpty(request.getFilePath())) {
                List<String> previousFilePahList = Arrays.asList(request.getFilePath().split(","));
                allFilePahList.addAll(previousFilePahList);
            }
//        http://localhost:8030/upload/20251009144517372/cat.png
            String filePath = request.getFilePath();
            if (files != null && files.length > 0) {
                String directory = uploadDirectory + "\\" + truckOrder.getTruckOrderCode() + "\\";
//            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//            String directory = uploadDirectory + "\\" + dateStr + "\\" + truckOrder.getTruckOrderCode() + "\\";

                List<String> filePahList = FileUtil.saveFiles(files, directory);
                filePahList = filePahList.stream().map(p -> wmsFrontServer + PathUtils.removeDriveLetterAndNormalize(p)).collect(Collectors.toList());
                allFilePahList.addAll(filePahList);
                allFilePahList = allFilePahList.stream().distinct().collect(Collectors.toList());
                filePath = String.join(",", allFilePahList);
            }

            LoginUserTokenDto userTokenDto = UserInfoHolder.getUser();
            if (userTokenDto != null) {
                request.setLastModifierId(userTokenDto.getId());
                request.setLastModifierName(userTokenDto.getUserName());
            }

            LambdaUpdateWrapper<TruckOrder> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(TruckOrder::getSenderAddress, request.getSenderAddress());
            updateWrapper.set(TruckOrder::getReceiverAddress, request.getReceiverAddress());
            updateWrapper.set(TruckOrder::getSenderPhone, request.getSenderPhone());
            updateWrapper.set(TruckOrder::getReceiverPhone, request.getReceiverPhone());
            updateWrapper.set(request.getSendTime() != null, TruckOrder::getSendTime, request.getSendTime());
            updateWrapper.set(TruckOrder::getTrunkType, request.getTrunkType());
            updateWrapper.set(TruckOrder::getDriverPhone, request.getDriverPhone());
            updateWrapper.set(TruckOrder::getTrunkNo, request.getTrunkNo());
            updateWrapper.set(TruckOrder::getFilePath, filePath);
            updateWrapper.set(TruckOrder::getLastModificationTime, LocalDateTime.now());
            updateWrapper.set(TruckOrder::getVersion, truckOrder.getVersion() + 1);
            updateWrapper.eq(TruckOrder::getId, request.getId());
            updateWrapper.eq(TruckOrder::getDeleted, 0);
            updateWrapper.eq(TruckOrder::getVersion, truckOrder.getVersion());

            boolean re = this.update(updateWrapper);
            if (!re) {
                String msg = MessageFormat.format("Update truckOrder id - {0} fail", request.getId());
                throw new Exception(msg);
            }

            MqMessageRequest mqMessageRequest = new MqMessageRequest();
            String content = objectMapper.writeValueAsString(truckOrder.getId());
            mqMessageRequest.setBusinessId(truckOrder.getId());
            mqMessageRequest.setBusinessKey(truckOrder.getTruckOrderCode());
            mqMessageRequest.setMsgContent(content);
            mqMessageRequest.setExchange(RabbitMQConfig.DIRECT_EXCHANGE);
            mqMessageRequest.setRouteKey(RabbitMQConfig.DIRECT_MQ_MESSAGE_KEY);
            mqMessageRequest.setQueue(RabbitMQConfig.DIRECT_MQ_MESSAGE_NAME);
            mqMessageRequest.setSendMq(true);
            MqMessage updateTruckOrderStatusMsg = mqMessageService.addMessage(mqMessageRequest);
        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {
            //非事务操作在此释放
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
            redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void expungeStaleAttachment(long id) throws Exception {
        log.info("expungeStaleAttachment - {}", id);

        String lockKey = RedisKey.UPDATE_TRUCK_ORDER_INFO + ":" + id;
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {

//            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, RedisKey.INIT_INVENTORY_INFO_FROM_DB_LEASE_TIME, TimeUnit.SECONDS);
            //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                throw new Exception(msg);
            }
            log.info("expungeStaleAttachment get lock {}", lockKey);

            TruckOrder truckOrder = this.getById(id);
            if (truckOrder == null) {
                throw new Exception("Can not get TruckOrder by id " + id);
            }
            String directory = uploadDirectory + "\\" + truckOrder.getTruckOrderCode() + "\\";

            Path startPath = Paths.get(directory);
            if (!Files.exists(startPath)) {
                String fullPath = startPath.toAbsolutePath().normalize().toString();
                log.info("directory {} doesn't exist", fullPath);
                return;
            }
            // 获取所有文件（包括子目录）
            List<Path> allFiles = Files.walk(startPath)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            List<String> truckOrderAttachmentList = new ArrayList<>();
            if (StringUtils.isNotEmpty(truckOrder.getFilePath())) {
                truckOrderAttachmentList = Arrays.asList(truckOrder.getFilePath().split(","));
            }
            truckOrderAttachmentList = truckOrderAttachmentList.stream().map(p -> p.replace(wmsFrontServer, "")).collect(Collectors.toList());
            //删除磁盘多余文件
            for (Path path : allFiles) {
                // 输出: C:\workspace\project\example.txt (Windows)
                // 输出: /home/user/workspace/project/example.txt (Linux)
                String filePath = path.toAbsolutePath().toString();
                String newFilePath = PathUtils.removeDriveLetterAndNormalize(filePath);
                if (!truckOrderAttachmentList.contains(newFilePath)) {
                    Files.delete(path);
                    log.info("Disk expungeStaleAttachment {}", filePath);
                }
            }

            //删除不在磁盘的文件路径
            List<String> diskFilePath = allFiles.stream().map(p -> PathUtils.removeDriveLetterAndNormalize(p.toAbsolutePath().toString())).collect(Collectors.toList());
            List<String> existFilePathList = new ArrayList<>();
            for (String filePath : truckOrderAttachmentList) {
                if (diskFilePath.contains(filePath)) {
                    existFilePathList.add(filePath);
                } else {
                    log.info("TruckOrder expungeStaleAttachment {}", filePath);
                }
            }

            List<String> newPathList = existFilePathList.stream().map(p -> wmsFrontServer + PathUtils.removeDriveLetterAndNormalize(p)).collect(Collectors.toList());
            String newFilePath = String.join(",", newPathList);

            LambdaUpdateWrapper<TruckOrder> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(TruckOrder::getFilePath, newFilePath);
            updateWrapper.set(TruckOrder::getLastModificationTime, LocalDateTime.now());
            updateWrapper.set(TruckOrder::getVersion, truckOrder.getVersion() + 1);
            updateWrapper.eq(TruckOrder::getId, truckOrder.getId());
            updateWrapper.eq(TruckOrder::getDeleted, 0);
            updateWrapper.eq(TruckOrder::getVersion, truckOrder.getVersion());

            boolean re = this.update(updateWrapper);
            if (!re) {
                String msg = MessageFormat.format("Update truckOrder id - {0} fail", truckOrder.getId());
                throw new Exception(msg);
            }

        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {
            //非事务操作在此释放
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
            redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
        }
    }


    @Override
    public PageData<TruckOrderResponse> getTruckOrderPage(TruckOrderRequest request) {
        LambdaQueryWrapper<TruckOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TruckOrder::getDeleted, 0);
        if (StringUtils.isNotEmpty(request.getTruckOrderCode())) {
            queryWrapper.eq(TruckOrder::getTruckOrderCode, request.getTruckOrderCode());
        }

        if (StringUtils.isNotEmpty(request.getSenderAddress())) {
            queryWrapper.like(TruckOrder::getSenderAddress, request.getSenderAddress());
        }
        if (StringUtils.isNotEmpty(request.getReceiverAddress())) {
            queryWrapper.like(TruckOrder::getReceiverAddress, request.getReceiverAddress());

        }
        if (StringUtils.isNotEmpty(request.getSenderPhone())) {
            queryWrapper.like(TruckOrder::getSenderPhone, request.getSenderPhone());
        }
        if (StringUtils.isNotEmpty(request.getReceiverPhone())) {
            queryWrapper.like(TruckOrder::getReceiverPhone, request.getReceiverPhone());
        }
//        if (StringUtils.isNotEmpty(request.getTrunkType())) {
//            queryWrapper.like(TruckOrder::getTrunkType, request.getTrunkType());
//        }
        if (StringUtils.isNotEmpty(request.getDriverPhone())) {
            queryWrapper.like(TruckOrder::getDriverPhone, request.getDriverPhone());
        }
        if (StringUtils.isNotEmpty(request.getTrunkNo())) {
            queryWrapper.like(TruckOrder::getTrunkNo, request.getTrunkNo());
        }
        if (StringUtils.isNotEmpty(request.getTrunkType())) {
            queryWrapper.like(TruckOrder::getTrunkType, request.getTrunkType());
        }
        if (StringUtils.isNotEmpty(request.getCreatorName())) {
            queryWrapper.like(TruckOrder::getCreatorName, request.getCreatorName());
        }
        // 创建分页对象 (当前页, 每页大小)
        Page<TruckOrder> page = new Page<>(request.getPageIndex(), request.getPageSize());

        if (CollectionUtils.isEmpty(request.getSortFieldList())) {
            List<Sort> sortFieldList = new ArrayList<>();
            Sort sort = new Sort();
            sort.setSortField("id");
            sort.setSortType("desc");
            sortFieldList.add(sort);
            request.setSortFieldList(sortFieldList);
        }
        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
            List<OrderItem> orderItems = LambdaFunctionHelper.getWithDynamicSort(request.getSortFieldList());
            page.setOrders(orderItems);
        }

        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<TruckOrder> truckOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取当前页数据
        List<TruckOrder> records = truckOrderPage.getRecords();
        long total = truckOrderPage.getTotal();

        List<TruckOrderResponse> truckOrderResponseResponseList = records.stream().map(p -> {
            TruckOrderResponse response = new TruckOrderResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<TruckOrderResponse> pageData = new PageData<>();
        pageData.setData(truckOrderResponseResponseList);
        pageData.setCount(total);
        return pageData;
    }

    @Override
    public void trunkOrderMq(Integer id) throws Exception {
        TruckOrder truckOrder = this.getById(id);
        if (truckOrder == null) {
            String msg = MessageFormat.format("Can't get TruckOrder by id - {0}", id);
            throw new Exception(msg);
        }
        trunkOrderMqtt(truckOrder);
    }

    @Override
    public void exportTrunkOrderExcel(Long id, HttpServletResponse httpServletResponse) throws Exception {

        TruckOrder trunkOrder = this.getById(id);
        if (trunkOrder == null) {
            throw new Exception(MessageFormat.format("TruckOrder - {0} doesn't exist", id));
        }
        TruckOrderItemRequest truckOrderItemRequest = new TruckOrderItemRequest();
        truckOrderItemRequest.setTruckOrderId(id);
        truckOrderItemRequest.setSearchCount(false);
        truckOrderItemRequest.setPageSize(Integer.MAX_VALUE);
        PageData<TruckOrderItemResponse> itemPage = this.truckOrderItemService.getTruckOrderItemPage(truckOrderItemRequest);
        List<TruckOrderItemResponse> itemList = itemPage.getData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String sendTimeStr = LocalDateTime.now().format(formatter);

        Map<String, Object> map = new HashMap<>();
        map.put("senderAddress", trunkOrder.getSenderAddress());
        map.put("receiverAddress", trunkOrder.getReceiverAddress());
        map.put("senderPhone", trunkOrder.getSenderPhone());
        map.put("receiverPhone", trunkOrder.getReceiverPhone());
        map.put("sendTime", sendTimeStr);
        map.put("trunkType", trunkOrder.getTrunkType());
        map.put("driverPhone", trunkOrder.getDriverPhone());
        map.put("trunkNo", trunkOrder.getTrunkNo());

//        List<TruckOrderItemBo> items = new ArrayList<>();
//        items.add(new TruckOrderItemBo(1, "XM001", "供电工程", "CK20230601", "WL1001", "电缆", "SB1001", BigDecimal.ONE, "第1批", "注意防水"));
//        items.add(new TruckOrderItemBo(2, "XM002", "管道工程", "CK20230602", "WL1002", "法兰", "SB1002", BigDecimal.TEN, "第1批", ""));

        int seqNo = 0;
        for (TruckOrderItemResponse item : itemList) {
            item.setSeqNo(++seqNo);
        }


//        String templatePath = "发车单模板.xlsx";
//        String outputPath = "发货单导出.xlsx";

        String currentWorkingDir = System.getProperty("user.dir");
        File folder = new File(currentWorkingDir + File.separator + "tmp");
        // 写入Excel文件
        String templatePath = MessageFormat.format("{0}/{1}.xlsx", folder, "发车单模板");
        String outputPath = MessageFormat.format("{0}/{1}.xlsx", folder, "发货单导出");


        // Excel 文件路径（确保存在）
        File file = new File(templatePath);

        if (!file.exists()) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // 输出设置
        prepareResponds("发车单导出", httpServletResponse);

        /* 4) 直接把 EasyExcel 写到浏览器输出流 */
        try (ServletOutputStream out = httpServletResponse.getOutputStream()) {

            ExcelWriter writer = EasyExcel.write(out).autoCloseStream(true)       // 写完顺带关流
                    .withTemplate(templatePath).build();
            WriteSheet sheet = EasyExcel.writerSheet().build();
            // ① 填充单值
            writer.fill(map, sheet);
            // ② 填充列表
            writer.fill(new FillWrapper("items", itemList), FillConfig.builder().forceNewRow(true).build(), sheet);
            writer.finish();  // !!! 必须调用，否则文件只写了一半
        }

    }


    private void prepareResponds(String fileName, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8'zh_cn'" + fileName + ExcelTypeEnum.XLSX.getValue());

//        fileName = URLEncoder.encode(fileName, "UTF-8");
//        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setCharacterEncoding("utf-8");
//        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
//        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");


    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<Long> idList) {
        LambdaUpdateWrapper<TruckOrder> truckOrderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        truckOrderLambdaUpdateWrapper.in(TruckOrder::getId, idList).set(TruckOrder::getDeleted, 1);
        boolean re = this.update(null, truckOrderLambdaUpdateWrapper);
        return re;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void synchronizeStatus(MqMessage mqMessage) throws Exception {

        log.info("synchronizeStatus MqMessage - {}", objectMapper.writeValueAsString(mqMessage));
        String lockKey = RedisKey.SYNCHRONIZE_TRUCK_ORDER_STATUS + ":" + mqMessage.getBusinessId();
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {
            lockSuccessfully = lock.tryLock();
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                log.info(msg);
                return;
            }
            log.info("update get lock {}", lockKey);

            long truckOrderId = mqMessage.getBusinessId();
            TruckOrderItemRequest truckOrderItemRequest = new TruckOrderItemRequest();
            truckOrderItemRequest.setTruckOrderId(truckOrderId);
            truckOrderItemRequest.setSearchCount(false);
            truckOrderItemRequest.setPageSize(Integer.MAX_VALUE);
            PageData<TruckOrderItemResponse> pageData = this.truckOrderItemService.getTruckOrderItemPage(truckOrderItemRequest);
            List<TruckOrderItemResponse> truckOrderItemResponseList = pageData.getData();
            if (CollectionUtils.isEmpty(truckOrderItemResponseList)) {
                throw new Exception("Can't get TruckOrderItem by TruckOrderId " + truckOrderId);
            }
            List<Integer> statusList = truckOrderItemResponseList.stream().map(p -> p.getStatus())
                    .sorted(Collections.reverseOrder())
                    .distinct().collect(Collectors.toList());


            TruckOrder truckOrder = this.getById(truckOrderId);
            if (truckOrder == null) {
                String msg = MessageFormat.format("TruckOrder {0} doesn't exist", truckOrderId);
                throw new Exception(msg);
            }
            if (truckOrder.getStatus().equals(TruckOrderStausEnum.DEBITED.getValue())) {
                log.info("truckOrderItem - {} has been debited", truckOrder.getId());
                return;
            }
            int truckOrderStatus = truckOrder.getStatus() == null ? 0 : truckOrder.getStatus().intValue();
            boolean updateTruckOrder = false;
            int maxStatus = statusList.get(0);
//            TruckOrderStausEnum itemStausEnum = TruckOrderStausEnum.getTruckOrderStausEnum(maxStatus);
//            switch (itemStausEnum) {
//                //不能使用枚举
//                case NOT_DEBITED:
//                    break;
//                case DEBITING:
//                    break;
//                case DEBITED:
//                    break;
//                default:
//                    break;
//            }
            if (maxStatus != TruckOrderStausEnum.DEBITED.getValue()) {
                if (truckOrderStatus < maxStatus) {
                    updateTruckOrder = true;
                    truckOrder.setStatus(maxStatus);
                    log.info("truckOrderUpdateStatus {} set status {}", truckOrder.getId(), maxStatus);
                }
            } else {
                //只有一种状态（完成）
                if (statusList.size() == 1) {
                    //不是完成更新为完成
                    if (truckOrderStatus != maxStatus) {
                        updateTruckOrder = true;
                        truckOrder.setStatus(maxStatus);
                        log.info("truckOrderUpdateStatus {} set status {}", truckOrder.getId(), maxStatus);
                    }
                }
            }
            if (!updateTruckOrder) {
                return;
            }
            this.updateTruckOrder(truckOrder);
            LoginUserTokenDto userTokenDto = UserInfoHolder.getUser(truckOrder.getCreatorId());
            if (userTokenDto != null) {
                TruckOrderResponse response = new TruckOrderResponse();
                BeanUtils.copyProperties(truckOrder, response);
                String msg = objectMapper.writeValueAsString(response);
                String userId = userTokenDto.getId();
                //事务回调：事务同步，此处待处理， 所有事务提交了才会执行 事务回调
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCompletion(int status) {
                        sseEmitterService.sendMsgToClient(userId, msg);
                    }
                });
            }

        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {
            redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
        }

    }

    @Override
    public void updateTruckOrder(TruckOrder truckOrder) throws Exception {
        String lockKey = RedisKey.UPDATE_TRUCK_ORDER_INFO + ":" + truckOrder.getId();
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {

            //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                throw new Exception(msg);
            }
            log.info("update get lock {}", lockKey);

            Integer oldVersion = truckOrder.getVersion();
            truckOrder.setVersion(truckOrder.getVersion() + 1);
            truckOrder.setLastModificationTime(LocalDateTime.now());
            LambdaUpdateWrapper<TruckOrder> updateWrapper = new LambdaUpdateWrapper<TruckOrder>();
            updateWrapper.eq(TruckOrder::getVersion, oldVersion);
            updateWrapper.eq(TruckOrder::getId, truckOrder.getId());
            boolean re = this.update(truckOrder, updateWrapper);
            if (!re) {
                String message = MessageFormat.format("TruckOrder update fail :id - {0} ,version - {1}", truckOrder.getId(), oldVersion);
                throw new Exception(message);
            }


        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {
            //非事务操作在此释放
//            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
            redisUtil.releaseLockAfterTransaction(lock, lockSuccessfully);
        }
    }

}




