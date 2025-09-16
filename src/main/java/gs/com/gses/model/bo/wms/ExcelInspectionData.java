package gs.com.gses.model.bo.wms;

import lombok.Data;

import java.util.List;

@Data
public class ExcelInspectionData {
    private String materialCode;
    private String projectNo;
    private String deviceNo;
    private String batchNo;
    private List<InspectionData> inspectionDataList;
    private String originalFilename;
    private String outputPath;
    private String ftpPath;

    /**
     * 不合格
     */
    private Boolean unqualified;

    private String errMsg;

    private Long matchedApplyReceiptOrderItem;

}
