package gs.com.gses.model.request.wms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionRecordRequest extends RequestPage {
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
