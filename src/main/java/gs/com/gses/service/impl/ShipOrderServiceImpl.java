package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.request.wms.InventoryInfoRequest;
import gs.com.gses.model.request.wms.ShipOrderRequest;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.ShipOrderResponse;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.service.ShipOrderService;
import gs.com.gses.mapper.ShipOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ShipOrder】的数据库操作Service实现
 * @createDate 2024-08-11 10:23:06
 */
@Slf4j
@Service
public class ShipOrderServiceImpl extends ServiceImpl<ShipOrderMapper, ShipOrder> implements ShipOrderService {


    @Autowired
    private InventoryInfoServiceImpl inventoryInfoService;

    @Autowired
    private ShipOrderItemService shipOrderItemService;

    @Autowired
    private RedisTemplate redisTemplate;


    public static final String shipOrderPreparePercent = "ShipOrderPreparePercent";
    @Qualifier("objectMapper")
    @Autowired
    private ObjectMapper objectMapper;


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


}




