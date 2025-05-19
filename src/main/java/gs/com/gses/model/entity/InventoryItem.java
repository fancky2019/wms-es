package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 
 * @TableName InventoryItem
 */
@TableName(value ="InventoryItem")
@Data
public class InventoryItem implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 库存主表id
     */
    @TableField(value = "InventoryId")
    private Long inventoryId;

    /**
     * 物料id
     */
    @TableField(value = "MaterialId")
    private Long materialId;

    /**
     * 包装单位id
     */
    @TableField(value = "PackageUnitId")
    private Long packageUnitId;

    /**
     * 最小单位数量
     */
    @TableField(value = "SmallUnitQuantity")
    private BigDecimal smallUnitQuantity;

    /**
     * 包装单位数量
     */
    @TableField(value = "PackageQuantity")
    private BigDecimal packageQuantity;

    /**
     * 已分配最小单位数量
     */
    @TableField(value = "AllocatedSmallUnitQuantity")
    private BigDecimal allocatedSmallUnitQuantity;

    /**
     * 已分配包装单位数量
     */
    @TableField(value = "AllocatedPackageQuantity")
    private BigDecimal allocatedPackageQuantity;

    /**
     * 质检状态（0待检，1已取样，2合格，-1不合格）
     */
    @JsonProperty("QCStatus")
    @TableField(value = "QCStatus")
    private Integer QCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
    @JsonProperty("XStatus")
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 是否任务锁定
     */
    @TableField(value = "IsLocked")
    private Boolean isLocked;

    /**
     * 是否封存
     */
    @TableField(value = "IsSealed")
    private Boolean isSealed;

    /**
     * 是否零托，散货
     */
    @TableField(value = "IsScattered")
    private Boolean isScattered;

    /**
     * 是否过期
     */
    @TableField(value = "IsExpired")
    private Boolean isExpired;

    /**
     * 过期时间时间戳
     */
    @TableField(value = "ExpiredTime")
    private Long expiredTime;

    /**
     * 备注
     */
    @TableField(value = "Comments")
    private String comments;

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
     * 组织（客户）
     */
    @TableField(value = "OrganiztionId")
    private Long organiztionId;

    /**
     * 组织（供应商）
     */
    @TableField(value = "OrganiztionSupplierId")
    private Long organiztionSupplierId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}