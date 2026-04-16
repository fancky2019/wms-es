package com.gs.gses.controller;

import com.gs.gses.model.request.erp.ErpWorkOrderInfoViewRequest;
import com.gs.gses.model.response.MessageResult;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.erp.ErpWorkOrderInfoViewResponse;
import com.gs.gses.service.erp.ErpWorkOrderInfoViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/erpWorkOrderInfoView")
public class ErpWorkOrderInfoViewController {


    @Autowired
    private ErpWorkOrderInfoViewService erpWorkorderinfoService;

    @PostMapping("/getErpWorkOrderInfoViewPage")
    public MessageResult<PageData<ErpWorkOrderInfoViewResponse>> getErpWorkOrderInfoViewPage(@Validated @RequestBody ErpWorkOrderInfoViewRequest request) throws Exception {
        return MessageResult.success(erpWorkorderinfoService.getErpWorkOrderInfoViewPage(request));
    }

    /**
     *
     * @param request
     * @param httpServletResponse
     * @throws Exception
     */
    @PostMapping(value = "/export")
    public void export(@Validated @RequestBody ErpWorkOrderInfoViewRequest request, HttpServletResponse httpServletResponse) throws Exception {
        this.erpWorkorderinfoService.export(request, httpServletResponse);
    }


}
