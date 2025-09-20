package gs.com.gses.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import gs.com.gses.mapper.InspectionRecordMapper;
import gs.com.gses.model.entity.*;
import gs.com.gses.model.request.Sort;
import gs.com.gses.model.request.wms.InspectionRecordRequest;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InspectionRecordResponse;
import gs.com.gses.model.response.wms.TruckOrderItemResponse;
import gs.com.gses.service.InspectionRecordService;
import gs.com.gses.utility.LambdaFunctionHelper;
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
* @description 针对表【InspectionRecord】的数据库操作Service实现
* @createDate 2025-09-16 13:29:34
*/
@Service
public class InspectionRecordServiceImpl extends ServiceImpl<InspectionRecordMapper, InspectionRecord>
    implements InspectionRecordService {

    @Override
    public Boolean addBatch(List<InspectionRecord> inspectionRecordList) {
        for (InspectionRecord inspectionRecord : inspectionRecordList) {
            this.save(inspectionRecord);
        }
        return true;
    }

    @Override
    public PageData<InspectionRecordResponse> getInspectionRecordPage(InspectionRecordRequest request)  {
        LambdaQueryWrapper<InspectionRecord> inspectionRecordQueryWrapper = new LambdaQueryWrapper<>();
        inspectionRecordQueryWrapper.eq(InspectionRecord::getDeleted, 0);


        if (request.getId() != null && request.getId() > 0) {
            inspectionRecordQueryWrapper.eq(InspectionRecord::getId, request.getId());
        }



        if (StringUtils.isNotEmpty(request.getProjectNo())) {
            inspectionRecordQueryWrapper.like(InspectionRecord::getProjectNo, request.getProjectNo());
        }

        if (StringUtils.isNotEmpty(request.getApplyReceiptOrderCode())) {
            inspectionRecordQueryWrapper.like(InspectionRecord::getApplyReceiptOrderCode, request.getApplyReceiptOrderCode());
        }
        if (request.getApplyReceiptOrderItemRowNo()!=null && request.getApplyReceiptOrderItemRowNo()>0) {
            inspectionRecordQueryWrapper.eq(InspectionRecord::getApplyReceiptOrderItemRowNo, request.getApplyReceiptOrderItemRowNo());
        }
        if (StringUtils.isNotEmpty(request.getMaterialCode())) {
            inspectionRecordQueryWrapper.like(InspectionRecord::getMaterialCode, request.getMaterialCode());
        }


        if (StringUtils.isNotEmpty(request.getDeviceNo())) {
            inspectionRecordQueryWrapper.like(InspectionRecord::getDeviceNo, request.getDeviceNo());
        }

        if (StringUtils.isNotEmpty(request.getBatchNo())) {
            inspectionRecordQueryWrapper.like(InspectionRecord::getBatchNo, request.getBatchNo());
        }

        if (StringUtils.isNotEmpty(request.getInspectionResult())) {
            inspectionRecordQueryWrapper.eq(InspectionRecord::getInspectionResult, request.getInspectionResult());
        }

        if (StringUtils.isNotEmpty(request.getCreatorName())) {
            inspectionRecordQueryWrapper.like(InspectionRecord::getCreatorName, request.getCreatorName());
        }

        // 创建分页对象 (当前页, 每页大小)
        Page<InspectionRecord> page = new Page<>(request.getPageIndex(), request.getPageSize());
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
        IPage<InspectionRecord> inspectionRecordPage = this.baseMapper.selectPage(page, inspectionRecordQueryWrapper);

        // 获取当前页数据
        List<InspectionRecord> records = inspectionRecordPage.getRecords();
        long total = inspectionRecordPage.getTotal();

        List<InspectionRecordResponse> InspectionRecordResponseList = records.stream().map(p -> {
            InspectionRecordResponse response = new InspectionRecordResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());


        PageData<InspectionRecordResponse> pageData = new PageData<>();
        pageData.setData(InspectionRecordResponseList);
        pageData.setCount(total);
        return pageData;
    }

}




