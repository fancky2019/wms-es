package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.entity.ShipOrderItem;
import gs.com.gses.model.request.InventoryInfoRequest;
import gs.com.gses.model.request.ShipOrderRequest;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.ShipOrderResponse;
import gs.com.gses.service.ShipOrderItemService;
import gs.com.gses.service.ShipOrderService;
import gs.com.gses.mapper.ShipOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ShipOrder】的数据库操作Service实现
 * @createDate 2024-08-11 10:23:06
 */
@Slf4j
@Service
public class ShipOrderServiceImpl extends ServiceImpl<ShipOrderMapper, ShipOrder>
        implements ShipOrderService {


    @Autowired
    private InventoryInfoServiceImpl inventoryInfoService;

    @Autowired
    private ShipOrderItemService shipOrderItemService;


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

        // 创建分页对象 (当前页, 每页大小)
        Page<ShipOrder> page = new Page<>(request.getPageIndex(), request.getPageSize());
        // 关键设置：不执行 COUNT 查询
//        page.setSearchCount(false);
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

    public <T> LambdaQueryWrapper<T> orderByField(
            LambdaQueryWrapper<T> wrapper,
            String fieldName,
            boolean isAsc) {

        try {
//            Method method = LambdaQueryWrapper.class.getMethod(
//                    isAsc ? "orderByAsc" : "orderByDesc",
//                    SFunction.class );

            // 获取实体类字段对应的getter方法
            String getter = "get" + fieldName.substring(0, 1).toUpperCase()
                    + fieldName.substring(1);

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

        list = list.stream().filter(p -> p.getId().equals(674145933246534L)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(list)) {
            log.info("ShipOrder data is empty");
            return;
        }

        List<String> shipOrderCodeList = list.stream().map(p -> p.getXCode()).distinct().collect(Collectors.toList());
        log.info("shipOrderCode:{}", String.join(",", shipOrderCodeList));

        List<Long> shipOrderIdList = list.stream().map(p -> p.getId()).distinct().collect(Collectors.toList());
        List<ShipOrderItem> shipOrderItemList = shipOrderItemService.getByShipOrderIds(shipOrderIdList);

        for (ShipOrderResponse shipOrderResponse : list) {
            List<Long> materialIdList = shipOrderItemList.stream().filter(p -> p.getShipOrderId().equals(shipOrderResponse.getId()))
                    .map(p -> p.getMaterialId()).distinct().collect(Collectors.toList());
            InventoryInfoRequest inventoryInfoRequest = new InventoryInfoRequest();
            inventoryInfoRequest.setMaterialIdList(materialIdList);
            HashMap<Object, List<InventoryInfo>> page = this.inventoryInfoService.getDefaultAllocatedInventoryInfoList(inventoryInfoRequest);

            int m=0;
        }

        stopWatch.stop();
//        stopWatch.start("BatchInsert_Trace2");
        long mills = stopWatch.getTotalTimeMillis();
        log.info("allocate complete {} ms", mills);
    }


}




