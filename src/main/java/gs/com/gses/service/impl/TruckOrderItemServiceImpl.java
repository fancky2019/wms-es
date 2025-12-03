package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.filter.UserInfoHolder;
import gs.com.gses.listener.event.EwmsEvent;
import gs.com.gses.listener.event.EwmsEventTopic;
import gs.com.gses.listener.eventbus.CustomEvent;
import gs.com.gses.model.bo.wms.AllocateModel;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.enums.MqMessageSourceEnum;
import gs.com.gses.model.enums.MqMessageStatus;
import gs.com.gses.model.enums.TruckOrderStausEnum;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.authority.LoginUserTokenDto;
import gs.com.gses.model.request.wms.*;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.mqtt.PrintWrapper;
import gs.com.gses.model.response.mqtt.TrunkOrderBarCode;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.model.utility.RedisKey;
import gs.com.gses.rabbitMQ.mqtt.MqttProduce;
import gs.com.gses.rabbitMQ.mqtt.Topics;
import gs.com.gses.service.*;
import gs.com.gses.mapper.TruckOrderItemMapper;
import gs.com.gses.service.api.WmsService;
import gs.com.gses.sse.ISseEmitterService;
import gs.com.gses.utility.LambdaFunctionHelper;
import gs.com.gses.utility.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StopWatch;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【TruckOrderItem】的数据库操作Service实现
 * @createDate 2025-05-28 13:18:54
 */
@Slf4j
@Service
public class TruckOrderItemServiceImpl extends ServiceImpl<TruckOrderItemMapper, TruckOrderItem> implements TruckOrderItemService {

    @Autowired
    private ShipOrderItemService shipOrderItemService;

    @Autowired
    private InventoryItemDetailService inventoryItemDetailService;

    @Autowired
    private TruckOrderService truckOrderService;

    @Autowired
    private ShipOrderService shipOrderService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private MqttProduce mqttProduce;
    @Autowired
    @Qualifier("upperObjectMapper")
    private ObjectMapper upperObjectMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MqMessageService mqMessageService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private BusProperties busProperties;
    @Autowired
    private ISseEmitterService sseEmitterService;
    @Autowired
    private WmsService wmsService;

    @Override
    public Boolean checkAvailable(TruckOrderItemRequest request, List<ShipOrderItemResponse> matchedShipOrderItemResponseList, List<AllocateModel> allocateModelList) throws Exception {
        String currentTaskName = "checkTruckOrderItemRequest";
        StopWatch stopWatch = new StopWatch("checkShipOrderItemExist");
        stopWatch.start(currentTaskName);
        ShipOrderItemRequest shipOrderItemRequest = new ShipOrderItemRequest();
        shipOrderItemRequest.setM_Str7(request.getProjectNo());
        shipOrderItemRequest.setM_Str12(request.getDeviceNo());
        shipOrderItemRequest.setMaterialCode(request.getMaterialCode());
        shipOrderItemRequest.setRequiredPkgQuantity(request.getQuantity());
        if (matchedShipOrderItemResponseList == null) {
            matchedShipOrderItemResponseList = new ArrayList<>();
        }
        if (allocateModelList == null) {
            allocateModelList = new ArrayList<>();
        }
        Boolean shipOrderItemExist = shipOrderItemService.checkItemExist(shipOrderItemRequest, matchedShipOrderItemResponseList);
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "checkInventoryItemDetailExist";
        stopWatch.start(currentTaskName);
        InventoryItemDetailRequest inventoryItemDetailRequest = new InventoryItemDetailRequest();
        inventoryItemDetailRequest.setM_Str7(request.getProjectNo());
        inventoryItemDetailRequest.setM_Str12(request.getDeviceNo());
        inventoryItemDetailRequest.setMaterialCode(request.getMaterialCode());
        inventoryItemDetailRequest.setPackageQuantity(request.getQuantity());
        inventoryItemDetailRequest.setIgnoreDeviceNo(request.getIgnoreDeviceNo());
        Boolean detailExist = inventoryItemDetailService.checkDetailExist(inventoryItemDetailRequest, matchedShipOrderItemResponseList, allocateModelList);
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "getMaterialInfo";
        stopWatch.start(currentTaskName);
        request.setPallet(inventoryItemDetailRequest.getPallet());
        request.setMaterialId(inventoryItemDetailRequest.getMaterialId());
        request.setInventoryItemDetailId(inventoryItemDetailRequest.getId());
        Material material = materialService.getById(request.getMaterialId());
        if (material == null) {
            throw new Exception("can't find material - " + request.getMaterialId());
        }
        request.setDeviceName(material.getXName());
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        log.info("currentTaskName stopWatch {} cost {}", stopWatch.getId(), stopWatch.getTotalTimeMillis());

        return shipOrderItemExist && detailExist;
    }


    @Override
    public Boolean checkAvailableBatch(List<TruckOrderItemRequest> requestList, List<ShipOrderItemResponse> matchedShipOrderItemResponseList, List<AllocateModel> allocateModelList) throws Exception {
        if (CollectionUtils.isEmpty(requestList)) {
            throw new Exception("TruckOrderItemRequest is null");
        }
        String currentTaskName = "getMaterialInfo";
        StopWatch stopWatch = new StopWatch("checkAvailableBatch");
        stopWatch.start(currentTaskName);
        List<String> materialCodeList = requestList.stream().map(p -> p.getMaterialCode()).distinct().collect(Collectors.toList());
        List<Material> materialList = this.materialService.getByCodeList(materialCodeList);
        Map<String, Material> materialMap = materialList.stream().collect(Collectors.toMap(p -> p.getXCode(), p -> p));
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "PrepareRequest";
        stopWatch.start(currentTaskName);

        List<ShipOrderItemRequest> shipOrderItemRequestList = new ArrayList<>();
        List<InventoryItemDetailRequest> inventoryItemDetailRequestList = new ArrayList<>();
        for (TruckOrderItemRequest request : requestList) {

            if (StringUtils.isEmpty(request.getProjectNo())) {
                throw new Exception("ProjectNo is null");
            }

            if (StringUtils.isEmpty(request.getMaterialCode())) {
                throw new Exception("materialCode is null");
            }
            Material material = materialMap.get(request.getMaterialCode());
            request.setMaterialId(material.getId());
            ShipOrderItemRequest shipOrderItemRequest = new ShipOrderItemRequest();
            shipOrderItemRequest.setM_Str7(request.getProjectNo());
            shipOrderItemRequest.setM_Str12(request.getDeviceNo());
            shipOrderItemRequest.setMaterialCode(request.getMaterialCode());
            shipOrderItemRequest.setMaterialId(material.getId());
            shipOrderItemRequest.setRequiredPkgQuantity(request.getQuantity());
            shipOrderItemRequestList.add(shipOrderItemRequest);

            InventoryItemDetailRequest inventoryItemDetailRequest = new InventoryItemDetailRequest();
            inventoryItemDetailRequest.setM_Str7(request.getProjectNo());
            inventoryItemDetailRequest.setM_Str12(request.getDeviceNo());
            inventoryItemDetailRequest.setMaterialCode(request.getMaterialCode());
            inventoryItemDetailRequest.setMaterialId(material.getId());
            inventoryItemDetailRequest.setPackageQuantity(request.getQuantity());
            inventoryItemDetailRequest.setIgnoreDeviceNo(request.getIgnoreDeviceNo());
            inventoryItemDetailRequestList.add(inventoryItemDetailRequest);
        }

        if (matchedShipOrderItemResponseList == null) {
            matchedShipOrderItemResponseList = new ArrayList<>();
        }
        if (allocateModelList == null) {
            allocateModelList = new ArrayList<>();
        }
        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "checkItemExistBatch";
        stopWatch.start(currentTaskName);
        Boolean shipOrderItemExist = shipOrderItemService.checkItemExistBatch(shipOrderItemRequestList, matchedShipOrderItemResponseList);

        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        currentTaskName = "checkDetailExistBatch";
        stopWatch.start(currentTaskName);
        Boolean detailExist = inventoryItemDetailService.checkDetailExistBatch(inventoryItemDetailRequestList, matchedShipOrderItemResponseList, allocateModelList);

        stopWatch.stop();
        log.info("currentTaskName {} cost {}", currentTaskName, stopWatch.getLastTaskTimeMillis());
        log.info("currentTaskName stopWatch {} cost {}", stopWatch.getId(), stopWatch.getTotalTimeMillis());

        return shipOrderItemExist && detailExist;

    }

    @Override
    public Boolean add(TruckOrderItemRequest request) {
        TruckOrderItem truckOrderItem = new TruckOrderItem();
        BeanUtils.copyProperties(request, truckOrderItem);
        this.save(truckOrderItem);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(TruckOrderItem truckOrderItem) throws Exception {
        String lockKey = RedisKey.UPDATE_TRUCK_ORDER_ITEM + ":" + truckOrderItem.getId();
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

            Integer oldVersion = truckOrderItem.getVersion();
            truckOrderItem.setVersion(truckOrderItem.getVersion() + 1);
            truckOrderItem.setLastModificationTime(LocalDateTime.now());
            LambdaUpdateWrapper<TruckOrderItem> updateWrapper = new LambdaUpdateWrapper<TruckOrderItem>();
            updateWrapper.eq(TruckOrderItem::getVersion, oldVersion);
            updateWrapper.eq(TruckOrderItem::getId, truckOrderItem.getId());
            boolean re = this.update(truckOrderItem, updateWrapper);
            if (!re) {
                String message = MessageFormat.format("TruckOrderItem update fail :id - {0} ,version - {1}", truckOrderItem.getId(), oldVersion);
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

    @Override
    public List<TruckOrderItem> addBatch(List<TruckOrderItemRequest> requestList) {
        List<TruckOrderItem> truckOrderItemList = new ArrayList<>();
        for (TruckOrderItemRequest request : requestList) {
            TruckOrderItem item = new TruckOrderItem();
            BeanUtils.copyProperties(request, item);
            this.save(item);
            truckOrderItemList.add(item);
        }
        //SQL Server的JDBC驱动限制：SQL Server的JDBC驱动在批量插入时无法完美支持返回所有插入记录的主键值，只能返回最后一个插入记录的主键值
//        this.saveBatch(truckOrderItemList);

//        this.customSaveBatch(truckOrderItemList);

        return truckOrderItemList;
    }

    @Override
    public void trunkBarCodeMq(TruckOrderItemRequest truckOrderItemRequest) throws Exception {
        if (StringUtils.isNotEmpty(truckOrderItemRequest.getDeviceNo())) {
            if (truckOrderItemRequest.getDeviceNo().contains(",")) {
                throw new Exception("设备号不能包含逗号");
            }
        }
        String trunkBarCode = MessageFormat.format("{0},{1},{2}", truckOrderItemRequest.getProjectNo(), truckOrderItemRequest.getDeviceNo(), truckOrderItemRequest.getMaterialCode());
        TrunkOrderBarCode trunkOrderBarCode = new TrunkOrderBarCode();
        trunkOrderBarCode.setBarCode(trunkBarCode);

        PrintWrapper<TrunkOrderBarCode> printWrapper = new PrintWrapper<>();
        printWrapper.setCount(1);
        printWrapper.setData(Arrays.asList(trunkOrderBarCode));
        //        C#接收
        String json = upperObjectMapper.writeValueAsString(printWrapper);
        mqttProduce.publish(Topics.TRUNK_CODE, json, UUID.randomUUID().toString().replaceAll("-", ""));

    }

    @Transactional(rollbackFor = Exception.class)
    public void safeBatchInsert(List<TruckOrderItem> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // 使用BATCH执行器
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
        try {
            TruckOrderItemMapper mapper = sqlSession.getMapper(TruckOrderItemMapper.class);
            for (TruckOrderItem item : list) {
                mapper.insert(item);
            }
            sqlSession.commit();  // 手动提交
        } finally {
            sqlSession.close();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean customSaveBatch(Collection<TruckOrderItem> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        try (SqlSession batchSqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH)) {
            TruckOrderItemMapper batchMapper = batchSqlSession.getMapper(TruckOrderItemMapper.class);
            int i = 0;
            for (TruckOrderItem entity : entityList) {
                batchMapper.insert(entity);
                i++;
                if (i % 1000 == 0) {
                    batchSqlSession.flushStatements();
                }
            }
            batchSqlSession.flushStatements();
            return true;
        }
    }

    @Override
    public PageData<TruckOrderItemResponse> getTruckOrderItemPage(TruckOrderItemRequest request) throws Exception {
        LambdaQueryWrapper<TruckOrderItem> truckOrderItemQueryWrapper = new LambdaQueryWrapper<>();
        truckOrderItemQueryWrapper.eq(TruckOrderItem::getDeleted, 0);
        if (StringUtils.isNotEmpty(request.getTruckOrderCode())) {
            LambdaQueryWrapper<TruckOrder> truckOrderQueryWrapper = new LambdaQueryWrapper<>();
            truckOrderQueryWrapper.eq(TruckOrder::getTruckOrderCode, request.getTruckOrderCode());
            List<TruckOrder> truckOrderList = this.truckOrderService.list(truckOrderQueryWrapper);
            if (CollectionUtils.isNotEmpty(truckOrderList)) {
                List<Long> truckOrderIdList = truckOrderList.stream().map(TruckOrder::getId).distinct().collect(Collectors.toList());
                truckOrderItemQueryWrapper.in(TruckOrderItem::getTruckOrderId, truckOrderIdList);
            } else {
                return new PageData<>();
            }
        }

        if (request.getTruckOrderId() != null && request.getTruckOrderId() > 0) {
            truckOrderItemQueryWrapper.eq(TruckOrderItem::getTruckOrderId, request.getTruckOrderId());
        }

        if (CollectionUtils.isNotEmpty(request.getTruckOrderIdList())) {
            truckOrderItemQueryWrapper.in(TruckOrderItem::getTruckOrderId, request.getTruckOrderIdList());
        }

        if (StringUtils.isNotEmpty(request.getProjectNo())) {
            truckOrderItemQueryWrapper.like(TruckOrderItem::getProjectNo, request.getProjectNo());
        }
        if (StringUtils.isNotEmpty(request.getProjectName())) {
            truckOrderItemQueryWrapper.like(TruckOrderItem::getProjectName, request.getProjectName());
        }
        if (StringUtils.isNotEmpty(request.getApplyShipOrderCode())) {
            truckOrderItemQueryWrapper.like(TruckOrderItem::getApplyShipOrderCode, request.getApplyShipOrderCode());
        }
        if (StringUtils.isNotEmpty(request.getShipOrderCode())) {
            LambdaQueryWrapper<ShipOrder> shipOrderWrapper = new LambdaQueryWrapper<>();
            shipOrderWrapper.eq(ShipOrder::getXCode, request.getShipOrderCode());
            List<ShipOrder> shipOrderList = this.shipOrderService.list(shipOrderWrapper);
            if (CollectionUtils.isNotEmpty(shipOrderList)) {
                List<Long> shipOrderIdList = shipOrderList.stream().map(ShipOrder::getId).distinct().collect(Collectors.toList());
                truckOrderItemQueryWrapper.in(TruckOrderItem::getShipOrderId, shipOrderIdList);
            }
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {
            LambdaQueryWrapper<Material> materialLambdaQueryWrapper = new LambdaQueryWrapper<>();
            materialLambdaQueryWrapper.eq(Material::getXCode, request.getMaterialCode());
            List<Material> materialList = this.materialService.list(materialLambdaQueryWrapper);
            if (CollectionUtils.isNotEmpty(materialList)) {
                List<Long> materialIdList = materialList.stream().map(Material::getId).distinct().collect(Collectors.toList());
                truckOrderItemQueryWrapper.in(TruckOrderItem::getMaterialId, materialIdList);
            }
        }

        if (StringUtils.isNotEmpty(request.getDeviceName())) {
            truckOrderItemQueryWrapper.like(TruckOrderItem::getDeviceName, request.getDeviceName());
        }
        if (StringUtils.isNotEmpty(request.getDeviceNo())) {
            truckOrderItemQueryWrapper.like(TruckOrderItem::getDeviceNo, request.getDeviceNo());
        }

        if (StringUtils.isNotEmpty(request.getSendBatchNo())) {
            truckOrderItemQueryWrapper.like(TruckOrderItem::getSendBatchNo, request.getSendBatchNo());
        }

        if (StringUtils.isNotEmpty(request.getRemark())) {
            truckOrderItemQueryWrapper.like(TruckOrderItem::getRemark, request.getRemark());
        }
        if (StringUtils.isNotEmpty(request.getCreatorName())) {
            truckOrderItemQueryWrapper.like(TruckOrderItem::getCreatorName, request.getCreatorName());
        }

        // 创建分页对象 (当前页, 每页大小)
        Page<TruckOrderItem> page = new Page<>(request.getPageIndex(), request.getPageSize());
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
        IPage<TruckOrderItem> truckOrderPage = this.baseMapper.selectPage(page, truckOrderItemQueryWrapper);

        // 获取当前页数据
        List<TruckOrderItem> records = truckOrderPage.getRecords();
        long total = truckOrderPage.getTotal();

        List<TruckOrderItemResponse> truckOrderItemResponseResponseList = records.stream().map(p -> {
            TruckOrderItemResponse response = new TruckOrderItemResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());


        List<Long> truckOrderIdList = records.stream().map(p -> p.getTruckOrderId()).distinct().collect(Collectors.toList());
        List<TruckOrder> truckOrderList = this.truckOrderService.listByIds(truckOrderIdList);
        for (TruckOrderItemResponse response : truckOrderItemResponseResponseList) {
            TruckOrder truckOrder = truckOrderList.stream().filter(p -> p.getId().equals(response.getTruckOrderId())).findFirst().orElse(null);
            if (truckOrder == null) {
                throw new Exception(MessageFormat.format("TruckOrder - {0} lost", response.getTruckOrderId()));
            }
            response.setTruckOrderCode(truckOrder.getTruckOrderCode());
        }

        PageData<TruckOrderItemResponse> pageData = new PageData<>();
        pageData.setData(truckOrderItemResponseResponseList);
        pageData.setCount(total);
        return pageData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeTruckOrder(List<Long> truckOrderIdList) throws Exception {
        truckOrderIdList = truckOrderIdList.stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(truckOrderIdList)) {
            throw new Exception("truckOrderIdList empty");
        }

        if (truckOrderIdList.size() < 2) {
            throw new Exception("truckOrderIdList size 1");
        }

        List<TruckOrder> truckOrderList = this.truckOrderService.listByIds(truckOrderIdList);
        Map<Long, TruckOrder> truckOrderMap = truckOrderList.stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
        for (Long truckOrderId : truckOrderIdList) {
            TruckOrder truckOrder = truckOrderMap.get(truckOrderId);
            if (truckOrder == null) {
                throw new Exception(MessageFormat.format("TruckOrder - {0} lost", truckOrderId));
            }
        }

        truckOrderList = truckOrderList.stream().sorted(Comparator.comparingLong(TruckOrder::getId)).collect(Collectors.toList());
        Long retainId = truckOrderList.get(0).getId();
        List<Long> deletedTruckOrderIdList = truckOrderIdList.stream().filter(p -> !p.equals(retainId)).collect(Collectors.toList());
        String mergeMsg = MessageFormat.format("retainId:{0},deletedTruckOrderIdList:{1}", retainId, deletedTruckOrderIdList.stream().map(Object::toString).collect(Collectors.joining(",")));
        log.info(mergeMsg);
        boolean re = this.truckOrderService.deleteByIds(deletedTruckOrderIdList);
        if (!re) {
            throw new Exception("Delete truckOrder fail");
        }
        TruckOrderItemRequest truckOrderItemRequest = new TruckOrderItemRequest();
        truckOrderItemRequest.setSearchCount(false);
        truckOrderItemRequest.setPageSize(Integer.MAX_VALUE);
        truckOrderItemRequest.setTruckOrderIdList(deletedTruckOrderIdList);
        PageData<TruckOrderItemResponse> pageData = this.getTruckOrderItemPage(truckOrderItemRequest);
        List<TruckOrderItemResponse> truckOrderItemResponseList = pageData.getData();
        for (TruckOrderItemResponse item : truckOrderItemResponseList) {
            log.info("{},{},{}", item.getId(), item.getTruckOrderId(), retainId);
        }
        List<Long> truckOrderItemIdList = truckOrderItemResponseList.stream().map(TruckOrderItemResponse::getId).collect(Collectors.toList());
        LambdaUpdateWrapper<TruckOrderItem> truckOrderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        truckOrderLambdaUpdateWrapper.in(TruckOrderItem::getId, truckOrderItemIdList).set(TruckOrderItem::getTruckOrderId, retainId);

        boolean re1 = this.update(null, truckOrderLambdaUpdateWrapper);
        if (!re1) {
            throw new Exception("Update TruckOrderItem truckOrderId fail");
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void auditFieldTest(Long id) {
        TruckOrderItem truckOrderItem = this.getById(id);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeStr = dateTimeFormatter.format(LocalDateTime.now());
        truckOrderItem.setRemark(timeStr);
        LambdaUpdateWrapper<TruckOrderItem> updateWrapper = new LambdaUpdateWrapper<TruckOrderItem>();
        updateWrapper.eq(TruckOrderItem::getId, truckOrderItem.getId());
        boolean re = this.update(truckOrderItem, updateWrapper);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void debit(MqMessage mqMessage) throws Exception {
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("debit MqMessage - {}", objectMapper.writeValueAsString(mqMessage));
        String lockKey = RedisKey.DEBIT;
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {

            //  return this.tryLock(waitTime, -1L, unit); 不指定释放时间，RedissonLock内部设置-1，
            //获取不到锁直接返回，下一个定时任务周期在处理

            lockSuccessfully = lock.tryLock();
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                log.info(msg);
                return;
            }
            log.info("update get lock {}", lockKey);
            MqMessage dbMessage = this.mqMessageService.getById(mqMessage.getId());
            if (dbMessage.getStatus().equals(MqMessageStatus.CONSUMED.getValue())) {
                log.info("Msg id {} has been consumed", mqMessage.getId());
                return;
            }

            long truckOrderItemId = objectMapper.readValue(mqMessage.getMsgContent(), Long.class);
            TruckOrderItem truckOrderItem = this.getById(truckOrderItemId);
            if (truckOrderItem == null) {
                String msg = MessageFormat.format("TruckOrderItem {0} doesn't exist", truckOrderItemId);
                throw new Exception(msg);
            }
            if (truckOrderItem.getStatus().equals(TruckOrderStausEnum.DEBITED.getValue())) {
                log.info("truckOrderItem - {} has been debited", truckOrderItem.getId());
                return;
            }
            ShipOrderPalletRequest shipOrderPalletRequest = new ShipOrderPalletRequest();
            shipOrderPalletRequest.setShipOrderCode(truckOrderItem.getShipOrderCode());
            List<InventoryItemDetailRequest> inventoryItemDetailRequestList = new ArrayList<>();
            InventoryItemDetailRequest detailRequest = new InventoryItemDetailRequest();
            detailRequest.setId(truckOrderItem.getInventoryItemDetailId());
            detailRequest.setPallet(truckOrderItem.getPallet());
            detailRequest.setMaterialCode(truckOrderItem.getMaterialCode());
            detailRequest.setUpdateMStr12(true);
            detailRequest.setM_Str7(truckOrderItem.getProjectNo());
            detailRequest.setM_Str12(truckOrderItem.getDeviceNo());
            detailRequest.setMovedPkgQuantity(truckOrderItem.getQuantity());
            inventoryItemDetailRequestList.add(detailRequest);
            shipOrderPalletRequest.setInventoryItemDetailDtoList(inventoryItemDetailRequestList);
            List<ShipOrderPalletRequest> shipOrderPalletRequestList = new ArrayList<>();
            shipOrderPalletRequestList.add(shipOrderPalletRequest);
            //未登录会得到全局异常
            String jsonParam = objectMapper.writeValueAsString(shipOrderPalletRequest);
            log.info("Before request WmsService subAssignPalletsByShipOrderBatch - json:{}", jsonParam);
            LoginUserTokenDto userTokenDto = UserInfoHolder.getUser(truckOrderItem.getCreatorId());
            if (userTokenDto == null) {
                throw new Exception("Get token fail.");
            }
//            //            String token="";
//            String token = "Bearer " + userTokenDto.getAccessToken();
//            WmsResponse wmsResponse = wmsService.subAssignPalletsByShipOrderBatch(shipOrderPalletRequestList, token);
//            String jsonResponse = objectMapper.writeValueAsString(wmsResponse);
//            log.info("After request WmsService subAssignPalletsByShipOrderBatch - json:{}", jsonResponse);
//            if (wmsResponse.getResult()) {
//                try {
//                    log.info("ThreadId - {}", Thread.currentThread().getId());
//                    EwmsEvent event = new EwmsEvent(this, "debit");
//                    event.setData(truckOrderItem.getTruckOrderId().toString());
//                    event.setMsgTopic(EwmsEventTopic.TRUCK_ORDER_COMPLETE);
//                    eventPublisher.publishEvent(event);
//                } catch (Exception ex) {
//                    log.error("Publish event error", ex);
//                }
//            } else {
//                throw new Exception(" WmsApiException - " + wmsResponse.getExplain());
//            }

//            int n=Integer.parseInt("n");
            truckOrderItem.setStatus(TruckOrderStausEnum.DEBITED.getValue());
            this.update(truckOrderItem);


            MqMessageRequest mqMessageRequest = new MqMessageRequest();
            String content = objectMapper.writeValueAsString(truckOrderItem.getTruckOrderId());
            mqMessageRequest.setBusinessId(truckOrderItem.getTruckOrderId());
            mqMessageRequest.setBusinessKey(UtilityConst.CHECK_TRUCK_ORDER_STATUS);
            mqMessageRequest.setMsgContent(content);
            mqMessageRequest.setQueue(UtilityConst.CHECK_TRUCK_ORDER_STATUS);
            mqMessageRequest.setTopic(UtilityConst.CHECK_TRUCK_ORDER_STATUS);
            mqMessageRequest.setSendMq(false);
            MqMessage updateTruckOrderStatusMsg = mqMessageService.addMessage(mqMessageRequest);
            publishMsg(Arrays.asList(updateTruckOrderStatusMsg));

            TruckOrderItemResponse response = new TruckOrderItemResponse();
            BeanUtils.copyProperties(truckOrderItem, response);
            String msg = objectMapper.writeValueAsString(response);
            String userId = userTokenDto.getId();
            //事务回调：事务同步，此处待处理， 所有事务提交了才会执行 事务回调
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCompletion(int status) {
                    sseEmitterService.sendMsgToClient(userId, msg);

                }
            });
            //


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
}




