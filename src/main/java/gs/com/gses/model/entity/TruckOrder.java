package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName TruckOrder
 */
@TableName(value ="TruckOrder")
@Data
public class TruckOrder implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "SenderAddress")
    private String senderAddress;

    /**
     * 
     */
    @TableField(value = "ReceiverAddress")
    private String receiverAddress;

    /**
     * 
     */
    @TableField(value = "SenderPhone")
    private String senderPhone;

    /**
     * 
     */
    @TableField(value = "ReceiverPhone")
    private String receiverPhone;

    /**
     * 
     */
    @TableField(value = "SendTime")
    private Date sendTime;

    /**
     * 
     */
    @TableField(value = "TrunkType")
    private Integer trunkType;

    /**
     * 
     */
    @TableField(value = "DriverPhone")
    private String driverPhone;

    /**
     * 
     */
    @TableField(value = "TrunkNo")
    private String trunkNo;

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