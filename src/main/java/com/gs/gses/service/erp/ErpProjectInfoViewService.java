package com.gs.gses.service.erp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.entity.erp.ErpProjectInfoView;
import com.gs.gses.model.request.erp.ErpProjectInfoViewRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.erp.ErpProjectInfoViewResponse;

public interface ErpProjectInfoViewService extends IService<ErpProjectInfoView> {

    PageData<ErpProjectInfoViewResponse> getErpProjectInfoViewPage(ErpProjectInfoViewRequest request) throws Exception;

}
