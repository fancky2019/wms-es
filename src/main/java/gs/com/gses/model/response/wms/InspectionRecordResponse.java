package gs.com.gses.model.response.wms;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionRecordResponse {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private Long applyReceiptOrderId;

    /**
     *
     */
    private String applyReceiptOrderCode;

    /**
     *
     */
    private Long applyReceiptOrderItemId;

    /**
     *
     */
    private Integer applyReceiptOrderItemRowNo;

    /**
     *
     */
    private Long materialId;

    /**
     *
     */
    private String materialCode;

    /**
     *
     */
    private String projectNo;

    /**
     *
     */
    private String deviceNo;

    /**
     *
     */
    private String batchNo;

    /**
     *
     */
    private String inspectionResult;

    /**
     *
     */
    private String filePath;

    /**
     *
     */
    private Integer deleted;

    /**
     *
     */
    private Integer version;

    /**
     *
     */
    private String creatorId;

    /**
     *
     */
    private String creatorName;

    /**
     *
     */
    private String lastModifierId;

    /**
     *
     */
    private String lastModifierName;

    /**
     *
     */
    private LocalDateTime creationTime;

    /**
     *
     */
    private LocalDateTime lastModificationTime;
}
