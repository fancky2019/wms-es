package gs.com.gses.service;

import com.baomidou.mybatisplus.extension.service.IService;
import gs.com.gses.model.entity.InspectionRecord;
import gs.com.gses.model.request.wms.InspectionRecordRequest;
import gs.com.gses.model.request.wms.TruckOrderItemRequest;
import gs.com.gses.model.response.PageData;
import gs.com.gses.model.response.wms.InspectionRecordResponse;

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
