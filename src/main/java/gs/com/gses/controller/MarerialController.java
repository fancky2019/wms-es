package gs.com.gses.controller;

import gs.com.gses.ftp.FtpService;
import gs.com.gses.model.request.wms.ApplyReceiptOrderItemRequest;
import gs.com.gses.model.request.wms.MaterialRequest;
import gs.com.gses.model.response.MessageResult;
import gs.com.gses.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@RestController
@RequestMapping("/api/marerial")
public class MarerialController {

    @Autowired
    private MaterialService materialService;

    @PostMapping(value = "/uploadInspectionTemple")
    public MessageResult<Void> uploadInspectionTemple(@RequestPart(value = "files", required = false) MultipartFile[] files, @RequestPart("materialRequest") MaterialRequest materialRequest) throws Exception {
        this.materialService.uploadInspectionTemple(files, materialRequest);
        return MessageResult.success();
    }


}

