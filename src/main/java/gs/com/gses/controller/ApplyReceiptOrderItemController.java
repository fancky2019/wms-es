package gs.com.gses.controller;


import gs.com.gses.aspect.DuplicateSubmission;
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

    @DuplicateSubmission
    @PostMapping(value = "/inspection")
    public MessageResult inspection(@RequestPart(value = "files", required = false) MultipartFile[] files, @RequestPart("applyReceiptOrderItemRequest") ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception {
        this.applyReceiptOrderItemService.inspection(files, applyReceiptOrderItemRequest);
//        this.applyReceiptOrderItemService.inspectionOptimization(files, applyReceiptOrderItemRequest);
        return MessageResult.success();
    }

    @DuplicateSubmission
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

    @DuplicateSubmission(timeOut = 0)
    @PostMapping(value = "/createWorkingDirectory")
    public MessageResult createWorkingDirectory() throws Exception {
        int m = Integer.parseInt("m");
        this.ftpService.createWorkingDirectory("wms/2025/09/11/P0002150747");
//        this.applyReceiptOrderItemService.inspectionOptimization(files, applyReceiptOrderItemRequest);
        return MessageResult.success();
    }

    @DuplicateSubmission
    @PostMapping(value = "/inspectionForm")
    public MessageResult inspectionForm(@RequestPart(value = "files", required = false) MultipartFile[] files,
                                        @RequestPart(value = "files1", required = false) MultipartFile[] files1,
                                        @RequestPart(value = "files2", required = false) MultipartFile[] files2,
                                        @RequestPart("applyReceiptOrderItemRequest") ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest) throws Exception {
        this.applyReceiptOrderItemService.inspectionForm(files, files1, files2, applyReceiptOrderItemRequest);
//        this.applyReceiptOrderItemService.inspectionOptimization(files, applyReceiptOrderItemRequest);
        return MessageResult.success();
    }
}
