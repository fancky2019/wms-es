package com.gs.gses.controller;

import com.gs.gses.model.request.erp.ErpProjectInfoViewRequest;
import com.gs.gses.model.response.MessageResult;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.erp.ErpProjectInfoViewResponse;
import com.gs.gses.service.erp.ErpProjectInfoViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/erpProjectInfoView")
public class ErpProjectInfoViewController {

    @Autowired
    private ErpProjectInfoViewService erpProjectInfoViewService;

    @PostMapping("/getErpProjectInfoViewPage")
    public MessageResult<PageData<ErpProjectInfoViewResponse>> getErpProjectInfoViewPage(@Validated @RequestBody ErpProjectInfoViewRequest request) throws Exception {
        return MessageResult.success(erpProjectInfoViewService.getErpProjectInfoViewPage(request));
    }


}
