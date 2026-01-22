package gs.com.gses.controller;

import gs.com.gses.model.entity.demo.ProductTest;
import gs.com.gses.model.request.erp.ErpWorkOrderInfoViewRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.demo.ProductTestService;
import gs.com.gses.service.erp.ErpWorkOrderInfoViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dynamicDataSource")
public class DynamicDataSourceController {

    @Autowired
    private ProductTestService productTestService;

    @Autowired
    private ErpWorkOrderInfoViewService erpWorkorderinfoService;

    @GetMapping("/getProductTestById/{id}")
    public MessageResult<ProductTest> getProductTestById(@PathVariable("id") Integer id) throws Exception {
        ProductTest productTest = productTestService.getProductTestById(id);
        return MessageResult.success(productTest);
    }

    @GetMapping("/mssqlSlaveTranTest")
    public MessageResult<Void> mssqlSlaveTranTest() throws Exception {
        productTestService.mssqlSlaveTranTest1();
        return MessageResult.success();
    }

    @GetMapping("/mysqlSlaveTranTest")
    public MessageResult<Void> mysqlSlaveTranTest() throws Exception {
        productTestService.mysqlSlaveTranTest1();
        return MessageResult.success();
    }

    @GetMapping("/nestedTranTest")
    public MessageResult<Void> nestedTranTest() throws Exception {
        productTestService.nestedTranTest1();
        return MessageResult.success();
    }

    @PostMapping("/oracleQuery")
    public MessageResult<Void> oracleQuery(ErpWorkOrderInfoViewRequest request) throws Exception {
        erpWorkorderinfoService.getErpWorkOrderInfoViewPage(request);
        return MessageResult.success();
    }

}
