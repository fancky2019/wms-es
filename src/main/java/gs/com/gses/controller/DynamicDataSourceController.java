package gs.com.gses.controller;

import gs.com.gses.model.entity.demo.ProductTest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.demo.ProductTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dynamicDataSource")
public class DynamicDataSourceController {

    @Autowired
    private ProductTestService productTestService;

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
}
