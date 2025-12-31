package gs.com.gses.model.response.wms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class InventoryItemDetailResponse implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 库存明细表id
     */
    private Long inventoryItemId;
    private Long inventoryId;
    private String pallet;
    /**
     * 箱号
     */
    private String carton;

    /**
     * 序列号
     */
    private String serialNo;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 批号
     */
    private String batchNo;

    /**
     * 批号2
     */
    private String batchNo2;

    /**
     * 批号3
     */
    private String batchNo3;

    /**
     * 包装单位id
     */
    private Long packageUnitId;

    /**
     * 最小单位数量
     */
    private BigDecimal smallUnitQuantity;

    /**
     * 包装单位数量
     */
    private BigDecimal packageQuantity;

    /**
     * 已分配最小单位数量
     */
    private BigDecimal allocatedSmallUnitQuantity;

    /**
     * 已分配包装单位数量
     */
    private BigDecimal allocatedPackageQuantity;

    /**
     * 质检状态（0待检，1已取样，2合格，-1不合格）
     */
    @JsonProperty("QCStatus")
    private Integer QCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
    @JsonProperty("XStatus")
    private Integer XStatus;

    /**
     * 是否任务锁定
     */
    private Boolean isLocked;

    /**
     * 是否封存
     */
    private Boolean isSealed;

    /**
     * 是否零托，散货
     */
    private Boolean isScattered;

    /**
     * 是否过期
     */
    private Boolean isExpired;

    /**
     * 过期时间时间戳
     */
    private Long expiredTime;

    /**
     * 备注
     */
    private String comments;

    //region m_Str40
    /**
     * 物料扩展属性预留字段1
     */
    private String m_Str1;

    /**
     * 物料扩展属性预留字段2
     */
    private String m_Str2;

    /**
     * 物料扩展属性预留字段3
     */
    private String m_Str3;

    /**
     * 物料扩展属性预留字段4
     */
    private String m_Str4;

    /**
     * 物料扩展属性预留字段5
     */
    private String m_Str5;

    /**
     * 物料扩展属性预留字段6
     */
    private String m_Str6;

    /**
     * 物料扩展属性预留字段7
     */
    private String m_Str7;

    /**
     * 物料扩展属性预留字段8
     */
    private String m_Str8;

    /**
     * 物料扩展属性预留字段9
     */
    private String m_Str9;

    /**
     * 物料扩展属性预留字段10
     */
    private String m_Str10;

    /**
     * 物料扩展属性预留字段11
     */
    private String m_Str11;

    /**
     * 物料扩展属性预留字段12
     */
    private String m_Str12;

    /**
     * 物料扩展属性预留字段13
     */
    private String m_Str13;

    /**
     * 物料扩展属性预留字段14
     */
    private String m_Str14;

    /**
     * 物料扩展属性预留字段15
     */
    private String m_Str15;

    /**
     * 物料扩展属性预留字段16
     */
    private String m_Str16;

    /**
     * 物料扩展属性预留字段17
     */
    private String m_Str17;

    /**
     * 物料扩展属性预留字段18
     */
    private String m_Str18;

    /**
     * 物料扩展属性预留字段19
     */
    private String m_Str19;

    /**
     * 物料扩展属性预留字段20
     */
    private String m_Str20;

    /**
     * 物料扩展属性预留字段21
     */
    private String m_Str21;

    /**
     * 物料扩展属性预留字段22
     */
    private String m_Str22;

    /**
     * 物料扩展属性预留字段23
     */
    private String m_Str23;

    /**
     * 物料扩展属性预留字段24
     */
    private String m_Str24;

    /**
     * 物料扩展属性预留字段25
     */
    private String m_Str25;

    /**
     * 物料扩展属性预留字段26
     */
    private String m_Str26;

    /**
     * 物料扩展属性预留字段27
     */
    private String m_Str27;

    /**
     * 物料扩展属性预留字段28
     */
    private String m_Str28;

    /**
     * 物料扩展属性预留字段29
     */
    private String m_Str29;

    /**
     * 物料扩展属性预留字段30
     */
    private String m_Str30;

    /**
     * 物料扩展属性预留字段31
     */
    private String m_Str31;

    /**
     * 物料扩展属性预留字段32
     */
    private String m_Str32;

    /**
     * 物料扩展属性预留字段33
     */
    private String m_Str33;

    /**
     * 物料扩展属性预留字段34
     */
    private String m_Str34;

    /**
     * 物料扩展属性预留字段35
     */
    private String m_Str35;

    /**
     * 物料扩展属性预留字段36
     */
    private String m_Str36;

    /**
     * 物料扩展属性预留字段37
     */
    private String m_Str37;

    /**
     * 物料扩展属性预留字段38
     */
    private String m_Str38;

    /**
     * 物料扩展属性预留字段39
     */
    private String m_Str39;

    /**
     * 物料扩展属性预留字段40
     */
    private String m_Str40;
    //endregion
    /**
     * 创建人ID
     */
    private Object creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 最新修改人ID
     */
    private Object lastModifierId;

    /**
     * 最新修改人名称
     */
    private String lastModifierName;

    /**
     * 创建时间戳13位
     */
    private Long creationTime;

    /**
     * 修改时间戳13位
     */
    private Long lastModificationTime;

    /**
     * 入库时间
     */
    private Long inboundTime;

    /**
     * 生产时间
     */
    private Long productTime;

    /**
     * 货载在托盘上的码放工位号
     */
    private String positionCode;

    /**
     * 货载在托盘上的码放层级
     */
    private Integer positionLevel;

    /**
     * 包装方式
     */
    private String packageMethod;

    /**
     * 禁止出库原因
     */
    private String forbidOutboundRemark;

    /**
     * 是否禁止出库
     */
    private Boolean isForbidOutbound;

    /**
     * 是否保税
     */
    private Boolean isBonded;

    /**
     * 入库日期
     */
    private LocalDateTime inboundDate;

    /**
     * 料架格口ID
     */
    private Long materialRackId;

    /**
     * 入库天数
     */
    private Integer days;

    /**
     * 图片文件地址
     */
    private String imageFile;
    private Integer version;
    private static final long serialVersionUID = 1L;

}

