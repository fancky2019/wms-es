package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 
 * @TableName InspectionRecord
 */
@TableName(value ="InspectionRecord")
@Data
public class InspectionRecord {
    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "ApplyReceiptOrderId")
    private Long applyReceiptOrderId;

    /**
     * 
     */
    @TableField(value = "ApplyReceiptOrderCode")
    private String applyReceiptOrderCode;

    /**
     * 
     */
    @TableField(value = "ApplyReceiptOrderItemId")
    private Long applyReceiptOrderItemId;

    /**
     * 
     */
    @TableField(value = "ApplyReceiptOrderItemRowNo")
    private Integer applyReceiptOrderItemRowNo;

    /**
     * 
     */
    @TableField(value = "MaterialId")
    private Long materialId;

    /**
     * 
     */
    @TableField(value = "MaterialCode")
    private String materialCode;

    /**
     * 
     */
    @TableField(value = "ProjectNo")
    private String projectNo;

    /**
     * 
     */
    @TableField(value = "DeviceNo")
    private String deviceNo;

    /**
     * 
     */
    @TableField(value = "BatchNo")
    private String batchNo;

    /**
     * 
     */
    @TableField(value = "InspectionResult")
    private String inspectionResult;

    /**
     * 
     */
    @TableField(value = "FilePath")
    private String filePath;

    @TableField(value = "PurchaseOrder")
    private String purchaseOrder;

    /**
     * 
     */
    @TableField(value = "Deleted")
    private Integer deleted;

    /**
     * 
     */
    @TableField(value = "Version")
    private Integer version;

    /**
     * 
     */
    @TableField(value = "CreatorId")
    private String creatorId;

    /**
     * 
     */
    @TableField(value = "CreatorName")
    private String creatorName;

    /**
     * 
     */
    @TableField(value = "LastModifierId")
    private String lastModifierId;

    /**
     * 
     */
    @TableField(value = "LastModifierName")
    private String lastModifierName;

    /**
     * 
     */
    @TableField(value = "CreationTime")
    private LocalDateTime creationTime;

    /**
     * 
     */
    @TableField(value = "LastModificationTime")
    private LocalDateTime lastModificationTime;
}