package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.ReceiptOrderItem;
import gs.com.gses.mapper.wms.ReceiptOrderItemMapper;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.ReceiptOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ReceiptOrderItemResponse;
import gs.com.gses.service.ReceiptOrderItemService;
import gs.com.gses.utility.LambdaFunctionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ReceiptOrderItem】的数据库操作Service实现
 * @createDate 2025-07-29 11:07:16
 */
@Service
public class ReceiptOrderItemServiceImpl extends ServiceImpl<ReceiptOrderItemMapper, ReceiptOrderItem>
        implements ReceiptOrderItemService {

    @Override
    public PageData<ReceiptOrderItemResponse> getReceiptOrderItemPage(ReceiptOrderItemRequest request) {
        LambdaQueryWrapper<ReceiptOrderItem> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getReceiptOrderId() != null && request.getReceiptOrderId() > 0) {
            queryWrapper.eq(ReceiptOrderItem::getReceiptOrderId, request.getReceiptOrderId());
        }

        if (CollectionUtils.isNotEmpty(request.getReceiptOrderIdList())) {
            queryWrapper.in(ReceiptOrderItem::getReceiptOrderId, request.getReceiptOrderIdList());
        }

        // 创建分页对象 (当前页, 每页大小)
        Page<ReceiptOrderItem> page = new Page<>(request.getPageIndex(), request.getPageSize());

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
        IPage<ReceiptOrderItem> receiptOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取当前页数据
        List<ReceiptOrderItem> records = receiptOrderPage.getRecords();
        long total = receiptOrderPage.getTotal();

        List<ReceiptOrderItemResponse> receiptOrderItemResponseList = records.stream().map(p -> {
            ReceiptOrderItemResponse response = new ReceiptOrderItemResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ReceiptOrderItemResponse> pageData = new PageData<>();
        pageData.setData(receiptOrderItemResponseList);
        pageData.setCount(total);
        return pageData;
    }
}




