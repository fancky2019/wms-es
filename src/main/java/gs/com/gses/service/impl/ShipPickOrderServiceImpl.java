package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.ShipPickOrder;
import gs.com.gses.model.request.wms.ShipPickOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ShipPickOrderResponse;
import gs.com.gses.service.ShipPickOrderService;
import gs.com.gses.mapper.wms.ShipPickOrderMapper;
import gs.com.gses.utility.LambdaFunctionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ShipPickOrder】的数据库操作Service实现
 * @createDate 2024-08-11 10:23:06
 */
@Service
public class ShipPickOrderServiceImpl extends ServiceImpl<ShipPickOrderMapper, ShipPickOrder>
        implements ShipPickOrderService {
    @Override
    public PageData<ShipPickOrderResponse> getShipPickOrderPage(ShipPickOrderRequest request) {
        LambdaQueryWrapper<ShipPickOrder> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getShipOrderId() != null && request.getShipOrderId() > 0) {
            queryWrapper.eq(ShipPickOrder::getShipOrderId, request.getShipOrderId());
        }
        if (request.getStartCreationTime() != null && request.getStartCreationTime() > 0) {
            queryWrapper.ge(ShipPickOrder::getCreationTime, request.getStartCreationTime());
        }

        // 创建分页对象 (当前页, 每页大小)
        Page<ShipPickOrder> page = new Page<>(request.getPageIndex(), request.getPageSize());

        if (CollectionUtils.isNotEmpty(request.getSortFieldList())) {
            List<OrderItem> orderItems = LambdaFunctionHelper.getWithDynamicSort(request.getSortFieldList());
            page.setOrders(orderItems);
        }

        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<ShipPickOrder> truckOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取当前页数据
        List<ShipPickOrder> records = truckOrderPage.getRecords();
        long total = truckOrderPage.getTotal();

        List<ShipPickOrderResponse> shipPickOrderResponseList = records.stream().map(p -> {
            ShipPickOrderResponse response = new ShipPickOrderResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ShipPickOrderResponse> pageData = new PageData<>();
        pageData.setData(shipPickOrderResponseList);
        pageData.setCount(total);
        return pageData;
    }
}




