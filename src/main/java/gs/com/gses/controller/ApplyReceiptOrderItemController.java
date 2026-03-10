package gs.com.gses.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import gs.com.gses.aspect.DuplicateSubmission;
import gs.com.gses.ftp.FtpService;
import gs.com.gses.model.request.wms.ApplyReceiptOrderItemRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.ApplyReceiptOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/applyReceiptOrderItem")
public class ApplyReceiptOrderItemController {

    @Autowired
    private ApplyReceiptOrderItemService applyReceiptOrderItemService;


    @Autowired
    private FtpService ftpService;

    @Autowired
    private ObjectMapper objectMapper;


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



    /**
     * @RequestPart 解析 JSON 时 强依赖 Content-Type： application/json
     * @RequestPart("applyReceiptOrderItemRequest") ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest)
     * @RequestParam 完全不关心 Content-Type,接受任何文本类型。兼容：Vue、Uniapp、Android、iOS、Postman
     *
     * Spring 官方建议在 multipart 表单里：
     *
     * 类型	推荐注解
     * 文件	@RequestParam MultipartFile
     * 普通字段	@RequestParam
     * JSON body	@RequestPart
     *
     * @param productFiles
     * @param certificationFiles
     * @param applyReceiptOrderItemRequest
     * @return
     * @throws Exception
     */
    @DuplicateSubmission
    @PostMapping(value = "/inspectionForm")
    public MessageResult inspectionForm(@RequestParam(value = "productFiles", required = false) MultipartFile[] productFiles,
                                        @RequestParam(value = "certificationFiles", required = false) MultipartFile[] certificationFiles,
//                                        @RequestPart("applyReceiptOrderItemRequest") ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest
                                        @RequestParam("applyReceiptOrderItemRequest") String applyReceiptOrderItemRequest) throws Exception {
        //       //uni.uploadFile 会报错 ，postman 可以 @RequestPart("applyReceiptOrderItemRequest") ApplyReceiptOrderItemRequest applyReceiptOrderItemRequest)
////    兼容uniapp    @RequestParam("applyReceiptOrderItemRequest") String applyReceiptOrderItemRequest)
//        // 手动解析 JSON

        ApplyReceiptOrderItemRequest request = objectMapper.readValue(
                applyReceiptOrderItemRequest,
                ApplyReceiptOrderItemRequest.class
        );

        this.applyReceiptOrderItemService.inspectionForm(productFiles, certificationFiles, request);
        return MessageResult.success();
    }



}
