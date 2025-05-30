package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.TruckOrder;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.*;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.mqtt.TrunkOderMq;
import gs.com.gses.model.response.wms.ShipPickOrderResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import gs.com.gses.model.response.wms.WmsResponse;
import gs.com.gses.rabbitMQ.mqtt.MqttProduce;
import gs.com.gses.service.ShipPickOrderService;
import gs.com.gses.service.TruckOrderItemService;
import gs.com.gses.service.TruckOrderService;
import gs.com.gses.mapper.TruckOrderMapper;
import gs.com.gses.service.api.WmsService;
import gs.com.gses.utility.LambdaFunctionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.config.SortHandlerMethodArgumentResolverCustomizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【TruckOrder】的数据库操作Service实现
 * @createDate 2025-05-28 13:18:54
 */
@Slf4j
@Service
public class TruckOrderServiceImpl extends ServiceImpl<TruckOrderMapper, TruckOrder>
        implements TruckOrderService {

    @Autowired
    private TruckOrderItemService truckOrderItemService;

    @Autowired
    private WmsService wmsService;
    @Autowired
    private ShipPickOrderService shipPickOrderService;
    @Autowired
    private MqttProduce mqttProduce;
    @Autowired
    private ObjectMapper upperObjectMapper;


    private final static String TRUCK_ORDER_TOPIC = "GS/WMS/TruckOrder/Complete";


    //    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addTruckOrderAndItem(AddTruckOrderRequest request, String token) throws Throwable {
        List<ShipOrderPalletRequest> shipOrderPalletRequestList = new ArrayList<>();
        HashSet<String> shipOrderCodeSet = new HashSet<>();
        for (TruckOrderItemRequest itemRequest : request.getTruckOrderItemRequestList()) {
            Boolean result = truckOrderItemService.checkAvailable(itemRequest);
            if (!result) {
                String str = MessageFormat.format("CheckFail : 项目号 - {0} 设备号 - {1} 物料 - {2} 校验失败.",
                        itemRequest.getProjectNo()
                        , itemRequest.getDeviceNo()
                        , itemRequest.getMaterialCode());
                throw new Exception(str);
            }
            shipOrderCodeSet.add(itemRequest.getShipOrderCode());
        }

        for (String shipOrderCode : shipOrderCodeSet) {
            ShipOrderPalletRequest shipOrderPalletRequest = new ShipOrderPalletRequest();

            List<TruckOrderItemRequest> shipOrderTruckOrderItemmList = request.getTruckOrderItemRequestList().stream().filter(p -> p.getShipOrderCode().equals(shipOrderCode)).collect(Collectors.toList());
            List<InventoryItemDetailRequest> inventoryItemDetailRequestList = shipOrderTruckOrderItemmList.stream().map(p ->
            {
                InventoryItemDetailRequest detailRequest = new InventoryItemDetailRequest();
                detailRequest.setUpdateMStr12(true);
                detailRequest.setPallet(p.getPallet());
                detailRequest.setM_Str7(p.getProjectNo());
                detailRequest.setM_Str12(p.getDeviceNo());
                detailRequest.setMaterialCode(p.getMaterialCode());
                return detailRequest;
            }).collect(Collectors.toList());
            shipOrderPalletRequest.setShipOrderCode(shipOrderCode);
            shipOrderPalletRequest.setInventoryItemDetailDtoList(inventoryItemDetailRequestList);
            shipOrderPalletRequestList.add(shipOrderPalletRequest);
        }
//        long createTime = LocalDateTime.now().toInstant(ZoneOffset.of("+08:00")).toEpochMilli();
        long createTime = Instant.now().toEpochMilli();
        //未登录会得到全局异常
        WmsResponse wmsResponse = wmsService.subAssignPalletsByShipOrderBatch(shipOrderPalletRequestList, token);
        if (wmsResponse.getResult()) {
//            add(request.getTruckOrderRequest());
//            truckOrderItemService.addBatch(request.getTruckOrderItemRequestList());
            Long shipOrderId = request.getTruckOrderItemRequestList().get(0).getShipOrderId();
            ShipPickOrderRequest shipPickOrderRequest = new ShipPickOrderRequest();
            shipPickOrderRequest.setShipOrderId(shipOrderId);
            shipPickOrderRequest.setStartCreationTime(createTime);
            Sort sort = new Sort();
            sort.setSortType("desc");
            sort.setSortField("Id");
            List<Sort> sortList = new ArrayList<>();
            sortList.add(sort);
            shipPickOrderRequest.setSortFieldList(sortList);
            shipPickOrderRequest.setPageSize(1);
            shipPickOrderRequest.setPageIndex(1);
            shipPickOrderRequest.setSearchCount(false);
            PageData<ShipPickOrderResponse> shipPickOrderPage = shipPickOrderService.getShipPickOrderPage(shipPickOrderRequest);
            List<ShipPickOrderResponse> shipPickOrderResponseList = shipPickOrderPage.getData();
            if (shipPickOrderResponseList.size() != 1) {
                String str = MessageFormat.format("Get ShipPickOrder by shipOrderId - {0} fail", shipOrderId);
                throw new Exception(str);
            }
            ShipPickOrderResponse shipPickOrderResponse = shipPickOrderResponseList.get(0);

            TruckOrderRequest truckOrderRequest = request.getTruckOrderRequest();
            truckOrderRequest.setCreationTime(LocalDateTime.now());
            truckOrderRequest.setLastModificationTime(LocalDateTime.now());
            truckOrderRequest.setCreatorId(shipPickOrderResponse.getCreatorId().toString());
            truckOrderRequest.setCreatorName(shipPickOrderResponse.getCreatorName());

            TruckOrder truckOrder = add(request.getTruckOrderRequest());
            for (TruckOrderItemRequest truckOrderItemRequest : request.getTruckOrderItemRequestList()) {
                truckOrderItemRequest.setTruckOrderId(truckOrder.getId());
                truckOrderItemRequest.setCreationTime(LocalDateTime.now());
                truckOrderItemRequest.setLastModificationTime(LocalDateTime.now());
                truckOrderItemRequest.setCreatorId(shipPickOrderResponse.getCreatorId().toString());
                truckOrderItemRequest.setCreatorName(shipPickOrderResponse.getCreatorName());
            }
            truckOrderItemService.addBatch(request.getTruckOrderItemRequestList());

            //region mqtt
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

            trunkOderMq.setTruckOrderResponseList(truckOrderResponseList);
            trunkOderMq.setTruckOrderItemResponseList(itemList);

            String jsonStr = upperObjectMapper.writeValueAsString(trunkOderMq);
            mqttProduce.publish(TRUCK_ORDER_TOPIC, jsonStr);
            //endregion
        } else {
            throw new Exception("SubAssignPalletsByShipOrderBatch exception");
        }

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


        // 创建分页对象 (当前页, 每页大小)
        Page<TruckOrder> page = new Page<>(request.getPageIndex(), request.getPageSize());

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

}




