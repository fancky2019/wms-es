package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.InventoryItemDetail;
import gs.com.gses.model.entity.Material;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.entity.ShipOrderItem;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.InventoryItemDetailRequest;
import gs.com.gses.model.request.wms.ShipOrderItemRequest;
import gs.com.gses.model.request.wms.ShipOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.ShipOrderResponse;
import gs.com.gses.model.response.wms.ShipOrderItemResponse;
import gs.com.gses.service.MaterialService;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.mapper.ShipOrderItemMapper;
import gs.com.gses.service.ShipOrderService;
import gs.com.gses.utility.LambdaFunctionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ShipOrderItem】的数据库操作Service实现
 * @createDate 2024-08-11 10:23:06
 */
@Service
public class ShipOrderItemServiceImpl extends ServiceImpl<ShipOrderItemMapper, ShipOrderItem>
        implements ShipOrderItemService {

    @Autowired
    private MaterialService materialService;
    @Autowired
    private ShipOrderService shipOrderService;

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
    public Boolean checkItemExist(ShipOrderItemRequest request) throws Exception {

        if (StringUtils.isEmpty(request.getM_Str7())) {
            throw new Exception("m_Str7 is null");
        }
        if (StringUtils.isEmpty(request.getM_Str12())) {
            throw new Exception("m_Str12 is null");
        }
        if (StringUtils.isEmpty(request.getMaterialCode())) {
            throw new Exception("materialCode is null");
        }

        List<Integer> xStatusList = new ArrayList<>();
        xStatusList.add(1);
        xStatusList.add(2);
        xStatusList.add(3);
        request.setXStatusList(xStatusList);


        request.setSearchCount(false);
//        List<Sort> sortList = new ArrayList<>();
//        Sort sort1 = new Sort();
//        sort1.setSortField("id");
//        sort1.setSortType("asc");
//        sortList.add(sort1);
//        sort1 = new Sort();
//        sort1.setSortField("creationTime");
//        sort1.setSortType("asc");
//        sortList.add(sort1);
//        request.setSortFieldList(sortList);

        PageData<ShipOrderItemResponse> page = getShipOrderItemPage(request);

        int size = page.getData().size();
        if (size == 0) {
            throw new Exception("Can't get ShipOrderItem  by  m_Str7 ,m_Str12,materialCode");
        }
        if (size > 1) {
            throw new Exception("Get multiple  ShipOrderItem info by  m_Str7 ,m_Str12,materialCode");
        }
        ShipOrderItemResponse shipOrderItemResponse = page.getData().get(0);
        ShipOrder shipOrder = shipOrderService.getById(shipOrderItemResponse.getShipOrderId());
        if (shipOrder == null) {
            String str = MessageFormat.format("ShipOrder - {0} lost", shipOrderItemResponse.getShipOrderId().toString());
            throw new Exception(str);
        }
        request.setId(shipOrderItemResponse.getId());
        request.setShipOrderId(shipOrderItemResponse.getShipOrderId());
        request.setShipOrderCode(shipOrder.getXCode());
        request.setApplyShipOrderCode(shipOrder.getApplyShipOrderCode());

        return true;
    }


    @Override
    public PageData<ShipOrderItemResponse> getShipOrderItemPage(ShipOrderItemRequest request) throws Exception {
        LambdaQueryWrapper<ShipOrderItem> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(MqMessage::getStatus, 2);
        //排序
//        queryWrapper.orderByDesc(User::getAge)
//                .orderByAsc(User::getName);


        if (StringUtils.isNotEmpty(request.getM_Str7())) {
            queryWrapper.eq(ShipOrderItem::getM_Str7, request.getM_Str7());
        }
        if (StringUtils.isNotEmpty(request.getM_Str12())) {
            queryWrapper.eq(ShipOrderItem::getM_Str12, request.getM_Str12());
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {
            Material material = materialService.getByCode(request.getMaterialCode());
            if (material != null) {
                queryWrapper.eq(ShipOrderItem::getMaterialId, material.getId());
            }
        }

        if(CollectionUtils.isNotEmpty(request.getXStatusList()))
        {
            queryWrapper.in(ShipOrderItem::getXStatus,request.getXStatusList());
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

        PageData<ShipOrderItemResponse> pageData = new PageData<>();
        pageData.setData(shipOrderItemResponseList);
        pageData.setCount(total);
        return pageData;
    }


    // 调用示例
//    applySort(queryWrapper, "createTime", "desc");  // 按createTime降序


}




