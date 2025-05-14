package gs.com.gses.model.elasticsearch;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "inventory_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryInfo {

    //不加field 注解， @Transient 创建索引时候不会在mapping 中创建该字段
    @Transient
    private final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'||yyyy-MM-dd HH:mm:ss.S||yyyy-MM-dd HH:mm:ss.SS||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis";

    //region location
    @Field(type = FieldType.Long)
    private Long locationId;
    @Field(type = FieldType.Keyword)
//    es 默认null 不创建索引，会创建mapping
//    @Field(type = FieldType.Text, nullValue = "NULL_PLACEHOLDER")
    private String locationCode;

    /**
     * 状态 （0正常，-1禁用）
     */
    @Field(type = FieldType.Short)
    private Integer locationXStatus;

    /**
     * 是否任务锁定
     */
    @Field(type = FieldType.Boolean)
    private Boolean locationIsLocked;


    /**
     *
     */
    @Field(type = FieldType.Boolean)
    private Boolean forbidOutbound;

    /**
     *
     */
    @Field(type = FieldType.Boolean)
    private Boolean isCountLocked;
    @Field(type = FieldType.Short)
    private Integer locationXType;

    //endregion

    //region laneway
    @Field(type = FieldType.Long)
    private Long lanewayId;
    @Field(type = FieldType.Keyword)
    private Integer lanewayCode;
    /**
     * 状态 （0正常，-1禁用）
     */
    @Field(type = FieldType.Short)
    private Integer lanewayXStatus;
    //endregion

    //region zone
    @Field(type = FieldType.Long)
    private Long zoneId;
    @Field(type = FieldType.Keyword)
    private String zoneCode;
    //endregion

    //region Inventory
    @Field(type = FieldType.Long)
    private Long inventoryId;

    /**
     * 仓库id
     */
    @Field(type = FieldType.Long)
    private Long whid;
    @Field(type = FieldType.Keyword)
    private String whCode;
//    /**
//     * 位置id
//     */
//    private Long locationId;

    /**
     * 托盘号
     */
    @Field(type = FieldType.Keyword)
    private String pallet;

    /**
     * 已分配最小单位数量
     */
    @Field(type = FieldType.Double)
    private BigDecimal inventoryAllocatedSmallUnitQuantity;

    /**
     * 已分配包装单位数量
     */
    @Field(type = FieldType.Double)
    private BigDecimal inventoryAllocatedPackageQuantity;

    /**
     * 质检状态（0待检，1已取样，2合格，-1不合格）
     */
    @Field(type = FieldType.Short)
    private Integer inventoryQCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
    @Field(type = FieldType.Short)
    private Integer inventoryXStatus;

    /**
     * 是否任务锁定
     */
    @Field(type = FieldType.Boolean)
    private Boolean inventoryIsLocked;

    /**
     * 是否封存
     */
    @Field(type = FieldType.Boolean)
    private Boolean inventoryIsSealed;

    /**
     * 是否零托，散货
     */
    @Field(type = FieldType.Boolean)
    private Boolean inventoryIsScattered;

    /**
     * 是否过期
     */
    @Field(type = FieldType.Boolean)
    private Boolean inventoryIsExpired;

    /**
     * 备注
     */
    @Field(type = FieldType.Text)
    private String inventoryComments;

    /**
     * 重量
     */
    @Field(type = FieldType.Double)
    private BigDecimal weight;

    /**
     * 长
     */
    @Field(type = FieldType.Double)
    private BigDecimal length;

    /**
     * 宽
     */
    @Field(type = FieldType.Double)
    private BigDecimal width;

    /**
     * 高
     */
    @Field(type = FieldType.Double)
    private BigDecimal height;

    /**
     * 预留字段1
     */
    @Field(type = FieldType.Text)
    private String inventoryStr1;

    /**
     * 预留字段2
     */
    @Field(type = FieldType.Text)
    private String inventoryStr2;

    /**
     * 预留字段3
     */
    @Field(type = FieldType.Text)
    private String inventoryStr3;

    /**
     * 预留字段4
     */
    @Field(type = FieldType.Text)
    private String inventoryStr4;

    /**
     * 预留字段5
     */
    @Field(type = FieldType.Text)
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
    @Field(type = FieldType.Double)
    private BigDecimal inventoryPackageQuantity;

    /**
     * 最小单位数量
     */
    @Field(type = FieldType.Double)
    private BigDecimal inventorySmallUnitQuantity;

    /**
     * 空托层数
     */
    @Field(type = FieldType.Integer)
    private Integer levelCount;

    /**
     * 输送线编码
     */
    @Field(type = FieldType.Keyword)
    private String conveyorCode;

    /**
     * 理货/备货单号
     */
    @Field(type = FieldType.Keyword)
    private String applyOrOrderCode;

    /**
     * 该库存是用某辆AGVID车号执行的任务
     */
    @Field(type = FieldType.Integer)
    private Integer orginAGVID;

    /**
     * 库存原库位，用户返回原库位使用
     */
    @Field(type = FieldType.Keyword)
    private String orginLocationCode;

    /**
     * 托盘类型,用于设备区分
     */
    @Field(type = FieldType.Keyword)
    private String palletType;

    /**
     * 体积
     */
    @Field(type = FieldType.Keyword)
    private String volume;
    //endregion

    //region InventoryItem
    @Field(type = FieldType.Long)
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

//    /**
//     * 包装单位数量
//     */
//    private BigDecimal packageQuantity;
//
//    /**
//     * 已分配最小单位数量
//     */
//    private BigDecimal allocatedSmallUnitQuantity;
//
    /**
     * 已分配包装单位数量
     */
    @Field(type = FieldType.Double)
    private BigDecimal inventoryItemPackageQuantity;

    @Field(type = FieldType.Double)
    private BigDecimal inventoryItemAllocatedPackageQuantity;
//
//    /**
//     * 质检状态（0待检，1已取样，2合格，-1不合格）
//     */
//    private Integer QCStatus;
//
//    /**
//     * 状态 （0正常，-1禁用）
//     */
//    private Integer XStatus;
//
//    /**
//     * 是否任务锁定
//     */
//    private Boolean isLocked;
//
//    /**
//     * 是否封存
//     */
//    private Boolean isSealed;
//
//    /**
//     * 是否零托，散货
//     */
//    private Boolean isScattered;
//
//    /**
//     * 是否过期
//     */
//    private Boolean isExpired;

    @Field(type = FieldType.Boolean)
    private Boolean inventoryItemIsLocked;

    /**
     * 状态 （0正常，-1禁用）
     */
    @Field(type = FieldType.Short)
    private Integer inventoryItemXStatus;
    @Field(type = FieldType.Boolean)
    private Boolean inventoryItemIsExpired;
    /**
     * 过期时间时间戳
     */
    @Field(name = "inventoryItemExpiredTime", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime inventoryItemExpiredTime;

    /**
     * 备注
     */
    @Field(type = FieldType.Text)
    private String inventoryItemComments;

    /**
     * 预留字段1
     */
    @Field(type = FieldType.Text)
    private String inventoryItemStr1;

    /**
     * 预留字段2
     */
    @Field(type = FieldType.Text)
    private String inventoryItemStr2;

    /**
     * 预留字段3
     */
    @Field(type = FieldType.Text)
    private String inventoryItemStr3;

    /**
     * 预留字段4
     */
    @Field(type = FieldType.Text)
    private String inventoryItemStr4;

    /**
     * 预留字段5
     */
    @Field(type = FieldType.Text)
    private String inventoryItemStr5;


    /**
     * 组织（客户）
     */
    @Field(type = FieldType.Long)
    private Long organiztionId;
    @Field(type = FieldType.Keyword)
    private String organiztionCode;
    /**
     * 组织（供应商）
     */
    @Field(type = FieldType.Long)
    private Long organiztionSupplierId;
    @Field(type = FieldType.Keyword)
    private String organiztionSupplierCode;
    //endregion

    //region InventoryItemDetail

    /**
     * #@Id es 默认 id  为keyword,es 会默认生成一个_id 字段值等于inventoryItemDetailId
     */
    @Id
    @Field(type = FieldType.Long)
    private Long inventoryItemDetailId;

//脚本设置 为long
//    @Field(type = FieldType.Long)
//    private Long id;

    /**
     * 箱号
     */
    @Field(type = FieldType.Keyword)
    private String carton;

    /**
     * 序列号
     */
    @Field(type = FieldType.Keyword)
    private String serialNo;

    /**
     * 物料id
     */
    @Field(type = FieldType.Long)
    private Long materialId;
    @Field(type = FieldType.Keyword)
    private String materialCode;
    /**
     * 批号
     */
    @Field(type = FieldType.Keyword)
    private String batchNo;

    /**
     * 批号2
     */
    @Field(type = FieldType.Keyword)
    private String batchNo2;

    /**
     * 批号3
     */
    @Field(type = FieldType.Keyword)
    private String batchNo3;

    /**
     * 包装单位id
     */
    @Field(type = FieldType.Long)
    private Long packageUnitId;
    @Field(type = FieldType.Keyword)
    private String packageUnitCode;
    /**
     * 最小单位数量
     */
    @Field(type = FieldType.Double)
    private BigDecimal smallUnitQuantity;

    /**
     * 包装单位数量
     */
    @Field(type = FieldType.Double)
    private BigDecimal packageQuantity;

    /**
     * 已分配最小单位数量
     */
    @Field(type = FieldType.Double)
    private BigDecimal allocatedSmallUnitQuantity;

    /**
     * 已分配包装单位数量
     */
    @Field(type = FieldType.Double)
    private BigDecimal allocatedPackageQuantity;

    /**
     * 质检状态（0待检，1已取样，2合格，-1不合格）
     */
    @Field(type = FieldType.Short)
    private Integer qCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
    @Field(type = FieldType.Short)
    private Integer xStatus;

    /**
     * 是否任务锁定
     */
    @Field(type = FieldType.Boolean)
    private Boolean isLocked;

    /**
     * 是否封存
     */
    @Field(type = FieldType.Boolean)
    private Boolean isSealed;

    /**
     * 是否零托，散货
     */
    @Field(type = FieldType.Boolean)
    private Boolean isScattered;

    /**
     * 是否过期
     */
    @Field(type = FieldType.Boolean)
    private Boolean isExpired;

    /**
     * 过期时间时间戳
     */
    @Field(name = "expiredTime", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime expiredTime;

    /**
     * 备注
     */
    @Field(type = FieldType.Text)
    private String comments;

    /**
     * 物料扩展属性预留字段1
     */
    @Field(type = FieldType.Text)
    private String m_Str1;

    /**
     * 物料扩展属性预留字段2
     */
    @Field(type = FieldType.Text)
    private String m_Str2;

    /**
     * 物料扩展属性预留字段3
     */
    @Field(type = FieldType.Text)
    private String m_Str3;

    /**
     * 物料扩展属性预留字段4
     */
    @Field(type = FieldType.Text)
    private String m_Str4;

    /**
     * 物料扩展属性预留字段5
     */
    @Field(type = FieldType.Text)
    private String m_Str5;

    /**
     * 物料扩展属性预留字段6
     */
    @Field(type = FieldType.Text)
    private String m_Str6;

    /**
     * 物料扩展属性预留字段7
     */
    @Field(type = FieldType.Text)
    private String m_Str7;

    /**
     * 物料扩展属性预留字段8
     */
    @Field(type = FieldType.Text)
    private String m_Str8;

    /**
     * 物料扩展属性预留字段9
     */
    @Field(type = FieldType.Text)
    private String m_Str9;

    /**
     * 物料扩展属性预留字段10
     */
    @Field(type = FieldType.Text)
    private String m_Str10;

    /**
     * 物料扩展属性预留字段11
     */
    @Field(type = FieldType.Text)
    private String m_Str11;

    /**
     * 物料扩展属性预留字段12
     */
    @Field(type = FieldType.Text)
    private String m_Str12;

    /**
     * 物料扩展属性预留字段13
     */
    @Field(type = FieldType.Text)
    private String m_Str13;

    /**
     * 物料扩展属性预留字段14
     */
    @Field(type = FieldType.Text)
    private String m_Str14;

    /**
     * 物料扩展属性预留字段15
     */
    @Field(type = FieldType.Text)
    private String m_Str15;

    /**
     * 物料扩展属性预留字段16
     */
    @Field(type = FieldType.Text)
    private String m_Str16;

    /**
     * 物料扩展属性预留字段17
     */
    @Field(type = FieldType.Text)
    private String m_Str17;

    /**
     * 物料扩展属性预留字段18
     */
    @Field(type = FieldType.Text)
    private String m_Str18;

    /**
     * 物料扩展属性预留字段19
     */
    @Field(type = FieldType.Text)
    private String m_Str19;

    /**
     * 物料扩展属性预留字段20
     */
    @Field(type = FieldType.Text)
    private String m_Str20;

    /**
     * 物料扩展属性预留字段21
     */
    @Field(type = FieldType.Text)
    private String m_Str21;

    /**
     * 物料扩展属性预留字段22
     */
    @Field(type = FieldType.Text)
    private String m_Str22;

    /**
     * 物料扩展属性预留字段23
     */
    @Field(type = FieldType.Text)
    private String m_Str23;

    /**
     * 物料扩展属性预留字段24
     */
    @Field(type = FieldType.Text)
    private String m_Str24;

    /**
     * 物料扩展属性预留字段25
     */
    @Field(type = FieldType.Text)
    private String m_Str25;

    /**
     * 物料扩展属性预留字段26
     */
    @Field(type = FieldType.Text)
    private String m_Str26;

    /**
     * 物料扩展属性预留字段27
     */
    @Field(type = FieldType.Text)
    private String m_Str27;

    /**
     * 物料扩展属性预留字段28
     */
    @Field(type = FieldType.Text)
    private String m_Str28;

    /**
     * 物料扩展属性预留字段29
     */
    @Field(type = FieldType.Text)
    private String m_Str29;

    /**
     * 物料扩展属性预留字段30
     */
    @Field(type = FieldType.Text)
    private String m_Str30;

    /**
     * 物料扩展属性预留字段31
     */
    @Field(type = FieldType.Text)
    private String m_Str31;

    /**
     * 物料扩展属性预留字段32
     */
    @Field(type = FieldType.Text)
    private String m_Str32;

    /**
     * 物料扩展属性预留字段33
     */
    @Field(type = FieldType.Text)
    private String m_Str33;

    /**
     * 物料扩展属性预留字段34
     */
    @Field(type = FieldType.Text)
    private String m_Str34;

    /**
     * 物料扩展属性预留字段35
     */
    @Field(type = FieldType.Text)
    private String m_Str35;

    /**
     * 物料扩展属性预留字段36
     */
    @Field(type = FieldType.Text)
    private String m_Str36;

    /**
     * 物料扩展属性预留字段37
     */
    @Field(type = FieldType.Text)
    private String m_Str37;

    /**
     * 物料扩展属性预留字段38
     */
    @Field(type = FieldType.Text)
    private String m_Str38;

    /**
     * 物料扩展属性预留字段39
     */
    @Field(type = FieldType.Text)
    private String m_Str39;

    /**
     * 物料扩展属性预留字段40
     */
    @Field(type = FieldType.Text)
    private String m_Str40;

    /**
     * 创建人ID
     */
    @Field(type = FieldType.Text)
    private Object creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 最新修改人ID
     */
    @Field(type = FieldType.Text)
    private Object lastModifierId;

    /**
     * 最新修改人名称
     */
    @Field(type = FieldType.Text)
    private String lastModifierName;

    /**
     * 创建时间戳13位
     */
    @Field(name = "creationTime", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime creationTime;

    /**
     * 修改时间戳13位
     */
    @Field(name = "lastModificationTime", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime lastModificationTime;

    /**
     * 入库时间
     */
    @Field(index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime inboundTime;

    /**
     * 生产时间
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'||yyyy-MM-dd HH:mm:ss.S||yyyy-MM-dd HH:mm:ss.SS||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
     */
    @Field(index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime productTime;

    /**
     * 货载在托盘上的码放工位号
     */
    @Field(type = FieldType.Keyword)
    private String positionCode;

    /**
     * 货载在托盘上的码放层级
     */
    @Field(type = FieldType.Short)
    private Integer positionLevel;

    /**
     * 包装方式
     */
    @Field(type = FieldType.Keyword)
    private String packageMethod;
    //endregion
}
