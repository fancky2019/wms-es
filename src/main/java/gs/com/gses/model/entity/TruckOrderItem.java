package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName TruckOrderItem
 */
@TableName(value ="TruckOrderItem")
@Data
public class TruckOrderItem implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "TruckOrderId")
    private Long truckOrderId;

    /**
     * 
     */
    @TableField(value = "ProjectNo")
    private String projectNo;

    /**
     * 
     */
    @TableField(value = "ProjectName")
    private String projectName;

    /**
     * 
     */
    @TableField(value = "ApplyShipOrderCode")
    private String applyShipOrderCode;

    /**
     * 
     */
    @TableField(value = "ShipOrderId")
    private Long shipOrderId;

    /**
     * 
     */
    @TableField(value = "ShipOrderItemId")
    private Long shipOrderItemId;

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
    @TableField(value = "DeviceName")
    private String deviceName;

    /**
     * 
     */
    @TableField(value = "DeviceNo")
    private String deviceNo;

    /**
     * 
     */
    @TableField(value = "DriverPhone")
    private String driverPhone;

    /**
     * 
     */
    @TableField(value = "Quantity")
    private BigDecimal quantity;

    /**
     * 
     */
    @TableField(value = "SendBatchNo")
    private String sendBatchNo;

    /**
     * 
     */
    @TableField(value = "Remark")
    private String remark;

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
    private Date creationTime;

    /**
     * 
     */
    @TableField(value = "LastModificationTime")
    private Date lastModificationTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}