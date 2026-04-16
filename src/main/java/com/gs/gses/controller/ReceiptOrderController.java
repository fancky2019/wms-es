package com.gs.gses.controller;

import com.gs.gses.model.request.wms.ReceiptOrderRequest;
import com.gs.gses.model.response.MessageResult;
import com.gs.gses.service.ReceiptOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
