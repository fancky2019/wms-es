package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.ApplyReceiptOrder;
import gs.com.gses.model.entity.Material;
import gs.com.gses.model.entity.TruckOrder;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.ApplyReceiptOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.ApplyReceiptOrderResponse;
import gs.com.gses.model.response.wms.TruckOrderResponse;
import gs.com.gses.service.ApplyReceiptOrderService;
import gs.com.gses.mapper.wms.ApplyReceiptOrderMapper;
import gs.com.gses.utility.LambdaFunctionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ApplyReceiptOrder】的数据库操作Service实现
 * @createDate 2025-09-03 16:25:44
 */
@Service
public class ApplyReceiptOrderServiceImpl extends ServiceImpl<ApplyReceiptOrderMapper, ApplyReceiptOrder>
        implements ApplyReceiptOrderService {


    @Override
    public  PageData<ApplyReceiptOrderResponse> getApplyReceiptOrderPage(ApplyReceiptOrderRequest request)
    {
        LambdaQueryWrapper<ApplyReceiptOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(request.getXCode())) {
            queryWrapper.eq(ApplyReceiptOrder::getXCode, request.getXCode());
        }

//        if (StringUtils.isNotEmpty(request.getXCode())) {
//            queryWrapper.like(ApplyReceiptOrder::getXCode, request.getXCode());
//        }

        // 创建分页对象 (当前页, 每页大小)
        Page<ApplyReceiptOrder> page = new Page<>(request.getPageIndex(), request.getPageSize());

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
        IPage<ApplyReceiptOrder> applyReceiptOrderPage = this.baseMapper.selectPage(page, queryWrapper);

        // 获取当前页数据
        List<ApplyReceiptOrder> records = applyReceiptOrderPage.getRecords();
        long total = applyReceiptOrderPage.getTotal();

        List<ApplyReceiptOrderResponse> applyReceiptOrderResponseList = records.stream().map(p -> {
            ApplyReceiptOrderResponse response = new ApplyReceiptOrderResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ApplyReceiptOrderResponse> pageData = new PageData<>();
        pageData.setData(applyReceiptOrderResponseList);
        pageData.setCount(total);
        return pageData;
    }

    @Override
    public ApplyReceiptOrder getByCode(String applyReceiptOrderCode) throws Exception {
        if (StringUtils.isEmpty(applyReceiptOrderCode)) {
            throw new Exception("applyReceiptOrderCode is empty ");
        }
        LambdaQueryWrapper<ApplyReceiptOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApplyReceiptOrder::getXCode, applyReceiptOrderCode);
        List<ApplyReceiptOrder> list = this.list(queryWrapper);
        if (list.size() > 1) {
            throw new Exception("find more than one ApplyReceiptOrder info  by " + applyReceiptOrderCode);
        }
        if (list.isEmpty()) {
            throw new Exception("can't get ApplyReceiptOrder info  by " + applyReceiptOrderCode);
        }
        return list.get(0);
    }
}




