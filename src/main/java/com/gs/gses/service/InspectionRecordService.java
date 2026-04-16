package com.gs.gses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gses.model.entity.InspectionRecord;
import com.gs.gses.model.request.wms.InspectionRecordRequest;
import com.gs.gses.model.response.PageData;
import com.gs.gses.model.response.wms.InspectionRecordResponse;

import java.util.List;

/**
 * @author lirui
 * @description 针对表【InspectionRecord】的数据库操作Service
 * @createDate 2025-09-16 13:29:34
 */
public interface InspectionRecordService extends IService<InspectionRecord> {
    Boolean addBatch(List<InspectionRecord> inspectionRecordList);

    PageData<InspectionRecordResponse> getInspectionRecordPage(InspectionRecordRequest request);
}
