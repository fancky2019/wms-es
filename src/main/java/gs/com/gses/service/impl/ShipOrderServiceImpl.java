package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.flink.DataChangeInfo;
import gs.com.gses.mapper.ShipOrderMapper;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.BillType;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.entity.ShipOrderItem;
import gs.com.gses.model.enums.OutboundOrderXStatus;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.InventoryInfoRequest;
import gs.com.gses.model.request.wms.ShipOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.ShipOrderResponse;
import gs.com.gses.model.utility.RedisKey;
import gs.com.gses.service.BillTypeService;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.service.ShipOrderService;
import gs.com.gses.service.WaveShipOrderItemRelationService;
import gs.com.gses.utility.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ShipOrder】的数据库操作Service实现
 * @createDate 2024-08-11 10:23:06
 */
@Slf4j
@Service
public class ShipOrderServiceImpl extends ServiceImpl<ShipOrderMapper, ShipOrder> implements ShipOrderService {


    @Value("${sbp.enableCopyShipOrder:false}") // 默认 false，避免报错
    private boolean enableCopyShipOrder;
    @Autowired
    @Lazy  // 防止循环依赖
    private ShipOrderService selfProxy;
    @Autowired
    private InventoryInfoServiceImpl inventoryInfoService;

    @Autowired
    private ShipOrderItemService shipOrderItemService;

    @Autowired
    private BillTypeService billTypeService;

    @Autowired
    private WaveShipOrderItemRelationService waveShipOrderItemRelationService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SnowFlake snowFlake;


    public static final String shipOrderPreparePercent = "ShipOrderPreparePercent";
    @Qualifier("objectMapper")
    @Autowired
    private ObjectMapper objectMapper;
    @Qualifier("upperObjectMapper")  // 明确指定名称
    @Autowired
    private ObjectMapper upperObjectMapper;
    @Autowired
    private RedissonClient redissonClient;


    @Override
    public ShipOrder test(Long id) {
        ShipOrder shipOrder = this.getById(id);
        return shipOrder;
    }

    @Override
    public PageData<ShipOrderResponse> getShipOrderPage(ShipOrderRequest request) {
//        LambdaQueryWrapper<ShipOrder> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(MqMessage::getStatus, 2);
        //排序
//        queryWrapper.orderByDesc(User::getAge)
//                .orderByAsc(User::getName);


        QueryWrapper<ShipOrder> queryWrapper = new QueryWrapper<>();
        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
            // 多字段排序
//            queryWrapper.orderByAsc("age", "create_time")
//                    .orderByDesc("update_time");

            for (Sort sort : request.getSortFieldList()) {
                if (sort.getSortType().equals("desc")) {
//                    queryWrapper.orderByDesc(ShipOrder::getId);
                    queryWrapper.orderByDesc(sort.getSortField());
                } else {
                    queryWrapper.orderByAsc(sort.getSortField());
                }
//                orderByField(queryWrapper, sort.getSortField(), sort.getSortType().equals("asc"));
            }
        }

        if (CollectionUtils.isNotEmpty(request.getShipOrderIdList())) {
            queryWrapper.in("Id", request.getShipOrderIdList());
        }
        if (StringUtils.isNotEmpty(request.getXcode())) {
            queryWrapper.like("XCode", request.getXcode());

            // truckOrderItemQueryWrapper.like(TruckOrderItem::getCreatorName, request.getCreatorName());
        }
        // 创建分页对象 (当前页, 每页大小)
        Page<ShipOrder> page = new Page<>(request.getPageIndex(), request.getPageSize());
        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<ShipOrder> shipOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取结果   // 当前页数据
        List<ShipOrder> records = shipOrderPage.getRecords();
        long total = shipOrderPage.getTotal();

        List<ShipOrderResponse> shipOrderResponseList = records.stream().map(p -> {
            ShipOrderResponse response = new ShipOrderResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ShipOrderResponse> pageData = new PageData<>();
        pageData.setData(shipOrderResponseList);
        pageData.setCount(total);
        return pageData;
    }

    public <T> LambdaQueryWrapper<T> orderByField(LambdaQueryWrapper<T> wrapper, String fieldName, boolean isAsc) {
        // 获取实体类所有字段的SFunction缓存
//        Map<String, SFunction<T, R>> fieldMap = LambdaUtils.getFieldMap(entityClass);
//         fieldMap.get(fieldName);
        try {
//            Method method = LambdaQueryWrapper.class.getMethod(
//                    isAsc ? "orderByAsc" : "orderByDesc",
//                    SFunction.class );

            // 获取实体类字段对应的getter方法
            String getter = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            // 创建方法引用
            SFunction<T, ?> function = entity -> {
                try {
                    return entity.getClass().getMethod(getter).invoke(entity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            if (isAsc) {
                wrapper.orderByAsc(function);
            } else {
                wrapper.orderByDesc(function);
            }

        } catch (Exception e) {
            throw new RuntimeException("排序字段不存在", e);
        }

        return wrapper;
    }


    @Override
    public List<ShipOrderResponse> getShipOrderList(ShipOrderRequest request) {
        LambdaQueryWrapper<ShipOrder> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(MqMessage::getStatus, 2);
        //排序
//        queryWrapper.orderByDesc(User::getAge)
//                .orderByAsc(User::getName);
        //避免排序

        queryWrapper.orderByDesc(ShipOrder::getId);
        List<ShipOrder> mqMessageList = this.list(queryWrapper);
        List<ShipOrderResponse> shipOrderResponseList = mqMessageList.stream().map(p -> {
            ShipOrderResponse response = new ShipOrderResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());
        return shipOrderResponseList;
    }

    @Override
    public void allocate() throws Exception {
        StopWatch stopWatch = new StopWatch("allocate");
        stopWatch.start("allocate");

        ShipOrderRequest request = new ShipOrderRequest();
        request.setPageIndex(1);
        request.setPageSize(10);
        List<Sort> sorts = new ArrayList<>();
        Sort sort = new Sort();
        sort.setSortField("XStatus");
        sort.setSortType("asc");
        sorts.add(sort);

        Sort sortId = new Sort();
        sortId.setSortField("id");
        sortId.setSortType("desc");
        sorts.add(sortId);
        request.setSortFieldList(sorts);

        PageData<ShipOrderResponse> pageData = getShipOrderPage(request);

        List<ShipOrderResponse> list = pageData.getData();
        list = list.stream().filter(p -> p.getXStatus() == 1).collect(Collectors.toList());

//        list = list.stream().filter(p -> p.getId().equals(674145933246534L)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(list)) {
            log.info("ShipOrder data is empty");
            return;
        }

        List<String> shipOrderCodeList = list.stream().map(p -> p.getXCode()).distinct().collect(Collectors.toList());
        log.info("shipOrderCode:{}", String.join(",", shipOrderCodeList));

        List<Long> shipOrderIdList = list.stream().map(p -> p.getId()).distinct().collect(Collectors.toList());
        List<ShipOrderItem> shipOrderItemList = shipOrderItemService.getByShipOrderIds(shipOrderIdList);
//        Map<Integer, ShipOrderItem> shipOrderItemMap = list.stream().collect(Collectors.toMap(ShipOrderItem::getId, item -> item));
        Map<Long, List<ShipOrderItem>> shipOrderItemMap = shipOrderItemList.stream().collect(Collectors.groupingBy(ShipOrderItem::getShipOrderId));
        HashMap<String, String> shipOrderPreparePercentMap = new HashMap<>();
        for (ShipOrderResponse shipOrderResponse : list) {

            List<ShipOrderItem> currentShipOrderItemList = shipOrderItemMap.get(shipOrderResponse.getId());
            List<Long> materialIdList = currentShipOrderItemList.stream().map(p -> p.getMaterialId()).distinct().collect(Collectors.toList());
            InventoryInfoRequest inventoryInfoRequest = new InventoryInfoRequest();
            inventoryInfoRequest.setMaterialIdList(materialIdList);
            HashMap<Long, List<InventoryInfo>> materialInventoryInfoMap = this.inventoryInfoService.getDefaultAllocatedInventoryInfoList(inventoryInfoRequest);
            HashMap<Long, BigDecimal> allocatedPackageQuantityMap = new HashMap<>();
            HashMap<Long, Integer> shipOrderItemInventoryMap = new HashMap<>();
            for (ShipOrderItem item : currentShipOrderItemList) {
                List<InventoryInfo> materialInventoryInfoList = materialInventoryInfoMap.get(item.getMaterialId());
                if (CollectionUtils.isEmpty(materialInventoryInfoList)) {
                    log.info("materialId:{} can't get available inventory", item.getMaterialId());
                    continue;
                }

                Map<Long, BigDecimal> materialPackQuantity = materialInventoryInfoList.stream().collect(Collectors.groupingBy(InventoryInfo::getMaterialId, Collectors.reducing(BigDecimal.ZERO, InventoryInfo::getPackageQuantity, BigDecimal::add)));
                Long materialId = item.getMaterialId();
                //当前分配数量
                BigDecimal currentAllocatedPackageQuantity = BigDecimal.ZERO;
                //库存总数量
                BigDecimal inventoryPackageQuantity = materialPackQuantity.get(materialId);
                if (inventoryPackageQuantity == null) {
                    inventoryPackageQuantity = BigDecimal.ZERO;
                }
                //已分配数量
                BigDecimal allocatedPackageQuantity = BigDecimal.ZERO;
                //是否分配过
                if (allocatedPackageQuantityMap.containsKey(materialId)) {
                    allocatedPackageQuantity = allocatedPackageQuantityMap.get(item.getMaterialId());
                    BigDecimal leftPackageQuantity = inventoryPackageQuantity.subtract(allocatedPackageQuantity);
                    //还有剩余数量
                    if (leftPackageQuantity.compareTo(BigDecimal.ZERO) >= 0) {
                        //剩余数量大于需求数量
                        if (leftPackageQuantity.compareTo(item.getRequiredPkgQuantity()) >= 0) {
                            currentAllocatedPackageQuantity = item.getRequiredPkgQuantity();
                        } else {
                            currentAllocatedPackageQuantity = inventoryPackageQuantity;
                        }
                    }
                } else {
                    if (inventoryPackageQuantity.compareTo(item.getRequiredPkgQuantity()) >= 0) {
                        currentAllocatedPackageQuantity = item.getRequiredPkgQuantity();
                    } else {
                        currentAllocatedPackageQuantity = inventoryPackageQuantity;
                    }
                }
                boolean enough = currentAllocatedPackageQuantity.compareTo(item.getRequiredPkgQuantity()) >= 0;
                int zeroOne = enough ? 1 : 0;
                shipOrderItemInventoryMap.put(item.getId(), zeroOne);
                BigDecimal materialAllocatedPackageQuantity = allocatedPackageQuantity.add(currentAllocatedPackageQuantity);
                allocatedPackageQuantityMap.put(item.getMaterialId(), materialAllocatedPackageQuantity);
            }
            long total = shipOrderItemInventoryMap.keySet().size();
            long enoughCount = shipOrderItemInventoryMap.values().stream().filter(p -> p.compareTo(0) > 0).count();

            double result = (double) enoughCount / total;
            DecimalFormat df = new DecimalFormat("0.0000");  // 四位小数
            String formatted = df.format(result);
            shipOrderPreparePercentMap.put(shipOrderResponse.getId().toString(), formatted);
            int m = 0;
        }

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        redisTemplate.delete(shipOrderPreparePercent);
        hashOps.putAll(shipOrderPreparePercent, shipOrderPreparePercentMap);

        stopWatch.stop();
//        stopWatch.start("BatchInsert_Trace2");
        long mills = stopWatch.getTotalTimeMillis();
        log.info("allocate complete {} ms", mills);
    }

    //待优化
    @Override
    public HashMap<String, String> allocateDesignatedShipOrders(ShipOrderRequest request) throws Exception {

//        Material material = (Material)null;

        StopWatch stopWatch = new StopWatch("allocateDesignatedShipOrders");
        stopWatch.start("allocateDesignatedShipOrders");
        HashMap<String, String> shipOrderPreparePercentMap = new HashMap<>();
//        ShipOrderRequest request = new ShipOrderRequest();
        request.setSearchCount(false);
//        request.setShipOrderIdList(shipOrderIdList);

        PageData<ShipOrderResponse> pageData = getShipOrderPage(request);

        List<ShipOrderResponse> list = pageData.getData();
        //  list = list.stream().filter(p -> p.getXStatus() == 1).collect(Collectors.toList());

//        list = list.stream().filter(p -> p.getId().equals(674145933246534L)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(list)) {
            log.info("ShipOrder data is empty");
            return shipOrderPreparePercentMap;
        }

        List<String> shipOrderCodeList = list.stream().map(p -> p.getXCode()).distinct().collect(Collectors.toList());
        log.info("shipOrderCode:{}", String.join(",", shipOrderCodeList));

        List<ShipOrderItem> shipOrderItemList = shipOrderItemService.getByShipOrderIds(request.getShipOrderIdList());
//        Map<Integer, ShipOrderItem> shipOrderItemMap = list.stream().collect(Collectors.toMap(ShipOrderItem::getId, item -> item));
        Map<Long, List<ShipOrderItem>> shipOrderItemMap = shipOrderItemList.stream().collect(Collectors.groupingBy(ShipOrderItem::getShipOrderId));

        for (ShipOrderResponse shipOrderResponse : list) {

            List<ShipOrderItem> currentShipOrderItemList = shipOrderItemMap.get(shipOrderResponse.getId());
            if (CollectionUtils.isEmpty(currentShipOrderItemList)) {
                shipOrderPreparePercentMap.put(shipOrderResponse.getId().toString(), "0.00");
                continue;
            }

            List<Long> materialIdList = currentShipOrderItemList.stream().map(p -> p.getMaterialId()).distinct().collect(Collectors.toList());
            InventoryInfoRequest inventoryInfoRequest = new InventoryInfoRequest();
            inventoryInfoRequest.setMaterialIdList(materialIdList);
            HashMap<Long, List<InventoryInfo>> materialInventoryInfoMap = this.inventoryInfoService.getDefaultAllocatedInventoryInfoList(inventoryInfoRequest);
            HashMap<Long, BigDecimal> allocatedPackageQuantityMap = new HashMap<>();
            HashMap<Long, Integer> shipOrderItemInventoryMap = new HashMap<>();
            for (ShipOrderItem item : currentShipOrderItemList) {
                //520334173728837L
                if (item.getMaterialId().equals(674167812272197L)) {
                    int m = 0;
                }
                //
                BigDecimal itemNeedAllocatedPackageQuantity = item.getRequiredPkgQuantity().subtract(item.getAlloactedPkgQuantity());

                List<InventoryInfo> materialInventoryInfoList = materialInventoryInfoMap.get(item.getMaterialId());
                if (CollectionUtils.isEmpty(materialInventoryInfoList)) {
                    log.info("materialId:{} can't get available inventory", item.getMaterialId());
                    int zeroOne = itemNeedAllocatedPackageQuantity.compareTo(BigDecimal.ZERO) > 0 ? 0 : 1;
                    shipOrderItemInventoryMap.put(item.getId(), zeroOne);
                    continue;
                }

                Map<Long, BigDecimal> materialPackQuantity = materialInventoryInfoList.stream().collect(Collectors.groupingBy(InventoryInfo::getMaterialId, Collectors.reducing(BigDecimal.ZERO, InventoryInfo::getPackageQuantity, BigDecimal::add)));
                Long materialId = item.getMaterialId();
                //当前分配数量
                BigDecimal currentAllocatedPackageQuantity = BigDecimal.ZERO;
                //库存总数量
                BigDecimal inventoryPackageQuantity = materialPackQuantity.get(materialId);
                if (inventoryPackageQuantity == null) {
                    inventoryPackageQuantity = BigDecimal.ZERO;
                }
                //已分配数量
                BigDecimal allocatedPackageQuantity = BigDecimal.ZERO;
                //是否分配过
                if (allocatedPackageQuantityMap.containsKey(materialId)) {
                    allocatedPackageQuantity = allocatedPackageQuantityMap.get(item.getMaterialId());
                    BigDecimal leftPackageQuantity = inventoryPackageQuantity.subtract(allocatedPackageQuantity);
                    //还有剩余数量
                    if (leftPackageQuantity.compareTo(BigDecimal.ZERO) >= 0) {
                        //剩余数量大于需求数量
                        if (leftPackageQuantity.compareTo(itemNeedAllocatedPackageQuantity) >= 0) {
                            currentAllocatedPackageQuantity = itemNeedAllocatedPackageQuantity;
                        } else {
                            currentAllocatedPackageQuantity = inventoryPackageQuantity;
                        }
                    }
                } else {
                    if (inventoryPackageQuantity.compareTo(itemNeedAllocatedPackageQuantity) >= 0) {
                        currentAllocatedPackageQuantity = itemNeedAllocatedPackageQuantity;
                    } else {
                        currentAllocatedPackageQuantity = inventoryPackageQuantity;
                    }
                }
                boolean enough = currentAllocatedPackageQuantity.compareTo(itemNeedAllocatedPackageQuantity) >= 0;
                int zeroOne = enough ? 1 : 0;
                shipOrderItemInventoryMap.put(item.getId(), zeroOne);
                BigDecimal materialAllocatedPackageQuantity = allocatedPackageQuantity.add(currentAllocatedPackageQuantity);
                allocatedPackageQuantityMap.put(item.getMaterialId(), materialAllocatedPackageQuantity);
            }
            log.info("shipOrderItemInventoryMap - {} ", objectMapper.writeValueAsString(shipOrderItemInventoryMap));

            long total = shipOrderItemInventoryMap.keySet().size();
            long enoughCount = shipOrderItemInventoryMap.values().stream().filter(p -> p.compareTo(0) > 0).count();

            double result = (double) enoughCount / total;
            result *= 100;
            //两位小数
            DecimalFormat df = new DecimalFormat("0.00");
            String formatted = df.format(result);
            shipOrderPreparePercentMap.put(shipOrderResponse.getId().toString(), formatted);
            int m = 0;
        }

        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        redisTemplate.delete(shipOrderPreparePercent);
        hashOps.putAll(shipOrderPreparePercent, shipOrderPreparePercentMap);
        stopWatch.stop();
//        stopWatch.start("BatchInsert_Trace2");
        long mills = stopWatch.getTotalTimeMillis();
        log.info("allocate complete {} ms", mills);

        return shipOrderPreparePercentMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyShipOrder(long shipOrderId) throws Exception {

        ShipOrder shipOrderDb = this.getById(shipOrderId);
        if (shipOrderDb == null) {
            throw new Exception("shipOrder - {shipOrderId} doesn't exist");
        }

        LambdaQueryWrapper<ShipOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShipOrder::getXCode, shipOrderDb.getXCode() + "_理货");
        List<ShipOrder> clonedShipOrderList = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(clonedShipOrderList)) {
            log.info("{}_理货,has been copyed", shipOrderDb.getXCode());
            return;
        }
//        if (shipOrderDb.getXStatus() != OutboundOrderXStatus.NEW.getValue()) {
//            String msg = MessageFormat.format("shipOrder - {0} XStatus is not new", shipOrderId);
//            throw new Exception(msg);
//        }

        BillType billType = billTypeService.getByCode("OutWarehouse");
        ShipOrder shipOrder = new ShipOrder();
        BeanUtils.copyProperties(shipOrderDb, shipOrder);
        long id1 = snowFlake.nextId() / 1000;
        Long id = nextId(Arrays.asList(shipOrderDb.getId() + 1)).get(0);
        shipOrder.setId(id);
        shipOrder.setBillTypeId(billType.getId());
        shipOrder.setXStatus(OutboundOrderXStatus.NEW.getValue());
        shipOrder.setAlloactedPkgQuantity(BigDecimal.ZERO);
        shipOrder.setPickedPkgQuantity(BigDecimal.ZERO);

        LambdaUpdateWrapper<ShipOrder> shipOrderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        shipOrderLambdaUpdateWrapper.set(ShipOrder::getXCode, shipOrderDb.getXCode() + "_理货");
        if (StringUtils.isNotEmpty(shipOrderDb.getApplyShipOrderCode())) {
            shipOrderLambdaUpdateWrapper.set(ShipOrder::getApplyShipOrderCode, shipOrderDb.getApplyShipOrderCode() + "_理货");
        }
        shipOrderLambdaUpdateWrapper.eq(ShipOrder::getId, shipOrderDb.getId());
        this.update(shipOrderLambdaUpdateWrapper);
        this.save(shipOrder);

        HashMap<Long, Long> cloneRelation = this.shipOrderItemService.copyShipOrderItem(shipOrderId, shipOrder.getId());
        waveShipOrderItemRelationService.bindingNewRelation(cloneRelation);

    }

    @Override
    public void sink(DataChangeInfo dataChangeInfo) throws Exception {

        if (!enableCopyShipOrder) {
            return;
        }
        String lockKey = RedisKey.UPDATE_INVENTORY_INFO;// "redisson:updateInventoryInfo:" + id;
        //获取分布式锁，此处单体应用可用 synchronized，分布式就用redisson 锁
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {
            //boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException
            lockSuccessfully = lock.tryLock(RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME, RedisKey.INIT_INVENTORY_INFO_FROM_DB_LEASE_TIME, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                String msg = MessageFormat.format("Get lock {0} fail，wait time : {1} s", lockKey, RedisKey.INIT_INVENTORY_INFO_FROM_DB_WAIT_TIME);
                throw new Exception(msg);
            }
            long startChangeTime = dataChangeInfo.getChangeTime();
            log.info("start sink - {}", dataChangeInfo.getId());
            if (StringUtils.isEmpty(dataChangeInfo.getAfterData()) || "READ".equals(dataChangeInfo.getEventType())) {
                log.info("read - {}", dataChangeInfo.getId());
                return;
            }


            ShipOrder shipOrder = upperObjectMapper.readValue(dataChangeInfo.getAfterData(), ShipOrder.class);
            if (shipOrder.getId() == null) {
                log.info("changedInventoryItemDetail {} id is null ,dataChangeInfo.getEventType - {}, BeforeData {},AfterData {}", dataChangeInfo.getId(), dataChangeInfo.getEventType(), dataChangeInfo.getBeforeData(), dataChangeInfo.getAfterData());
                shipOrder = upperObjectMapper.readValue(dataChangeInfo.getBeforeData(), ShipOrder.class);
            }


            switch (dataChangeInfo.getEventType()) {
                case "CREATE":
                    break;
                case "UPDATE":
//                    AJ02 AJ05
                    if (shipOrder.getXStatus().equals(OutboundOrderXStatus.COMPLETED.getValue())) {
                        if (shipOrder.getApplyShipOrderCode().contains("_理货")) {
                            return;
                        }
                        String mqMsgIdKey = RedisKey.SHIP_ORDER_COMPLETE + shipOrder.getId();
                        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                        //添加重复消费redis 校验，不会存在并发同一个message
                        Boolean hasKey = redisTemplate.hasKey(mqMsgIdKey);
                        boolean exist = hasKey != null && hasKey;
//                        exist = false;
                        if (!exist) {
                            //设置key并制定过期时间
                            valueOperations.set(mqMsgIdKey, shipOrder.getId(), 1, TimeUnit.DAYS);

//                            Object proxyObj = AopContext.currentProxy();
//                            ShipOrderService shipOrderService = null;
//                            if (proxyObj instanceof ShipOrderService) {
//                                shipOrderService = (ShipOrderService) proxyObj;
//                                shipOrderService.copyShipOrder(shipOrder.getId());
//                            }

                            selfProxy.copyShipOrder(shipOrder.getId());
                        }
                    }

                    break;
                case "DELETE":
                    break;
                case "READ":
                    break;
                default:
                    break;
            }


            long sinkCompletedTime = System.currentTimeMillis();
            long sinkCostTime = sinkCompletedTime - startChangeTime;
            log.info("Sink {} completed sinkCostTime {}", dataChangeInfo.getId(), sinkCostTime);
        } catch (Exception ex) {
            log.error("Sink {} exception ,dataChangeInfo.getEventType - {}, BeforeData {},AfterData {}", dataChangeInfo.getId(), dataChangeInfo.getEventType(), dataChangeInfo.getBeforeData(), dataChangeInfo.getAfterData());
            //待优化处理
            log.error("", ex);
            throw ex;
        } finally {
            if (lockSuccessfully) {
                try {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                } catch (Exception e) {
                    log.warn("Redis check lock ownership failed: ", e);
                }
            }
        }

    }

    @Override
    public List<Long> nextId(List<Long> idList) throws Exception {
        List<ShipOrder> shipOrderList = this.listByIds(idList);
        List<Long> result = new ArrayList<>();
        List<Long> existList = new ArrayList<>();
        for (Long id : idList) {
            Optional<ShipOrder> opt = shipOrderList.stream().filter(shipOrder -> shipOrder.getId().equals(id)).findFirst();
            if (!opt.isPresent()) {
                result.add(id);
            } else {
                result.add(snowFlake.nextId() / 1000);
            }

        }
        return result;
    }
}




