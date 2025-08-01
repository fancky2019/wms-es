package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.model.entity.Material;
import gs.com.gses.model.entity.ReceiptOrder;
import gs.com.gses.mapper.ReceiptOrderMapper;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.entity.TruckOrder;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.ReceiptOrderItemRequest;
import gs.com.gses.model.request.wms.ReceiptOrderRequest;
import gs.com.gses.model.request.wms.ShipOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.ShipOrderResponse;
import gs.com.gses.model.response.mqtt.PrintProcessInBoundCode;
import gs.com.gses.model.response.mqtt.PrintWrapper;
import gs.com.gses.model.response.mqtt.TrunkOderMq;
import gs.com.gses.model.response.wms.ReceiptOrderItemResponse;
import gs.com.gses.model.response.wms.ReceiptOrderResponse;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import gs.com.gses.rabbitMQ.mqtt.MqttProduce;
import gs.com.gses.service.MaterialService;
import gs.com.gses.service.ReceiptOrderItemService;
import gs.com.gses.service.ReceiptOrderService;
import gs.com.gses.utility.LambdaFunctionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ReceiptOrder】的数据库操作Service实现
 * @createDate 2025-07-29 11:07:16
 */
@Slf4j
@Service
public class ReceiptOrderServiceImpl extends ServiceImpl<ReceiptOrderMapper, ReceiptOrder>
        implements ReceiptOrderService {

    @Autowired
    private ReceiptOrderItemService receiptOrderItemService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MqttProduce mqttProduce;

    @Autowired
    @Qualifier("upperObjectMapper")
    private ObjectMapper upperObjectMapper;

    @Override
    public void printProcessInBoundCode(ReceiptOrderRequest request) throws Exception {
        if (StringUtils.isEmpty(request.getStr3())) {
            throw new Exception("工单号为空");
        }
        if (StringUtils.isEmpty(request.getStr4())) {
            throw new Exception("工序号为空");
        }
        request.setSearchCount(false);
        request.setPageSize(100);
        PageData<ReceiptOrderResponse> pageData = getReceiptOrderPage(request);
        List<ReceiptOrderResponse> receiptOrderResponseList = pageData.getData();

        List<Long> receiptOrderIdList = receiptOrderResponseList.stream().map(p -> p.getId()).collect(Collectors.toList());
        ReceiptOrderItemRequest receiptOrderItemRequest = new ReceiptOrderItemRequest();
        receiptOrderItemRequest.setReceiptOrderIdList(receiptOrderIdList);
        request.setSearchCount(false);
        PageData<ReceiptOrderItemResponse> receiptOrderItemPageData = receiptOrderItemService.getReceiptOrderItemPage(receiptOrderItemRequest);
        List<ReceiptOrderItemResponse> receiptOrderItemResponseList = receiptOrderItemPageData.getData();
        List<Long> materialIdList = receiptOrderItemResponseList.stream().map(p -> p.getMaterialId()).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(materialIdList)) {
            log.info("收货单没有明细");
            return;
        }
        List<Material> materialList = this.materialService.listByIds(materialIdList);
        for (ReceiptOrderItemResponse item : receiptOrderItemResponseList) {
            ReceiptOrderResponse receiptOrderResponse = receiptOrderResponseList.stream().filter(p -> p.getId().equals(item.getReceiptOrderId())).collect(Collectors.toList()).get(0);
            String receiptOrderCode = receiptOrderResponse.getXCode();
            Material material = materialList.stream().filter(p -> p.getId().equals(item.getMaterialId())).collect(Collectors.toList()).get(0);
            String materialCode = material.getXCode();

            String msgId = UUID.randomUUID().toString().replaceAll("-", "");
            PrintProcessInBoundCode printProcessInBoundCode = new PrintProcessInBoundCode();
            printProcessInBoundCode.setMsgId(msgId);
            printProcessInBoundCode.setStr3(request.getStr3());
            printProcessInBoundCode.setStr4(request.getStr4());
            printProcessInBoundCode.setReceiptOrderCode(receiptOrderCode);
            printProcessInBoundCode.setMaterialCode(materialCode);

            PrintWrapper<PrintProcessInBoundCode> mqttWrapper = new PrintWrapper();
            mqttWrapper.setCount(1);
            mqttWrapper.setData(Arrays.asList(printProcessInBoundCode));
            String jsonStr = upperObjectMapper.writeValueAsString(mqttWrapper);
            log.info("start publish msgId:{}", msgId);
            mqttProduce.publish(UtilityConst.PRINT_PROCESS_INBOUND_CODE, jsonStr, msgId);
        }
    }

    @Override
    public PageData<ReceiptOrderResponse> getReceiptOrderPage(ReceiptOrderRequest request) {
        LambdaQueryWrapper<ReceiptOrder> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotEmpty(request.getStr3())) {
            queryWrapper.eq(ReceiptOrder::getStr3, request.getStr3());
        }

        if (StringUtils.isNotEmpty(request.getStr4())) {
            queryWrapper.eq(ReceiptOrder::getStr4, request.getStr4());
        }


        // 创建分页对象 (当前页, 每页大小)
        Page<ReceiptOrder> page = new Page<>(request.getPageIndex(), request.getPageSize());

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
        IPage<ReceiptOrder> receiptOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取当前页数据
        List<ReceiptOrder> records = receiptOrderPage.getRecords();
        long total = receiptOrderPage.getTotal();

        List<ReceiptOrderResponse> receiptOrderResponseList = records.stream().map(p -> {
            ReceiptOrderResponse response = new ReceiptOrderResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ReceiptOrderResponse> pageData = new PageData<>();
        pageData.setData(receiptOrderResponseList);
        pageData.setCount(total);
        return pageData;
    }
}




