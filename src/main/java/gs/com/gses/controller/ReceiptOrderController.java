package gs.com.gses.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import gs.com.gses.model.entity.ShipOrder;
import gs.com.gses.model.request.wms.ReceiptOrderRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.ReceiptOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receiptOrder")
public class ReceiptOrderController {

    @Autowired
    private ReceiptOrderService receiptOrderService;


    @GetMapping("/printProcessInBoundCode")
    public MessageResult<Void> printProcessInBoundCode(ReceiptOrderRequest request) throws Exception {
        receiptOrderService.printProcessInBoundCode(request);
        return MessageResult.success();
    }
}
