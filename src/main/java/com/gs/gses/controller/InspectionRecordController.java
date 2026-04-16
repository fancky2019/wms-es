package com.gs.gses.controller;

import com.gs.gses.model.request.wms.InspectionRecordRequest;
import com.gs.gses.model.response.MessageResult;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.wms.InspectionRecordResponse;
import com.gs.gses.service.InspectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/inspectionRecord")
public class InspectionRecordController {
    @Autowired
    private InspectionRecordService inspectionRecordService;

    @PostMapping("/getInspectionRecordPage")
    public MessageResult<PageData<InspectionRecordResponse>> getInspectionRecordPage(@RequestBody InspectionRecordRequest request) throws Exception {
        PageData<InspectionRecordResponse> page = inspectionRecordService.getInspectionRecordPage(request);
        return MessageResult.success(page);
    }

}
