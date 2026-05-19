package com.gs.gses.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gs.gses.mapper.erp.ErpProjectInfoViewMapper;
import com.gs.gses.model.entity.erp.ErpProjectInfoView;
import com.gs.gses.model.request.erp.ErpProjectInfoViewRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.erp.ErpProjectInfoViewResponse;
import com.gs.gses.multipledatasource.DataSource;
import com.gs.gses.multipledatasource.DataSourceType;
import com.gs.gses.service.erp.ErpProjectInfoViewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ErpProjectInfoViewImpl extends ServiceImpl<ErpProjectInfoViewMapper, ErpProjectInfoView> implements ErpProjectInfoViewService {


//    事务内动态切换数据源，默认是切不过去的：Spring事务中的 Connection 是线程绑定的，当前事务已经绑定好的 MASTER Connection不会重新切换。


   /*
    REQUIRED	         融入或创建事务	    使用同一个连接
    REQUIRES_NEW	     挂起并创建新事务	    先挂起旧连接，再创建使用新连接（新连接会关闭自动提交）
    NOT_SUPPORTED	     挂起并以非事务运行	先挂起旧连接，再获取使用新连接（新连接保持自动提交）
    */


    @DataSource(DataSourceType.THIRD)
//    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)  // 挂起当前事务:开启新连接，挂起前一个连接
    @Override
    public PageData<ErpProjectInfoViewResponse> getErpProjectInfoViewPage(ErpProjectInfoViewRequest request) throws Exception {
        LambdaQueryWrapper<ErpProjectInfoView> projectInfoViewQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(request.getProjectCode())) {
            projectInfoViewQueryWrapper.eq(ErpProjectInfoView::getProjectCode, request.getProjectCode());
        }

        // 创建分页对象 (当前页, 每页大小)
        Page<ErpProjectInfoView> page = new Page<>(request.getPageIndex(), request.getPageSize());
        if (request.getSearchCount() != null) {
            // 关键设置：不执行 COUNT 查询
            page.setSearchCount(request.getSearchCount());
        }

        // 执行分页查询, sqlserver 使用通用表达式 WITH selectTemp AS
        IPage<ErpProjectInfoView> erpProjectInfoViewPage = this.baseMapper.selectPage(page, projectInfoViewQueryWrapper);
        // 获取当前页数据
        List<ErpProjectInfoView> erpProjectInfoViewList = erpProjectInfoViewPage.getRecords();
        long total = erpProjectInfoViewPage.getTotal();
        if (CollectionUtils.isEmpty(erpProjectInfoViewPage.getRecords())) {
            log.info("No records found");
            return PageData.getDefault();
        }

        List<ErpProjectInfoViewResponse> erpProjectInfoViewResponseList = erpProjectInfoViewList.stream().map(p -> {
            ErpProjectInfoViewResponse response = new ErpProjectInfoViewResponse();
            BeanUtils.copyProperties(p, response);
            return response;
        }).collect(Collectors.toList());

        PageData<ErpProjectInfoViewResponse> pageData = new PageData<>();
        pageData.setData(erpProjectInfoViewResponseList);
        pageData.setCount(total);
        return pageData;
    }
}
