package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @TableName InventoryItemDetail
 */
@TableName(value ="InventoryItemDetail")
@Data
public class InventoryItemDetail implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 库存明细表id
     */
    @TableField(value = "InventoryItemId")
    private Long inventoryItemId;

    /**
     * 箱号
     */
    @TableField(value = "Carton")
    private String carton;

    /**
     * 序列号
     */
    @TableField(value = "SerialNo")
    private String serialNo;

    /**
     * 物料id
     */
    @TableField(value = "MaterialId")
    private Long materialId;

    /**
     * 批号
     */
    @TableField(value = "BatchNo")
    private String batchNo;

    /**
     * 批号2
     */
    @TableField(value = "BatchNo2")
    private String batchNo2;

    /**
     * 批号3
     */
    @TableField(value = "BatchNo3")
    private String batchNo3;

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
     * 物料扩展属性预留字段1
     */
    @TableField(value = "M_Str1")
    private String m_Str1;

    /**
     * 物料扩展属性预留字段2
     */
    @TableField(value = "M_Str2")
    private String m_Str2;

    /**
     * 物料扩展属性预留字段3
     */
    @TableField(value = "M_Str3")
    private String m_Str3;

    /**
     * 物料扩展属性预留字段4
     */
    @TableField(value = "M_Str4")
    private String m_Str4;

    /**
     * 物料扩展属性预留字段5
     */
    @TableField(value = "M_Str5")
    private String m_Str5;

    /**
     * 物料扩展属性预留字段6
     */
    @TableField(value = "M_Str6")
    private String m_Str6;

    /**
     * 物料扩展属性预留字段7
     */
    @TableField(value = "M_Str7")
    private String m_Str7;

    /**
     * 物料扩展属性预留字段8
     */
    @TableField(value = "M_Str8")
    private String m_Str8;

    /**
     * 物料扩展属性预留字段9
     */
    @TableField(value = "M_Str9")
    private String m_Str9;

    /**
     * 物料扩展属性预留字段10
     */
    @TableField(value = "M_Str10")
    private String m_Str10;

    /**
     * 物料扩展属性预留字段11
     */
    @TableField(value = "M_Str11")
    private String m_Str11;

    /**
     * 物料扩展属性预留字段12
     */
    @TableField(value = "M_Str12")
    private String m_Str12;

    /**
     * 物料扩展属性预留字段13
     */
    @TableField(value = "M_Str13")
    private String m_Str13;

    /**
     * 物料扩展属性预留字段14
     */
    @TableField(value = "M_Str14")
    private String m_Str14;

    /**
     * 物料扩展属性预留字段15
     */
    @TableField(value = "M_Str15")
    private String m_Str15;

    /**
     * 物料扩展属性预留字段16
     */
    @TableField(value = "M_Str16")
    private String m_Str16;

    /**
     * 物料扩展属性预留字段17
     */
    @TableField(value = "M_Str17")
    private String m_Str17;

    /**
     * 物料扩展属性预留字段18
     */
    @TableField(value = "M_Str18")
    private String m_Str18;

    /**
     * 物料扩展属性预留字段19
     */
    @TableField(value = "M_Str19")
    private String m_Str19;

    /**
     * 物料扩展属性预留字段20
     */
    @TableField(value = "M_Str20")
    private String m_Str20;

    /**
     * 物料扩展属性预留字段21
     */
    @TableField(value = "M_Str21")
    private String m_Str21;

    /**
     * 物料扩展属性预留字段22
     */
    @TableField(value = "M_Str22")
    private String m_Str22;

    /**
     * 物料扩展属性预留字段23
     */
    @TableField(value = "M_Str23")
    private String m_Str23;

    /**
     * 物料扩展属性预留字段24
     */
    @TableField(value = "M_Str24")
    private String m_Str24;

    /**
     * 物料扩展属性预留字段25
     */
    @TableField(value = "M_Str25")
    private String m_Str25;

    /**
     * 物料扩展属性预留字段26
     */
    @TableField(value = "M_Str26")
    private String m_Str26;

    /**
     * 物料扩展属性预留字段27
     */
    @TableField(value = "M_Str27")
    private String m_Str27;

    /**
     * 物料扩展属性预留字段28
     */
    @TableField(value = "M_Str28")
    private String m_Str28;

    /**
     * 物料扩展属性预留字段29
     */
    @TableField(value = "M_Str29")
    private String m_Str29;

    /**
     * 物料扩展属性预留字段30
     */
    @TableField(value = "M_Str30")
    private String m_Str30;

    /**
     * 物料扩展属性预留字段31
     */
    @TableField(value = "M_Str31")
    private String m_Str31;

    /**
     * 物料扩展属性预留字段32
     */
    @TableField(value = "M_Str32")
    private String m_Str32;

    /**
     * 物料扩展属性预留字段33
     */
    @TableField(value = "M_Str33")
    private String m_Str33;

    /**
     * 物料扩展属性预留字段34
     */
    @TableField(value = "M_Str34")
    private String m_Str34;

    /**
     * 物料扩展属性预留字段35
     */
    @TableField(value = "M_Str35")
    private String m_Str35;

    /**
     * 物料扩展属性预留字段36
     */
    @TableField(value = "M_Str36")
    private String m_Str36;

    /**
     * 物料扩展属性预留字段37
     */
    @TableField(value = "M_Str37")
    private String m_Str37;

    /**
     * 物料扩展属性预留字段38
     */
    @TableField(value = "M_Str38")
    private String m_Str38;

    /**
     * 物料扩展属性预留字段39
     */
    @TableField(value = "M_Str39")
    private String m_Str39;

    /**
     * 物料扩展属性预留字段40
     */
    @TableField(value = "M_Str40")
    private String m_Str40;

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
     * 入库时间
     */
    @TableField(value = "InboundTime")
    private Long inboundTime;

    /**
     * 生产时间
     */
    @TableField(value = "ProductTime")
    private Long productTime;

    /**
     * 货载在托盘上的码放工位号
     */
    @TableField(value = "PositionCode")
    private String positionCode;

    /**
     * 货载在托盘上的码放层级
     */
    @TableField(value = "PositionLevel")
    private Integer positionLevel;

    /**
     * 包装方式
     */
    @TableField(value = "PackageMethod")
    private String packageMethod;

    /**
     * 禁止出库原因
     */
    @TableField(value = "ForbidOutboundRemark")
    private String forbidOutboundRemark;

    /**
     * 是否禁止出库
     */
    @TableField(value = "IsForbidOutbound")
    private Boolean isForbidOutbound;

    /**
     * 是否保税
     */
    @TableField(value = "IsBonded")
    private Boolean isBonded;

    /**
     * 入库日期
     */
    @TableField(value = "InboundDate")
    private Date inboundDate;

    /**
     * 料架格口ID
     */
    @TableField(value = "MaterialRackId")
    private Long materialRackId;

    /**
     * 入库天数
     */
    @TableField(value = "Days")
    private Integer days;

    /**
     * 图片文件地址
     */
    @TableField(value = "ImageFile")
    private String imageFile;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}