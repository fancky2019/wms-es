package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.bo.wms.AllocateModel;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.mqtt.PrintWrapper;
import gs.com.gses.model.response.mqtt.TrunkOrderBarCode;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.rabbitMQ.mqtt.MqttProduce;
import gs.com.gses.rabbitMQ.mqtt.Topics;
import gs.com.gses.service.*;
import gs.com.gses.mapper.TruckOrderItemMapper;
import gs.com.gses.utility.LambdaFunctionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【TruckOrderItem】的数据库操作Service实现
 * @createDate 2025-05-28 13:18:54
 */
@Slf4j
@Service
public class TruckOrderItemServiceImpl extends ServiceImpl<TruckOrderItemMapper, TruckOrderItem>
        implements TruckOrderItemService {

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

    @Override
    public Boolean addBatch(List<TruckOrderItemRequest> requestList) {
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

        return true;
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
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory()
                .openSession(ExecutorType.BATCH);
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
        truckOrderLambdaUpdateWrapper.in(TruckOrderItem::getId, truckOrderItemIdList)
                .set(TruckOrderItem::getTruckOrderId, retainId);

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
}




