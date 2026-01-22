package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.mapper.wms.ApplyShipOrderItemMapper;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.entity.erp.ErpWorkOrderInfoView;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.ApplyShipOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.erp.ErpWorkOrderInfoViewResponse;
import gs.com.gses.model.response.wms.ApplyShipOrderItemResponse;
import gs.com.gses.model.response.wms.ApplyShipOrderResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.multipledatasource.DataSource;
import gs.com.gses.multipledatasource.DataSourceType;
import gs.com.gses.service.ApplyShipOrderItemService;
import gs.com.gses.utility.LambdaFunctionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lirui
 * @description 针对表【ApplyShipOrderItem】的数据库操作Service实现
 * @createDate 2024-08-11 10:19:07
 */
@Slf4j
@Service
public class ApplyShipOrderItemServiceImpl extends ServiceImpl<ApplyShipOrderItemMapper, ApplyShipOrderItem>
        implements ApplyShipOrderItemService {

    @Override
    public PageData<ApplyShipOrderItemResponse> getApplyShipOrderItemPage(ApplyShipOrderItemRequest request) throws Exception {
        LambdaQueryWrapper<ApplyShipOrderItem> applyShipOrderQueryWrapper = new LambdaQueryWrapper<>();


        if (request.getApplyShipOrderId() != null && request.getApplyShipOrderId() > 0) {
            applyShipOrderQueryWrapper.eq(ApplyShipOrderItem::getApplyShipOrderId, request.getApplyShipOrderId());
        }

        if (request.getMaterialId() != null && request.getMaterialId() > 0) {
            applyShipOrderQueryWrapper.eq(ApplyShipOrderItem::getMaterialId, request.getMaterialId());
        }


        // 创建分页对象 (当前页, 每页大小)
        Page<ApplyShipOrderItem> page = new Page<>(request.getPageIndex(), request.getPageSize());
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
        IPage<ApplyShipOrderItem> applyShipOrderPage = this.baseMapper.selectPage(page, applyShipOrderQueryWrapper);

        // 获取当前页数据
        List<ApplyShipOrderItem> records = applyShipOrderPage.getRecords();
        long total = applyShipOrderPage.getTotal();

        //searchCount 可能false
        if (CollectionUtils.isEmpty(records)) {
            log.info("No records found");
            return PageData.getDefault();
        }


        List<ApplyShipOrderItemResponse> applyShipOrderItemResponseList = records.stream().map(p -> {
            ApplyShipOrderItemResponse response = new ApplyShipOrderItemResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ApplyShipOrderItemResponse> pageData = new PageData<>();
        pageData.setData(applyShipOrderItemResponseList);
        pageData.setCount(total);
        return pageData;
    }

    @DataSource(DataSourceType.MASTER)
    @Override
    public List<ApplyShipOrderItem> getByApplyMaterialIdBatch(List<ErpWorkOrderInfoViewResponse> erpWorkOrderInfoViewResponseList,
                                                              List<ApplyShipOrderResponse> applyShipOrderResponseList,
                                                              Map<String, Material> materialCodeMap,
                                                              Map<String, List<Long>> workOrderApplyCodeMap) {
        List<String> materialCodeList = erpWorkOrderInfoViewResponseList.stream().map(p -> p.getMaterialCode()).collect(Collectors.toList());
//        List<Material> materialList = this.materialService.getByCodeList(materialCodeList);
//        Map<String, Material> materialCodeMap = materialList.stream().collect(Collectors.toMap(p -> p.getXCode(), p -> p));
//
        LambdaQueryWrapper<ApplyShipOrderItem> applyShipOrderItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        applyShipOrderItemLambdaQueryWrapper.and(qw -> {
            for (ErpWorkOrderInfoViewResponse query : erpWorkOrderInfoViewResponseList) {
                Material material = materialCodeMap.get(query.getMaterialCode());
                List<Long> currentApplyShipOrderIdList = applyShipOrderResponseList.stream().filter(p -> p.getXCode().contains(query.getApplyCode())).map(p -> p.getId()).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(currentApplyShipOrderIdList)) {
                    //Exception 抛不出来
                    //因为 Lambda 表达式中的代码块只能抛出 RuntimeException 或其子类，不能抛出受检异常（checked exception）。
                    throw new RuntimeException("Can't get ApplyShipOrder by ApplyCode " + query.getApplyCode());
                }
                workOrderApplyCodeMap.put(query.getApplyCode(), currentApplyShipOrderIdList);
                for (Long applyShipOrderId : currentApplyShipOrderIdList) {
                    qw.or(w -> {
                        w.eq(ApplyShipOrderItem::getApplyShipOrderId, applyShipOrderId);
                        w.eq(ApplyShipOrderItem::getMaterialId, material.getId());
                    });
                }
            }
        });


        List<ApplyShipOrderItem> applyShipOrderItemList = this.list(applyShipOrderItemLambdaQueryWrapper);
        return applyShipOrderItemList;
    }
}




