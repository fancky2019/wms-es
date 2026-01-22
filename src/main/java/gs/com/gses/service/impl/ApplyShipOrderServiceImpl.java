package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.ApplyShipOrderRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.erp.ErpWorkOrderInfoViewResponse;
import gs.com.gses.model.response.wms.ApplyShipOrderResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.multipledatasource.DataSource;
import gs.com.gses.multipledatasource.DataSourceType;
import gs.com.gses.service.ApplyShipOrderService;
import gs.com.gses.mapper.wms.ApplyShipOrderMapper;
import gs.com.gses.utility.LambdaFunctionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ApplyShipOrder】的数据库操作Service实现
 * @createDate 2024-08-11 10:19:07
 */
@Slf4j
@Service
public class ApplyShipOrderServiceImpl extends ServiceImpl<ApplyShipOrderMapper, ApplyShipOrder>
        implements ApplyShipOrderService {

    @DataSource(DataSourceType.MASTER)
    @Override
    public PageData<ApplyShipOrderResponse> getApplyShipOrderPage(ApplyShipOrderRequest request) throws Exception {
        LambdaQueryWrapper<ApplyShipOrder> applyShipOrderQueryWrapper = new LambdaQueryWrapper<>();


        if (StringUtils.isNotEmpty(request.getXCode())) {
            applyShipOrderQueryWrapper.like(ApplyShipOrder::getXCode, request.getXCode());
        }
        if (CollectionUtils.isNotEmpty(request.getXCodeList())) {
//            applyShipOrderQueryWrapper.in(ApplyShipOrder::getXCode, request.getXCodeList());
            applyShipOrderQueryWrapper.and(qw -> {
                for (String applyShipOrderCode : request.getXCodeList()) {
                    qw.or(w -> {
                        w.like(ApplyShipOrder::getXCode, applyShipOrderCode);
                    });
                }
            });

        }


        // 创建分页对象 (当前页, 每页大小)
        Page<ApplyShipOrder> page = new Page<>(request.getPageIndex(), request.getPageSize());
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
        IPage<ApplyShipOrder> applyShipOrderPage = this.baseMapper.selectPage(page, applyShipOrderQueryWrapper);

        // 获取当前页数据
        List<ApplyShipOrder> records = applyShipOrderPage.getRecords();
        long total = applyShipOrderPage.getTotal();

        //searchCount 可能false
        if (CollectionUtils.isEmpty(records)) {
            log.info("No records found");
            return PageData.getDefault();
        }


        List<ApplyShipOrderResponse> applyShipOrderResponseList = records.stream().map(p -> {
            ApplyShipOrderResponse response = new ApplyShipOrderResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ApplyShipOrderResponse> pageData = new PageData<>();
        pageData.setData(applyShipOrderResponseList);
        pageData.setCount(total);
        return pageData;
    }
}




