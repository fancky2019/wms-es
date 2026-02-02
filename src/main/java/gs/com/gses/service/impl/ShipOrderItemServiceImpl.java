package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.listener.event.EwmsEvent;
import gs.com.gses.listener.event.EwmsEventTopic;
import gs.com.gses.model.bo.wms.OutByAssignedInfoBo;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.enums.OutboundOrderXStatus;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.request.wms.MaterialRequest;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.request.wms.ShipOrderPalletRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InventoryItemDetailResponse;
import gs.com.gses.model.response.wms.MaterialResponse;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.service.MaterialService;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.mapper.wms.ShipOrderItemMapper;
import gs.com.gses.service.ShipOrderService;
import gs.com.gses.service.api.WmsService;
import gs.com.gses.utility.LambdaFunctionHelper;
import gs.com.gses.utility.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ShipOrderItem】的数据库操作Service实现
 * @createDate 2024-08-11 10:23:06
 */
@Slf4j
@Service
public class ShipOrderItemServiceImpl extends ServiceImpl<ShipOrderItemMapper, ShipOrderItem>
        implements ShipOrderItemService {

    @Autowired
    private MaterialService materialService;
    @Autowired
    private ShipOrderService shipOrderService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SnowFlake snowFlake;
    @Autowired
    private WmsService wmsService;

    @Override
    public List<ShipOrderItem> getByShipOrderIds(List<Long> shipOrderIdList) {
        List<ShipOrderItem> list = new ArrayList<>();
        if (shipOrderIdList.size() > 0) {
            LambdaQueryWrapper<ShipOrderItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ShipOrderItem::getShipOrderId, shipOrderIdList);
            list = this.list(queryWrapper);
        }
        return list;
    }

    @Override
    public Boolean checkItemExist(ShipOrderItemRequest request, List<ShipOrderItemResponse> matchedShipOrderItemResponseList) throws Exception {

        if (matchedShipOrderItemResponseList == null) {
            matchedShipOrderItemResponseList = new ArrayList<>();
        }
        if (StringUtils.isEmpty(request.getM_Str7())) {
            throw new Exception("m_Str7 is null");
        }
//        if (StringUtils.isEmpty(request.getM_Str12())) {
//            throw new Exception("m_Str12 is null");
//        }
        if (StringUtils.isEmpty(request.getMaterialCode())) {
            throw new Exception("materialCode is null");
        }

        List<Integer> xStatusList = new ArrayList<>();
        xStatusList.add(1);
        xStatusList.add(2);
        xStatusList.add(3);
        request.setXStatusList(xStatusList);


        request.setSearchCount(false);
        List<Sort> sortList = new ArrayList<>();
        Sort sort1 = new Sort();
        sort1.setSortField("id");
        sort1.setSortType("asc");
        sortList.add(sort1);
//        sort1 = new Sort();
//        sort1.setSortField("creationTime");
//        sort1.setSortType("asc");
//        sortList.add(sort1);
        request.setSortFieldList(sortList);

        PageData<ShipOrderItemResponse> page = getShipOrderItemPage(request);

        int size = page.getData().size();
        if (size == 0) {

//            throw new Exception("Can't get ShipOrderItem  by  m_Str7 ,m_Str12,materialCode");
            throw new Exception("发货单不存在");
        }

        if (StringUtils.isNotEmpty(request.getM_Str12())) {
            if (size > 1) {
//            throw new Exception("Get multiple  ShipOrderItem info by  m_Str7 ,m_Str12,materialCode");
                throw new Exception("找到多个发货单");
            }
//            log.info("M_Str12 is not empty set RequiredPkgQuantity one");
//            request.setRequiredPkgQuantity(BigDecimal.ONE);
        }
        BigDecimal requiredPkgQuantity = request.getRequiredPkgQuantity();
        List<ShipOrderItemResponse> shipOrderItemResponseList = page.getData();
        BigDecimal allRequiredPkgQuantity = shipOrderItemResponseList.stream()
                .map(ShipOrderItemResponse::getRequiredPkgQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (requiredPkgQuantity.compareTo(allRequiredPkgQuantity) > 0) {
            throw new Exception(MessageFormat.format("发货单待出库数量 - {0} 小于 当前要出库数量 {1}", allRequiredPkgQuantity, requiredPkgQuantity));
        }

        List<Long> shipOrderIdList = shipOrderItemResponseList.stream().map(ShipOrderItemResponse::getShipOrderId).distinct().collect(Collectors.toList());
        List<ShipOrder> shipOrderList = this.shipOrderService.listByIds(shipOrderIdList);
        BigDecimal leftRequiredPkgQuantity = request.getRequiredPkgQuantity();
        for (ShipOrderItemResponse shipOrderItemResponse : shipOrderItemResponseList) {
            if (leftRequiredPkgQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            ShipOrder shipOrder = shipOrderList.stream().filter(p -> p.getId().equals(shipOrderItemResponse.getShipOrderId()))
                    .findFirst()
                    .orElse(null);
            ;
            if (shipOrder == null) {
                String str = MessageFormat.format("ShipOrder - {0} lost", shipOrderItemResponse.getShipOrderId().toString());
                throw new Exception(str);
            }


            BigDecimal itemNeedPackageQuantity = shipOrderItemResponse.getRequiredPkgQuantity().subtract(shipOrderItemResponse.getPickedPkgQuantity());
            if (itemNeedPackageQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            shipOrderItemResponse.setTruckOrderItemRequestUuid(request.getTruckOrderItemRequestUuid());
            if (leftRequiredPkgQuantity.compareTo(itemNeedPackageQuantity) > 0) {
                shipOrderItemResponse.setCurrentAllocatedPkgQuantity(itemNeedPackageQuantity);
            } else {
                shipOrderItemResponse.setCurrentAllocatedPkgQuantity(leftRequiredPkgQuantity);
            }
            leftRequiredPkgQuantity = leftRequiredPkgQuantity.subtract(shipOrderItemResponse.getCurrentAllocatedPkgQuantity());
            shipOrderItemResponse.setApplyShipOrderCode(shipOrder.getApplyShipOrderCode());
            shipOrderItemResponse.setShipOrderCode(shipOrder.getXCode());
            matchedShipOrderItemResponseList.add(shipOrderItemResponse);
        }
        return true;
    }

    @Override
    public Boolean checkItemExistBatch(List<ShipOrderItemRequest> requestList, List<ShipOrderItemResponse> matchedShipOrderItemResponseList) throws Exception {

        if (matchedShipOrderItemResponseList == null) {
            matchedShipOrderItemResponseList = new ArrayList<>();
        }
        List<Integer> xStatusList = new ArrayList<>();
        xStatusList.add(1);
        xStatusList.add(2);
        xStatusList.add(3);

        LambdaQueryWrapper<ShipOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ShipOrderItem::getXStatus, xStatusList);
        wrapper.and(qw -> {
            for (ShipOrderItemRequest query : requestList) {
                qw.or(w -> {
                    w.eq(ShipOrderItem::getM_Str7, query.getM_Str7());
                    if (StringUtils.isNotEmpty(query.getM_Str12())) {
                        w.eq(ShipOrderItem::getM_Str12, query.getM_Str12());
                    }
                    if (query.getMaterialId() != null) {
                        w.eq(ShipOrderItem::getMaterialId, query.getMaterialId());
                    }
                });
            }
        });

        List<ShipOrderItem> shipOrderItemList = baseMapper.selectList(wrapper);
        shipOrderItemList = shipOrderItemList.stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shipOrderItemList)) {
            throw new Exception("Get ShipOrderItem fail");
        }
        List<Long> shipOrderIdList = shipOrderItemList.stream().map(ShipOrderItem::getShipOrderId).distinct().collect(Collectors.toList());
        List<ShipOrder> shipOrderList = this.shipOrderService.listByIds(shipOrderIdList);

        Map<Long, ShipOrder> shipOrderMap = shipOrderList.stream().collect(Collectors.toMap(p -> p.getId(), p -> p));

        for (ShipOrderItemRequest request : requestList) {
            log.info("MaterialId {} by ProjectNo {} MaterialCode {} DeviceNo {}", request.getMaterialId(), request.getM_Str7(), request.getMaterialCode(), request.getM_Str12());
            List<ShipOrderItem> currentShipOrderItemList = shipOrderItemList.stream().filter(p ->
                    request.getMaterialId().equals(p.getMaterialId()) && request.getM_Str7().equals(p.getM_Str7())
            ).collect(Collectors.toList());

            if (StringUtils.isNotEmpty(request.getM_Str12())) {
                log.info("Match DeviceNo {}", request.getM_Str12());
                currentShipOrderItemList = currentShipOrderItemList.stream().filter(p ->
                        request.getM_Str12().equals(p.getM_Str12())
                ).collect(Collectors.toList());
                currentShipOrderItemList = currentShipOrderItemList.stream().distinct().collect(Collectors.toList());
                int size = currentShipOrderItemList.size();
                if (size == 0) {
                    String msg = MessageFormat.format("Can't Match ShipOrderItems by ProjectNo {0} MaterialCode {1} DeviceNo {2}", request.getM_Str7(), request.getMaterialCode(), request.getM_Str12());
                    throw new Exception(msg);
                } else if (size > 1) {
                    List<String> shipOrderItemIdList = currentShipOrderItemList.stream().map(p -> p.getId().toString()).distinct().collect(Collectors.toList());
                    String idListStr = String.join(",", shipOrderItemIdList);
                    String msg = MessageFormat.format("Match multiple ShipOrderItems {0} by ProjectNo {1} MaterialCode {2} DeviceNo {3}", idListStr, request.getM_Str7(), request.getMaterialCode(), request.getM_Str12());
                    throw new Exception(msg);
                }
//                log.info("M_Str12 is not empty set RequiredPkgQuantity one");
//                request.setRequiredPkgQuantity(BigDecimal.ONE);
            }


            List<ShipOrderItemResponse> shipOrderItemResponseList = currentShipOrderItemList.stream().map(p -> {
                ShipOrderItemResponse response = new ShipOrderItemResponse();
                BeanUtils.copyProperties(p, response);
                return response;
            }).collect(Collectors.toList());


            BigDecimal requiredPkgQuantity = request.getRequiredPkgQuantity();
//            List<ShipOrderItemResponse> shipOrderItemResponseList = page.getData();
            BigDecimal allRequiredPkgQuantity = shipOrderItemResponseList.stream()
                    .map(ShipOrderItemResponse::getRequiredPkgQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (requiredPkgQuantity.compareTo(allRequiredPkgQuantity) > 0) {
                throw new Exception(MessageFormat.format("发货单待出库数量 - {0} 小于 当前要出库数量 {1}", allRequiredPkgQuantity, requiredPkgQuantity));
            }

//            List<Long> shipOrderIdList = shipOrderItemResponseList.stream().map(ShipOrderItemResponse::getShipOrderId).distinct().collect(Collectors.toList());
//            List<ShipOrder> shipOrderList = this.shipOrderService.listByIds(shipOrderIdList);
            BigDecimal leftRequiredPkgQuantity = request.getRequiredPkgQuantity();
            for (ShipOrderItemResponse shipOrderItemResponse : shipOrderItemResponseList) {
                if (leftRequiredPkgQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

//                ShipOrder shipOrder = shipOrderList.stream().filter(p -> p.getId().equals(shipOrderItemResponse.getShipOrderId()))
//                        .findFirst()
//                        .orElse(null);

                ShipOrder shipOrder = shipOrderMap.get(shipOrderItemResponse.getShipOrderId());
                if (shipOrder == null) {
                    String str = MessageFormat.format("ShipOrder - {0} lost", shipOrderItemResponse.getShipOrderId().toString());
                    throw new Exception(str);
                }


                BigDecimal itemNeedPackageQuantity = shipOrderItemResponse.getRequiredPkgQuantity().subtract(shipOrderItemResponse.getPickedPkgQuantity());
                if (itemNeedPackageQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                shipOrderItemResponse.setTruckOrderItemRequestUuid(request.getTruckOrderItemRequestUuid());
                if (leftRequiredPkgQuantity.compareTo(itemNeedPackageQuantity) > 0) {
                    shipOrderItemResponse.setCurrentAllocatedPkgQuantity(itemNeedPackageQuantity);
                } else {
                    shipOrderItemResponse.setCurrentAllocatedPkgQuantity(leftRequiredPkgQuantity);
                }
                leftRequiredPkgQuantity = leftRequiredPkgQuantity.subtract(shipOrderItemResponse.getCurrentAllocatedPkgQuantity());
                shipOrderItemResponse.setApplyShipOrderCode(shipOrder.getApplyShipOrderCode());
                shipOrderItemResponse.setShipOrderCode(shipOrder.getXCode());
                matchedShipOrderItemResponseList.add(shipOrderItemResponse);
            }
        }
        return true;

    }

    @Override
    public PageData<ShipOrderItemResponse> getShipOrderItemPage(ShipOrderItemRequest request) throws Exception {
        LambdaQueryWrapper<ShipOrderItem> queryWrapper = new LambdaQueryWrapper<>();
        log.info("getShipOrderItemPage - {}", objectMapper.writeValueAsString(request));
//        queryWrapper.eq(MqMessage::getStatus, 2);
        //排序
//        queryWrapper.orderByDesc(User::getAge)
//                .orderByAsc(User::getName);

        if (request.getId() != null && request.getId() > 0) {
            queryWrapper.eq(ShipOrderItem::getId, request.getId());
        }

        if (request.getShipOrderId() != null && request.getShipOrderId() > 0) {
            queryWrapper.eq(ShipOrderItem::getShipOrderId, request.getShipOrderId());
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {
            List<MaterialResponse> materialList = this.materialService.getByMatchedCode(request.getMaterialCode());
            if (CollectionUtils.isNotEmpty(materialList)) {
                List<Long> materialIdList = materialList.stream().map(p -> p.getId()).collect(Collectors.toList());
                queryWrapper.in(ShipOrderItem::getMaterialId, materialIdList);
            } else {
                return PageData.getDefault();
            }

        }
        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            queryWrapper.eq(ShipOrderItem::getMaterialId, request.getMaterialId());
        }
        if (StringUtils.isNotEmpty(request.getM_Str7())) {
            queryWrapper.eq(ShipOrderItem::getM_Str7, request.getM_Str7());
        }
        if (StringUtils.isNotEmpty(request.getM_Str12())) {
            queryWrapper.eq(ShipOrderItem::getM_Str12, request.getM_Str12());
        }


        if (CollectionUtils.isNotEmpty(request.getXStatusList())) {
            queryWrapper.in(ShipOrderItem::getXStatus, request.getXStatusList());
        }


//        QueryWrapper<ShipOrderItem> queryWrapper = new QueryWrapper<>();
//        if (StringUtils.isNotEmpty(request.getM_Str7())) {
//            queryWrapper.eq("M_Str7", request.getM_Str7());
//        }
//        if (StringUtils.isNotEmpty(request.getM_Str12())) {
//            queryWrapper.eq("M_Str12", request.getM_Str12());
//        }
//        if (StringUtils.isNotEmpty(request.getMaterialCode())) {
//            Material material = materialService.getByCode(request.getMaterialCode());
//            if (material != null) {
//                queryWrapper.eq("MaterialId", material.getId());
//
//            }
//        }

        // 创建分页对象 (当前页, 每页大小)
        Page<ShipOrderItem> page = new Page<>(request.getPageIndex(), request.getPageSize());

        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
//            // 多字段排序
////            queryWrapper.orderByAsc("age", "create_time")
////                    .orderByDesc("update_time");
//
//            for (Sort sort : request.getSortFieldList()) {
//
//                if (sort.getSortType().equals("desc")) {
////                    queryWrapper.orderByDesc(ShipOrder::getId);
//
//                    queryWrapper.orderByDesc(sort.getSortField());
//                } else {
//                    queryWrapper.orderByAsc(sort.getSortField());
//                }
////                orderByField(queryWrapper, sort.getSortField(), sort.getSortType().equals("asc"));
//            }


            // 多字段排序
//            queryWrapper.orderByAsc("age", "create_time")
//                    .orderByDesc("update_time");


            List<OrderItem> orderItems = LambdaFunctionHelper.getWithDynamicSort(request.getSortFieldList());
//            queryWrapper.orderBy(true, true, orderItems);
            // ROW_NUMBER() OVER (ORDER BY id ASC, creationTime ASC) as __row_number__
            page.setOrders(orderItems);
        }


        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<ShipOrderItem> shipOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取结果   // 当前页数据
        List<ShipOrderItem> records = shipOrderPage.getRecords();
        long total = shipOrderPage.getTotal();

        List<ShipOrderItemResponse> shipOrderItemResponseList = records.stream().map(p -> {
            ShipOrderItemResponse response = new ShipOrderItemResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());
        List<Long> shipOrderIdList = records.stream().map(p -> p.getShipOrderId()).distinct().collect(Collectors.toList());
        List<ShipOrder> shipOrderList = new ArrayList<>();
        Map<Long, ShipOrder> shipOrderMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(shipOrderIdList)) {
            shipOrderList = this.shipOrderService.listByIds(shipOrderIdList);
            shipOrderMap = shipOrderList.stream().collect(Collectors.toMap(ShipOrder::getId, p -> p));
        }

        List<Long> materialIdList = records.stream().map(p -> p.getMaterialId()).distinct().collect(Collectors.toList());
        List<Material> materialList = new ArrayList<>();
        Map<Long, Material> materialMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(shipOrderIdList)) {
            materialList = this.materialService.listByIds(materialIdList);
            materialMap = materialList.stream().collect(Collectors.toMap(Material::getId, p -> p));
        }

        for (ShipOrderItemResponse item : shipOrderItemResponseList) {
            ShipOrder shipOrder = shipOrderMap.get(item.getShipOrderId());
            if (shipOrder != null) {
                item.setShipOrderCode(shipOrder.getXCode());
            }
            Optional.ofNullable(materialMap.get(item.getMaterialId()))
                    .map(Material::getXCode)
                    .ifPresent(item::setMaterialCode);
        }
        PageData<ShipOrderItemResponse> pageData = new PageData<>();
        pageData.setData(shipOrderItemResponseList);
        pageData.setCount(total);
        return pageData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HashMap<Long, Long> copyShipOrderItem(long shipOrderId, long cloneShipOrderId) throws Exception {

        HashMap<Long, Long> cloneRelation = new HashMap<>();
        LambdaQueryWrapper<ShipOrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShipOrderItem::getShipOrderId, shipOrderId);
        List<ShipOrderItem> shipOrderItemList = this.list(queryWrapper);
        if (shipOrderItemList.isEmpty()) {
            return cloneRelation;
        }

        List<ShipOrderItem> cloneShipOrderItemList = new ArrayList<>();
        for (ShipOrderItem shipOrderItemDb : shipOrderItemList) {
            ShipOrderItem cloneShipOrderItem = new ShipOrderItem();
            BeanUtils.copyProperties(shipOrderItemDb, cloneShipOrderItem);
//            cloneShipOrderItem.setId(snowFlake.nextId());
            Long id = nextId(Arrays.asList(shipOrderItemDb.getId() + 3000)).get(0);
            cloneShipOrderItem.setId(id);
            cloneShipOrderItem.setShipOrderId(cloneShipOrderId);
            cloneShipOrderItem.setXStatus(OutboundOrderXStatus.NEW.getValue());
            cloneShipOrderItem.setAlloactedPkgQuantity(BigDecimal.ZERO);
            cloneShipOrderItem.setPickedPkgQuantity(BigDecimal.ZERO);
            this.save(cloneShipOrderItem);
            cloneRelation.put(shipOrderItemDb.getId(), cloneShipOrderItem.getId());
            cloneShipOrderItemList.add(cloneShipOrderItem);
        }
        return cloneRelation;
    }


    // 调用示例
//    applySort(queryWrapper, "createTime", "desc");  // 按createTime降序
    @Override
    public List<Long> nextId(List<Long> idList) throws Exception {
        List<ShipOrderItem> itemList = this.listByIds(idList);
        List<Long> result = new ArrayList<>();

        for (Long id : idList) {
            Optional<ShipOrderItem> opt = itemList.stream().filter(ShipOrderItem -> ShipOrderItem.getId().equals(id)).findFirst();
            if (!opt.isPresent()) {
                result.add(id);
            } else {
                result.add(snowFlake.nextId() / 1000);
            }

        }
        return result;
    }

    @Override
    public void outByAssignedInfo(OutByAssignedInfoBo outByAssignedInfoBo, String token) throws Exception {
        //判 字符串  null  ""  "   "
//        Assert.hasText(outByAssignedInfoBo.getRemark(), "remark id cannot be empty");
        for (InventoryItemDetailRequest detailRequest : outByAssignedInfoBo.getDetailRequest()) {
            Assert.notNull(detailRequest.getId(), "Detail id cannot be empty");
            Assert.notNull(detailRequest.getAllocatedPackageQuantity(), "AllocatedPackageQuantity cannot be empty");
            Assert.notNull(detailRequest.getMaterialCode(), "MaterialCode cannot be empty");
            Assert.notNull(detailRequest.getPallet(), "Pallet cannot be empty");
        }

        ShipOrderItem shipOrderItem = this.getById(outByAssignedInfoBo.getShipOrderItemId());
        Assert.notNull(shipOrderItem, MessageFormat.format("ShipOrderItem - {0}  loss", outByAssignedInfoBo.getShipOrderItemId()));
        ShipOrder shipOrder = this.shipOrderService.getById(shipOrderItem.getShipOrderId());
        Assert.notNull(shipOrderItem, MessageFormat.format("ShipOrder - {0}  loss", shipOrderItem.getShipOrderId()));

        BigDecimal needQuantity = shipOrderItem.getRequiredPkgQuantity().subtract(shipOrderItem.getAlloactedPkgQuantity());
        BigDecimal outQuantity = outByAssignedInfoBo.getDetailRequest().stream()
                .map(InventoryItemDetailRequest::getAllocatedPackageQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        checkPackageQuantity(needQuantity, outQuantity);

        List<ShipOrderPalletRequest> shipOrderPalletRequestList = new ArrayList<>();
        ShipOrderPalletRequest shipOrderPalletRequest = null;
        List<InventoryItemDetailRequest> inventoryItemDetailRequestList = null;
        InventoryItemDetailRequest detailRequest = null;
        for (InventoryItemDetailRequest item : outByAssignedInfoBo.getDetailRequest()) {
            shipOrderPalletRequest = new ShipOrderPalletRequest();
            shipOrderPalletRequest.setShipOrderCode(shipOrder.getXCode());
            shipOrderPalletRequest.setShipOrderItemId(outByAssignedInfoBo.getShipOrderItemId());
            inventoryItemDetailRequestList = new ArrayList<>();
            detailRequest = new InventoryItemDetailRequest();
            detailRequest.setPallet(item.getPallet());
            detailRequest.setM_Str7(item.getM_Str7());
            detailRequest.setM_Str12(item.getM_Str12());
            detailRequest.setMovedPkgQuantity(item.getAllocatedPackageQuantity());
            detailRequest.setId(item.getId());
            detailRequest.setMaterialCode(item.getMaterialCode());
            inventoryItemDetailRequestList.add(detailRequest);

            shipOrderPalletRequest.setInventoryItemDetailDtoList(inventoryItemDetailRequestList);
            shipOrderPalletRequestList.add(shipOrderPalletRequest);
        }

//        Integer.parseInt("m");
        String jsonParam = objectMapper.writeValueAsString(shipOrderPalletRequestList);
        log.info("OutByAssignedInfo Before request WmsService subAssignPalletsByShipOrderBatch - json:{}", jsonParam);
        WmsResponse wmsResponse = wmsService.subAssignPalletsByShipOrderBatch(shipOrderPalletRequestList, token);
        String jsonResponse = objectMapper.writeValueAsString(wmsResponse);
        log.info("OutByAssignedInfo After request WmsService subAssignPalletsByShipOrderBatch - json:{}", jsonResponse);
        if (wmsResponse.getResult()) {

        } else {
            throw new Exception(" WmsApiException - " + wmsResponse.getExplain());
        }
    }

    private void checkPackageQuantity(BigDecimal needQuantity, BigDecimal outQuantity) throws Exception {
        if (needQuantity.compareTo(outQuantity) < 0) {
            DecimalFormat df = new DecimalFormat("#0.00");
            String needQuantityFormatted = df.format(needQuantity);
            String outQuantityFormatted = df.format(outQuantity);
            String msg = MessageFormat.format("The quantity shipped out {0} exceeds the quantity required {1} .", outQuantityFormatted, needQuantityFormatted);
            throw new Exception(msg);
        }
    }

}




