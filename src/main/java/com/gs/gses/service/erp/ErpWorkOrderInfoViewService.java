package com.gs.gses.service.erp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.entity.erp.ErpWorkOrderInfoView;
import com.gs.gses.model.request.erp.ErpWorkOrderInfoViewRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.erp.ErpWorkOrderInfoViewResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * @author lirui
 * @description 针对表【ERP_WORKORDERINFO】的数据库操作Service
 * @createDate 2026-01-19 14:31:07
 */
public interface ErpWorkOrderInfoViewService extends IService<ErpWorkOrderInfoView> {

    PageData<ErpWorkOrderInfoViewResponse> getErpWorkOrderInfoViewPage(ErpWorkOrderInfoViewRequest request) throws Exception;

    void export(ErpWorkOrderInfoViewRequest request, HttpServletResponse httpServletResponse) throws Exception;
}
