package com.gs.gses.controller;

import com.gs.gses.ftp.FtpService;
import com.gs.gses.model.request.wms.MaterialRequest;
import com.gs.gses.model.response.MessageResult;
import com.gs.gses.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/marerial")
public class MarerialController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private FtpService ftpService;

    @PostMapping(value = "/uploadInspectionTemple")
    public MessageResult<Void> uploadInspectionTemple(@RequestPart(value = "files", required = false) MultipartFile[] files, @RequestPart("materialRequest") MaterialRequest materialRequest) throws Exception {
        this.materialService.uploadInspectionTemple(files, materialRequest);
        return MessageResult.success();
    }

    @GetMapping("/ftpDownloadFile")
    public void ftpDownloadFile(@RequestParam String filePath, HttpServletResponse response) {
        ftpService.ftpDownloadFile(filePath, response);
    }
}

