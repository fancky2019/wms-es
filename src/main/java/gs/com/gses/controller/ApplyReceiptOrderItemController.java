package gs.com.gses.controller;


import gs.com.gses.ftp.FtpService;
import gs.com.gses.model.request.wms.ApplyReceiptOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.ApplyReceiptOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/applyReceiptOrderItem")
public class ApplyReceiptOrderItemController {

    @Autowired
    private ApplyReceiptOrderItemService applyReceiptOrderItemService;


    @Autowired
    private FtpService ftpService;

    @PostMapping(value = "/inspection")
    public MessageResult inspection(@RequestPart(value = "files", required = false) MultipartFile[] files, @RequestPart("applyReceiptOrderItemRequest") ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception {
        this.applyReceiptOrderItemService.inspection(files, applyReceiptOrderItemRequest);
//        this.applyReceiptOrderItemService.inspectionOptimization(files, applyReceiptOrderItemRequest);
        return MessageResult.success();
    }

    @PostMapping(value = "/inspectionOptimization")
    public MessageResult inspectionOptimization(@RequestPart(value = "files", required = false) MultipartFile[] files, @RequestPart("applyReceiptOrderItemRequest") ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception {
        this.applyReceiptOrderItemService.inspectionOptimization(files, applyReceiptOrderItemRequest);
//        this.applyReceiptOrderItemService.inspectionOptimization(files, applyReceiptOrderItemRequest);
        return MessageResult.success();
    }

    @PostMapping(value = "/specificCellWriteExample")
    public MessageResult specificCellWriteExample() throws Exception {
        this.applyReceiptOrderItemService.specificCellWriteExample();
        return MessageResult.success();
    }


    @PostMapping(value = "/createWorkingDirectory")
    public MessageResult createWorkingDirectory() throws Exception {
        this.ftpService.createWorkingDirectory("wms/2025/09/11/P0002150747");
//        this.applyReceiptOrderItemService.inspectionOptimization(files, applyReceiptOrderItemRequest);
        return MessageResult.success();
    }
}
