package gs.com.gses.model.request.wms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import gs.com.gses.model.request.RequestPage;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
    private String certificationFilePath;
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于 Spring 接收前端传入的字符串日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 用于返回给前端时格式化 JSON 输出
    private String lastModifierName;

    /**
     *
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 用于 Spring 接收前端传入的字符串日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  // 用于返回给前端时格式化 JSON 输出
    private LocalDateTime creationTime;

    /**
     *
     */
    private LocalDateTime lastModificationTime;
}
