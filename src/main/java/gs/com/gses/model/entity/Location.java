package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 
 * @TableName Location
 */
@TableName(value ="Location")
@Data
public class Location implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 货位代码
     */
    @TableField(value = "XCode")
    private String XCode;

    /**
     * 货位名称
     */
    @TableField(value = "XName")
    private String XName;

    /**
     * 状态（DISABLED 0禁用 ENABLED 1正常，Exception2异常，NOEXIST -1不存在）
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 货位类型 （0未知,1存储，2越库，3地面，4收货区,5月台）
     */
    @TableField(value = "XType")
    private Integer XType;

    /**
     * 巷道id
     */
    @TableField(value = "LanewayId")
    private Long lanewayId;

    /**
     * 所属货架ID
     */
    @TableField(value = "RackId")
    private Long rackId;

    /**
     * 货架编号
     */
    @TableField(value = "XRack")
    private Integer XRack;

    /**
     * 列
     */
    @TableField(value = "XColumn")
    private Integer XColumn;

    /**
     * 层
     */
    @TableField(value = "XLevel")
    private Integer XLevel;

    /**
     * 深度
     */
    @TableField(value = "XDepth")
    private Integer XDepth;

    /**
     * 最大长度
     */
    @TableField(value = "MaxLength")
    private BigDecimal maxLength;

    /**
     * 最大宽度
     */
    @TableField(value = "MaxWidth")
    private BigDecimal maxWidth;

    /**
     * 最大高度
     */
    @TableField(value = "MaxHeight")
    private BigDecimal maxHeight;

    /**
     * 最大重量
     */
    @TableField(value = "MaxWeight")
    private BigDecimal maxWeight;

    /**
     * 异常原因
     */
    @TableField(value = "ExceptionComments")
    private String exceptionComments;

    /**
     * 普通任务锁定
     */
    @TableField(value = "IsLocked")
    private Boolean isLocked;

    /**
     * 有货
     */
    @TableField(value = "IsLoaded")
    private Boolean isLoaded;

    /**
     * 盘点任务锁定
     */
    @TableField(value = "IsCountLocked")
    private Boolean isCountLocked;

    /**
     * 禁入
     */
    @TableField(value = "ForbidInbound")
    private Boolean forbidInbound;

    /**
     * 禁入备注
     */
    @TableField(value = "ForbidInboundComments")
    private String forbidInboundComments;

    /**
     * 禁出
     */
    @TableField(value = "ForbidOutbound")
    private Boolean forbidOutbound;

    /**
     * 禁出备注
     */
    @TableField(value = "ForbidOutboundComments")
    private String forbidOutboundComments;

    /**
     * 预留字段1
     */
    @TableField(value = "Str1")
    private String str1;

    /**
     * 预留字段2
     */
    @TableField(value = "Str2")
    private String str2;

    /**
     * 预留字段3
     */
    @TableField(value = "Str3")
    private String str3;

    /**
     * 预留字段4
     */
    @TableField(value = "Str4")
    private String str4;

    /**
     * 预留字段5
     */
    @TableField(value = "Str5")
    private String str5;

    /**
     * 创建人ID
     */
    @TableField(value = "CreatorId")
    private Object creatorId;

    /**
     * 创建人名称
     */
    @TableField(value = "CreatorName")
    private String creatorName;

    /**
     * 最新修改人ID
     */
    @TableField(value = "LastModifierId")
    private Object lastModifierId;

    /**
     * 最新修改人名称
     */
    @TableField(value = "LastModifierName")
    private String lastModifierName;

    /**
     * 创建时间戳13位
     */
    @TableField(value = "CreationTime")
    private Long creationTime;

    /**
     * 修改时间戳13位
     */
    @TableField(value = "LastModificationTime")
    private Long lastModificationTime;

    /**
     * 
     */
    @TableField(value = "ColoumGroup")
    private Long coloumGroup;

    /**
     * 
     */
    @TableField(value = "GroupMaxLength")
    private BigDecimal groupMaxLength;

    /**
     * 
     */
    @TableField(value = "GroupMaxWeight")
    private BigDecimal groupMaxWeight;

    /**
     * 
     */
    @TableField(value = "GroupMaxWidth")
    private BigDecimal groupMaxWidth;

    /**
     * 
     */
    @TableField(value = "RackGroup")
    private Long rackGroup;

    /**
     * 
     */
    @TableField(value = "PickMode")
    private Integer pickMode;

    /**
     * 
     */
    @TableField(value = "ShipMode")
    private Integer shipMode;

    /**
     * 
     */
    @TableField(value = "WorkOrderSendCounts")
    private Integer workOrderSendCounts;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}