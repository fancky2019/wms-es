package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.ShipPickOrderItem;
import gs.com.gses.model.request.wms.ShipPickOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ShipPickOrderItemResponse;
import gs.com.gses.service.ShipPickOrderItemService;
import gs.com.gses.mapper.wms.ShipPickOrderItemMapper;
import gs.com.gses.utility.LambdaFunctionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author lirui
* @description 针对表【ShipPickOrderItem】的数据库操作Service实现
* @createDate 2024-08-11 10:23:06
*/
@Service
public class ShipPickOrderItemServiceImpl extends ServiceImpl<ShipPickOrderItemMapper, ShipPickOrderItem>
    implements ShipPickOrderItemService{
    @Override
    public PageData<ShipPickOrderItemResponse> getShipPickOrderItemPage(ShipPickOrderItemRequest request) throws Exception {
        LambdaQueryWrapper<ShipPickOrderItem> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(MqMessage::getStatus, 2);
        //排序
//        queryWrapper.orderByDesc(User::getAge)
//                .orderByAsc(User::getName);

        if (request.getShipOrderItemId() != null && request.getShipOrderItemId() > 0) {
            queryWrapper.eq(ShipPickOrderItem::getShipOrderItemId, request.getShipOrderItemId());
        }



        // 创建分页对象 (当前页, 每页大小)
        Page<ShipPickOrderItem> page = new Page<>(request.getPageIndex(), request.getPageSize());

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
        IPage<ShipPickOrderItem> shipOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取结果   // 当前页数据
        List<ShipPickOrderItem> records = shipOrderPage.getRecords();
        long total = shipOrderPage.getTotal();

        List<ShipPickOrderItemResponse> shipPickOrderItemResponseList = records.stream().map(p -> {
            ShipPickOrderItemResponse response = new ShipPickOrderItemResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ShipPickOrderItemResponse> pageData = new PageData<>();
        pageData.setData(shipPickOrderItemResponseList);
        pageData.setCount(total);
        return pageData;
    }

}




