package gs.com.gses.controller;

import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.OutBoundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    @Autowired
    private OutBoundOrderService outBoundOrderService;



    @PostMapping("/initInventoryInfoFromDb")
    public MessageResult<Void> initInventoryInfoFromDb() throws Exception {
        outBoundOrderService.initInventoryInfoFromDb();
        return MessageResult.success();
    }
}
