package gs.com.gses.model.request.wms;

import gs.com.gses.model.request.RequestPage;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InventoryInfoRequest extends RequestPage {
    private static final long serialVersionUID = 1L;

    //region location

    private Long locationId;
    private String locationCode;

    /**
     * 状态 （0正常，-1禁用）
     */
    private Integer locationXStatus;

    /**
     * 是否任务锁定
     */
    private Boolean locationIsLocked;


    /**
     *
     */
    private Boolean forbidOutbound;

    /**
     *
     */
    private Boolean isCountLocked;
    private Integer locationXType;


    //endregion

    //region laneway

    private Long lanewayId;
    private Integer lanewayCode;
    /**
     * 状态 （0正常，-1禁用）
     */
    private Integer lanewayXStatus;

    //endregion

    //region zone

    private Long zoneId;
    private String zoneCode;
    //endregion

    //region Inventory
    private Long inventoryId;

    /**
     * 仓库id
     */
    private Long whid;
    private String whCode;
//    /**
//     * 位置id
//     */
//    private Long locationId;

    /**
     * 托盘号
     */
    private String pallet;

    /**
     * 已分配最小单位数量
     */
    private BigDecimal inventoryAllocatedSmallUnitQuantity;

    /**
     * 已分配包装单位数量
     */
    private BigDecimal inventoryAllocatedPackageQuantity;

    /**
     * 质检状态（0待检，1已取样，2合格，-1不合格）
     */
    private Integer inventoryQCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
    private Integer inventoryXStatus;

    /**
     * 是否任务锁定
     */
    private Boolean inventoryIsLocked;

    /**
     * 是否封存
     */
    private Boolean inventoryIsSealed;

    /**
     * 是否零托，散货
     */
    private Boolean inventoryIsScattered;

    /**
     * 是否过期
     */
    private Boolean inventoryIsExpired;

    /**
     * 备注
     */
    private String inventoryComments;

    /**
     * 重量
     */
    private BigDecimal weight;

    /**
     * 长
     */
    private BigDecimal length;

    /**
     * 宽
     */
    private BigDecimal width;

    /**
     * 高
     */
    private BigDecimal height;

    /**
     * 预留字段1
     */
    private String inventoryStr1;

    /**
     * 预留字段2
     */
    private String inventoryStr2;

    /**
     * 预留字段3
     */
    private String inventoryStr3;

    /**
     * 预留字段4
     */
    private String inventoryStr4;

    /**
     * 预留字段5
     */
    private String inventoryStr5;

//    /**
//     * 创建人ID
//     */
//    private Object creatorId;
//
//    /**
//     * 创建人名称
//     */
//    private String creatorName;
//
//    /**
//     * 最新修改人ID
//     */
//    private Object lastModifierId;
//
//    /**
//     * 最新修改人名称
//     */
//    private String lastModifierName;
//
//    /**
//     * 创建时间戳13位
//     */
//    private Long creationTime;

//    /**
//     * 修改时间戳13位
//     */
//    private Long lastModificationTime;

    /**
     * 包装单位数量
     */
    private BigDecimal inventoryPackageQuantity;

    /**
     * 最小单位数量
     */
    private BigDecimal inventorySmallUnitQuantity;

    /**
     * 空托层数
     */
    private Integer levelCount;

    /**
     * 输送线编码
     */
    private String conveyorCode;

    /**
     * 理货/备货单号
     */
    private String applyOrOrderCode;

    /**
     * 该库存是用某辆AGVID车号执行的任务
     */
    private Integer orginAGVID;

    /**
     * 库存原库位，用户返回原库位使用
     */
    private String orginLocationCode;

    /**
     * 托盘类型,用于设备区分
     */
    private String palletType;

    /**
     * 体积
     */
    private String volume;
    //endregion

    //region InventoryItem
    private Long inventoryItemId;


//    /**
//     * 物料id
//     */
//    private Long materialId;

//    /**
//     * 包装单位id
//     */
//    private Long packageUnitId;

//    /**
//     * 最小单位数量
//     */
//    private BigDecimal smallUnitQuantity;

    /**
     * 包装单位数量
     */
    private BigDecimal inventoryItemPackageQuantity;
//
//    /**
//     * 已分配最小单位数量
//     */
//    private BigDecimal allocatedSmallUnitQuantity;
//
    /**
     * 已分配包装单位数量
     */
    private BigDecimal inventoryItemAllocatedPackageQuantity;
//
//    /**
//     * 质检状态（0待检，1已取样，2合格，-1不合格）
//     */
//    private Integer QCStatus;
//
    /**
     * 状态 （0正常，-1禁用）
     */
    private Integer inventoryItemXStatus;
//
    /**
     * 是否任务锁定
     */
    private Boolean inventoryItemIsLocked;

    /**
     * 是否封存
     */
    private Boolean inventoryItemIsSealed;


//
//    /**
//     * 是否零托，散货
//     */
//    private Boolean isScattered;
//
    /**
     * 是否过期
     */
    private Boolean inventoryItemIsExpired;

    /**
     * 过期时间时间戳
     */
    private LocalDateTime inventoryItemExpiredTime;

    /**
     * 备注
     */
    private String inventoryItemComments;

    /**
     * 预留字段1
     */
    private String inventoryItemStr1;

    /**
     * 预留字段2
     */
    private String inventoryItemStr2;

    /**
     * 预留字段3
     */
    private String inventoryItemStr3;

    /**
     * 预留字段4
     */
    private String inventoryItemStr4;

    /**
     * 预留字段5
     */
    private String inventoryItemStr5;


    /**
     * 组织（客户）
     */
    private Long organiztionId;
    private String organiztionCode;
    /**
     * 组织（供应商）
     */
    private Long organiztionSupplierId;
    private String organiztionSupplierCode;
    //endregion

    //region InventoryItemDetail

    /**
     * #@Id es 默认 id  为keyword,es 会默认生成一个_id 字段值等于inventoryItemDetailId
     */
    private Long inventoryItemDetailId;

//脚本设置 为long
//    @Field(type = FieldType.Long)
//    private Long id;

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
    private String materialCode;
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
    private String packageUnitCode;
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
    private Integer qCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
    private Integer xStatus;

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
    private LocalDateTime expiredTime;

    /**
     * 备注
     */
    private String comments;
    private Boolean enoughPackQuantity;
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
    private LocalDateTime creationTime;

    /**
     * 修改时间戳13位
     */
    private LocalDateTime lastModificationTime;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;

    /**
     * 生产时间
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'||yyyy-MM-dd HH:mm:ss.S||yyyy-MM-dd HH:mm:ss.SS||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
     */
    private LocalDateTime productTime;

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
    //endregion

    private List<Long> materialIdList;

    private Integer deleted = 0; // 数值默认值
}
