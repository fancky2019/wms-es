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
 * @TableName Inventory
 */
@TableName(value ="Inventory")
@Data
public class Inventory implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 仓库id
     */
    @TableField(value = "Whid")
    private Long whid;

    /**
     * 位置id
     */
    @TableField(value = "LocationId")
    private Long locationId;

    /**
     * 托盘号
     */
    @TableField(value = "Pallet")
    private String pallet;

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
    @TableField(value = "QCStatus")
    private Integer QCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
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
     * 备注
     */
    @TableField(value = "Comments")
    private String comments;

    /**
     * 重量
     */
    @TableField(value = "Weight")
    private BigDecimal weight;

    /**
     * 长
     */
    @TableField(value = "Length")
    private BigDecimal length;

    /**
     * 宽
     */
    @TableField(value = "Width")
    private BigDecimal width;

    /**
     * 高
     */
    @TableField(value = "Height")
    private BigDecimal height;

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
     * 包装单位数量
     */
    @TableField(value = "PackageQuantity")
    private BigDecimal packageQuantity;

    /**
     * 最小单位数量
     */
    @TableField(value = "SmallUnitQuantity")
    private BigDecimal smallUnitQuantity;

    /**
     * 空托层数
     */
    @TableField(value = "LevelCount")
    private Integer levelCount;

    /**
     * 输送线编码
     */
    @TableField(value = "ConveyorCode")
    private String conveyorCode;

    /**
     * 理货/备货单号
     */
    @TableField(value = "ApplyOrOrderCode")
    private String applyOrOrderCode;

    /**
     * 该库存是用某辆AGVID车号执行的任务
     */
    @TableField(value = "OrginAGVID")
    private Integer orginAGVID;

    /**
     * 库存原库位，用户返回原库位使用
     */
    @TableField(value = "OrginLocationCode")
    private String orginLocationCode;

    /**
     * 托盘类型,用于设备区分
     */
    @TableField(value = "PalletType")
    private String palletType;

    /**
     * 体积
     */
    @TableField(value = "Volume")
    private String volume;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}