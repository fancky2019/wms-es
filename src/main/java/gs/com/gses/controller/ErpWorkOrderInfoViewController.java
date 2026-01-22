package gs.com.gses.controller;

import gs.com.gses.model.request.erp.ErpWorkOrderInfoViewRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.erp.ErpWorkOrderInfoViewResponse;
import gs.com.gses.service.erp.ErpWorkOrderInfoViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/erpWorkOrderInfoView")
public class ErpWorkOrderInfoViewController {


    @Autowired
    private ErpWorkOrderInfoViewService erpWorkorderinfoService;

    @PostMapping("/getErpWorkOrderInfoViewPage")
    public MessageResult<PageData<ErpWorkOrderInfoViewResponse>> getErpWorkOrderInfoViewPage(@RequestBody ErpWorkOrderInfoViewRequest request) throws Exception {
        return MessageResult.success(erpWorkorderinfoService.getErpWorkOrderInfoViewPage(request));
    }

    /**
     *
     * @param request
     * @param httpServletResponse
     * @throws Exception
     */
    @PostMapping(value = "/export")
    public void export(@RequestBody ErpWorkOrderInfoViewRequest request, HttpServletResponse httpServletResponse) throws Exception {
        this.erpWorkorderinfoService.export(request, httpServletResponse);
    }


}
