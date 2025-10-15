package gs.com.gses.model.elasticsearch;


import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
//@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryInfo {
//    @ExcelProperty({"${productStyle}"}) //模板动态替换列名
////    @ExcelProperty(value = "产品型号")
//    @ExcelProperty(value = "图片路径")//指定列名





    //不加field 注解， @Transient 创建索引时候不会在mapping 中创建该字段
    @Transient
    @JsonIgnore  //jackson 不序列化 反序列化
    private final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'||yyyy-MM-dd HH:mm:ss.S||yyyy-MM-dd HH:mm:ss.SS||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis";

    //region location
    @ExcelProperty(value = "locationId")
    @Field(type = FieldType.Long)
    private Long locationId;
    @ExcelProperty(value = "locationCode")
    @Field(type = FieldType.Keyword)
//    es 默认null 不创建索引，会创建mapping
//    @Field(type = FieldType.Text, nullValue = "NULL_PLACEHOLDER")
    private String locationCode;

    /**
     * 状态 （0正常，-1禁用）
     */
    @ExcelProperty(value = "locationXStatus")
    @Field(type = FieldType.Short)
    private Integer locationXStatus;

    /**
     * 是否任务锁定
     */
    @ExcelProperty(value = "locationIsLocked")
    @Field(type = FieldType.Boolean)
    private Boolean locationIsLocked;


    /**
     *
     */
    @ExcelProperty(value = "forbidOutbound")
    @Field(type = FieldType.Boolean)
    private Boolean forbidOutbound;

    /**
     *
     */
    @ExcelProperty(value = "isCountLocked")
    @Field(type = FieldType.Boolean)
    private Boolean isCountLocked;
    @ExcelProperty(value = "locationXType")
    @Field(type = FieldType.Short)
    private Integer locationXType;

    //endregion

    //region laneway
    @ExcelProperty(value = "lanewayId")
    @Field(type = FieldType.Long)
    private Long lanewayId;
    @ExcelProperty(value = "lanewayCode")
    @Field(type = FieldType.Keyword)
    private Integer lanewayCode;
    /**
     * 状态 （0正常，-1禁用）
     */
    @ExcelProperty(value = "locationXStatus")
    @Field(type = FieldType.Short)
    private Integer lanewayXStatus;
    //endregion

    //region zone
    @ExcelProperty(value = "zoneId")
    @Field(type = FieldType.Long)
    private Long zoneId;
    @ExcelProperty(value = "zoneCode")
    @Field(type = FieldType.Keyword)
    private String zoneCode;
    //endregion

    //region Inventory
    @ExcelProperty(value = "inventoryId")
    @Field(type = FieldType.Long)
    private Long inventoryId;

    /**
     * 仓库id
     */
    @ExcelProperty(value = "whid")
    @Field(type = FieldType.Long)
    private Long whid;
    @ExcelProperty(value = "whCode")
    @Field(type = FieldType.Keyword)
    private String whCode;
//    /**
//     * 位置id
//     */
//    private Long locationId;

    /**
     * 托盘号
     */
    @ExcelProperty(value = "pallet")
    @Field(type = FieldType.Keyword)
    private String pallet;

    /**
     * 已分配最小单位数量
     */
    @ExcelProperty(value = "inventoryAllocatedSmallUnitQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal inventoryAllocatedSmallUnitQuantity;

    /**
     * 已分配包装单位数量
     */
    @ExcelProperty(value = "inventoryAllocatedPackageQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal inventoryAllocatedPackageQuantity;

    /**
     * 质检状态（0待检，1已取样，2合格，-1不合格）
     */
    @ExcelProperty(value = "inventoryQCStatus")
    @Field(type = FieldType.Short)
    private Integer inventoryQCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
    @ExcelProperty(value = "inventoryXStatus")
    @Field(type = FieldType.Short)
    private Integer inventoryXStatus;

    /**
     * 是否任务锁定
     */
    @ExcelProperty(value = "inventoryIsLocked")
    @Field(type = FieldType.Boolean)
    private Boolean inventoryIsLocked;

    /**
     * 是否封存
     */
    @ExcelProperty(value = "inventoryIsSealed")
    @Field(type = FieldType.Boolean)
    private Boolean inventoryIsSealed;

    /**
     * 是否零托，散货
     */
    @ExcelProperty(value = "inventoryIsScattered")
    @Field(type = FieldType.Boolean)
    private Boolean inventoryIsScattered;

    /**
     * 是否过期
     */
    @ExcelProperty(value = "inventoryIsExpired")
    @Field(type = FieldType.Boolean)
    private Boolean inventoryIsExpired;

    /**
     * 备注
     */
    @ExcelProperty(value = "inventoryComments")
    @Field(type = FieldType.Text)
    private String inventoryComments;

    /**
     * 重量
     */
    @ExcelProperty(value = "weight")
    @Field(type = FieldType.Double)
    private BigDecimal weight;

    /**
     * 长
     */
    @ExcelProperty(value = "length")
    @Field(type = FieldType.Double)
    private BigDecimal length;

    /**
     * 宽
     */
    @ExcelProperty(value = "width")
    @Field(type = FieldType.Double)
    private BigDecimal width;

    /**
     * 高
     */
    @ExcelProperty(value = "height")
    @Field(type = FieldType.Double)
    private BigDecimal height;

    /**
     * 预留字段1
     */
    @ExcelProperty(value = "inventoryStr1")
    @Field(type = FieldType.Text)
    private String inventoryStr1;

    /**
     * 预留字段2
     */
    @ExcelProperty(value = "inventoryStr2")
    @Field(type = FieldType.Text)
    private String inventoryStr2;

    /**
     * 预留字段3
     */
    @ExcelProperty(value = "inventoryStr3")
    @Field(type = FieldType.Text)
    private String inventoryStr3;

    /**
     * 预留字段4
     */
    @ExcelProperty(value = "inventoryStr4")
    @Field(type = FieldType.Text)
    private String inventoryStr4;

    /**
     * 预留字段5
     */
    @ExcelProperty(value = "inventoryStr5")
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
    @ExcelProperty(value = "inventoryPackageQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal inventoryPackageQuantity;

    /**
     * 最小单位数量
     */
    @ExcelProperty(value = "inventorySmallUnitQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal inventorySmallUnitQuantity;

    /**
     * 空托层数
     */
    @ExcelProperty(value = "levelCount")
    @Field(type = FieldType.Integer)
    private Integer levelCount;

    /**
     * 输送线编码
     */
    @ExcelProperty(value = "conveyorCode")
    @Field(type = FieldType.Keyword)
    private String conveyorCode;

    /**
     * 理货/备货单号
     */
    @ExcelProperty(value = "applyOrOrderCode")
    @Field(type = FieldType.Keyword)
    private String applyOrOrderCode;

    /**
     * 该库存是用某辆AGVID车号执行的任务
     */
    @ExcelProperty(value = "orginAGVID")
    @Field(type = FieldType.Integer)
    private Integer orginAGVID;

    /**
     * 库存原库位，用户返回原库位使用
     */
    @ExcelProperty(value = "orginLocationCode")
    @Field(type = FieldType.Keyword)
    private String orginLocationCode;

    /**
     * 托盘类型,用于设备区分
     */
    @ExcelProperty(value = "palletType")
    @Field(type = FieldType.Keyword)
    private String palletType;

    /**
     * 体积
     */
    @ExcelProperty(value = "volume")
    @Field(type = FieldType.Keyword)
    private String volume;
    //endregion

    //region InventoryItem
    @ExcelProperty(value = "inventoryItemId")
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
    @ExcelProperty(value = "inventoryItemPackageQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal inventoryItemPackageQuantity;

    @ExcelProperty(value = "inventoryItemAllocatedPackageQuantity")
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

    @ExcelProperty(value = "inventoryItemIsLocked")
    @Field(type = FieldType.Boolean)
    private Boolean inventoryItemIsLocked;

    /**
     * 状态 （0正常，-1禁用）
     */
    @ExcelProperty(value = "inventoryItemXStatus")
    @Field(type = FieldType.Short)
    private Integer inventoryItemXStatus;
    @ExcelProperty(value = "inventoryItemIsExpired")
    @Field(type = FieldType.Boolean)
    private Boolean inventoryItemIsExpired;
    /**
     * 过期时间时间戳
     */
    @ExcelProperty(value = "inventoryItemExpiredTime")
    @Transient
//    @Field(name = "inventoryItemExpiredTime", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime inventoryItemExpiredTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "inventoryItemComments")
    @Field(type = FieldType.Text)
    private String inventoryItemComments;

    /**
     * 预留字段1
     */
    @ExcelProperty(value = "inventoryItemStr1")
    @Field(type = FieldType.Text)
    private String inventoryItemStr1;

    /**
     * 预留字段2
     */
    @ExcelProperty(value = "inventoryItemStr2")
    @Field(type = FieldType.Text)
    private String inventoryItemStr2;

    /**
     * 预留字段3
     */
    @ExcelProperty(value = "inventoryItemStr3")
    @Field(type = FieldType.Text)
    private String inventoryItemStr3;

    /**
     * 预留字段4
     */
    @ExcelProperty(value = "inventoryItemStr4")
    @Field(type = FieldType.Text)
    private String inventoryItemStr4;

    /**
     * 预留字段5
     */
    @ExcelProperty(value = "inventoryItemStr5")
    @Field(type = FieldType.Text)
    private String inventoryItemStr5;


    /**
     * 组织（客户）
     */
    @ExcelProperty(value = "organiztionId")
    @Field(type = FieldType.Long)
    private Long organiztionId;
    @ExcelProperty(value = "organiztionCode")
    @Field(type = FieldType.Keyword)
    private String organiztionCode;
    /**
     * 组织（供应商）
     */
    @ExcelProperty(value = "organiztionSupplierId")
    @Field(type = FieldType.Long)
    private Long organiztionSupplierId;
    @ExcelProperty(value = "organiztionSupplierCode")
    @Field(type = FieldType.Keyword)
    private String organiztionSupplierCode;
    //endregion

    //region InventoryItemDetail

    /**
     * #@Id es 默认 id  为keyword,es 会默认生成一个_id 字段值等于inventoryItemDetailId
     */
    @ExcelProperty(value = "inventoryItemDetailId")
    @Id
    @Field(type = FieldType.Long)
    private Long inventoryItemDetailId;

//脚本设置 为long
//    @Field(type = FieldType.Long)
//    private Long id;

    /**
     * 箱号
     */
    @ExcelProperty(value = "carton")
    @Field(type = FieldType.Keyword)
    private String carton;

    /**
     * 序列号
     */
    @ExcelProperty(value = "serialNo")
    @Field(type = FieldType.Keyword)
    private String serialNo;

    /**
     * 物料id
     */
    @ExcelProperty(value = "materialId")
    @Field(type = FieldType.Long)
    private Long materialId;
    @ExcelProperty(value = "materialCode")
    @Field(type = FieldType.Keyword)
    private String materialCode;
    /**
     * 批号
     */
    @ExcelProperty(value = "batchNo")
    @Field(type = FieldType.Keyword)
    private String batchNo;

    /**
     * 批号2
     */
    @ExcelProperty(value = "batchNo2")
    @Field(type = FieldType.Keyword)
    private String batchNo2;

    /**
     * 批号3
     */
    @ExcelProperty(value = "batchNo3")
    @Field(type = FieldType.Keyword)
    private String batchNo3;

    /**
     * 包装单位id
     */
    @ExcelProperty(value = "packageUnitId")
    @Field(type = FieldType.Long)
    private Long packageUnitId;
    @ExcelProperty(value = "packageUnitCode")
    @Field(type = FieldType.Keyword)
    private String packageUnitCode;
    /**
     * 最小单位数量
     */
    @ExcelProperty(value = "smallUnitQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal smallUnitQuantity;

    /**
     * 包装单位数量
     */
    @ExcelProperty(value = "packageQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal packageQuantity;

    /**
     * 已分配最小单位数量
     */
    @ExcelProperty(value = "allocatedSmallUnitQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal allocatedSmallUnitQuantity;

    /**
     * 已分配包装单位数量
     */
    @ExcelProperty(value = "allocatedPackageQuantity")
    @Field(type = FieldType.Double)
    private BigDecimal allocatedPackageQuantity;

    /**
     * 质检状态（0待检，1已取样，2合格，-1不合格）
     */
    @ExcelProperty(value = "qCStatus")
    @Field(type = FieldType.Short)
    private Integer qCStatus;

    /**
     * 状态 （0正常，-1禁用）
     */
    @ExcelProperty(value = "xStatus")
    @Field(type = FieldType.Short)
    private Integer xStatus;

    /**
     * 是否任务锁定
     */
    @ExcelProperty(value = "isLocked")
    @Field(type = FieldType.Boolean)
    private Boolean isLocked;

    /**
     * 是否封存
     */
    @ExcelProperty(value = "isSealed")
    @Field(type = FieldType.Boolean)
    private Boolean isSealed;

    /**
     * 是否零托，散货
     */
    @ExcelProperty(value = "isScattered")
    @Field(type = FieldType.Boolean)
    private Boolean isScattered;

    /**
     * 是否过期
     */
    @ExcelProperty(value = "isExpired")
    @Field(type = FieldType.Boolean)
    private Boolean isExpired;

    /**
     * 过期时间时间戳
     */
    @Transient
//    @Field(name = "expiredTime", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime expiredTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "comments")
    @Field(type = FieldType.Text)
    private String comments;

    /**
     * 物料扩展属性预留字段1
     */
    @ExcelProperty(value = "m_Str1")
    @Field(type = FieldType.Text)
    private String m_Str1;

    /**
     * 物料扩展属性预留字段2
     */
    @ExcelProperty(value = "m_Str2")
    @Field(type = FieldType.Text)
    private String m_Str2;

    /**
     * 物料扩展属性预留字段3
     */
    @ExcelProperty(value = "m_Str3")
    @Field(type = FieldType.Text)
    private String m_Str3;

    /**
     * 物料扩展属性预留字段4
     */
    @ExcelProperty(value = "m_Str4")
    @Field(type = FieldType.Text)
    private String m_Str4;

    /**
     * 物料扩展属性预留字段5
     */
    @ExcelProperty(value = "m_Str5")
    @Field(type = FieldType.Text)
    private String m_Str5;

    /**
     * 物料扩展属性预留字段6
     */
    @ExcelProperty(value = "m_Str6")
    @Field(type = FieldType.Text)
    private String m_Str6;

    /**
     * 物料扩展属性预留字段7
     */
    @ExcelProperty(value = "m_Str7")
    @Field(type = FieldType.Text)
    private String m_Str7;

    /**
     * 物料扩展属性预留字段8
     */
    @ExcelProperty(value = "m_Str8")
    @Field(type = FieldType.Text)
    private String m_Str8;

    /**
     * 物料扩展属性预留字段9
     */
    @ExcelProperty(value = "m_Str9")
    @Field(type = FieldType.Text)
    private String m_Str9;

    /**
     * 物料扩展属性预留字段10
     */
    @ExcelProperty(value = "m_Str10")
    @Field(type = FieldType.Text)
    private String m_Str10;

    /**
     * 物料扩展属性预留字段11
     */
    @ExcelProperty(value = "m_Str11")
    @Field(type = FieldType.Text)
    private String m_Str11;

    /**
     * 物料扩展属性预留字段12
     */
    @ExcelProperty(value = "m_Str12")
    @Field(type = FieldType.Text)
    private String m_Str12;

    /**
     * 物料扩展属性预留字段13
     */
    @ExcelProperty(value = "m_Str13")
    @Field(type = FieldType.Text)
    private String m_Str13;

    /**
     * 物料扩展属性预留字段14
     */
    @ExcelProperty(value = "m_Str14")
    @Field(type = FieldType.Text)
    private String m_Str14;

    /**
     * 物料扩展属性预留字段15
     */
    @ExcelProperty(value = "m_Str15")
    @Field(type = FieldType.Text)
    private String m_Str15;

    /**
     * 物料扩展属性预留字段16
     */
    @ExcelProperty(value = "m_Str16")
    @Field(type = FieldType.Text)
    private String m_Str16;

    /**
     * 物料扩展属性预留字段17
     */
    @ExcelProperty(value = "m_Str17")
    @Field(type = FieldType.Text)
    private String m_Str17;

    /**
     * 物料扩展属性预留字段18
     */
    @ExcelProperty(value = "m_Str18")
    @Field(type = FieldType.Text)
    private String m_Str18;

    /**
     * 物料扩展属性预留字段19
     */
    @ExcelProperty(value = "m_Str19")
    @Field(type = FieldType.Text)
    private String m_Str19;

    /**
     * 物料扩展属性预留字段20
     */
    @ExcelProperty(value = "m_Str20")
    @Field(type = FieldType.Text)
    private String m_Str20;

    /**
     * 物料扩展属性预留字段21
     */
    @ExcelProperty(value = "m_Str21")
    @Field(type = FieldType.Text)
    private String m_Str21;

    /**
     * 物料扩展属性预留字段22
     */
    @ExcelProperty(value = "m_Str22")
    @Field(type = FieldType.Text)
    private String m_Str22;

    /**
     * 物料扩展属性预留字段23
     */
    @ExcelProperty(value = "m_Str23")
    @Field(type = FieldType.Text)
    private String m_Str23;

    /**
     * 物料扩展属性预留字段24
     */
    @ExcelProperty(value = "m_Str24")
    @Field(type = FieldType.Text)
    private String m_Str24;

    /**
     * 物料扩展属性预留字段25
     */
    @ExcelProperty(value = "m_Str25")
    @Field(type = FieldType.Text)
    private String m_Str25;

    /**
     * 物料扩展属性预留字段26
     */
    @ExcelProperty(value = "m_Str26")
    @Field(type = FieldType.Text)
    private String m_Str26;

    /**
     * 物料扩展属性预留字段27
     */
    @ExcelProperty(value = "m_Str27")
    @Field(type = FieldType.Text)
    private String m_Str27;

    /**
     * 物料扩展属性预留字段28
     */
    @ExcelProperty(value = "m_Str28")
    @Field(type = FieldType.Text)
    private String m_Str28;

    /**
     * 物料扩展属性预留字段29
     */
    @ExcelProperty(value = "m_Str29")
    @Field(type = FieldType.Text)
    private String m_Str29;

    /**
     * 物料扩展属性预留字段30
     */
    @ExcelProperty(value = "m_Str30")
    @Field(type = FieldType.Text)
    private String m_Str30;

    /**
     * 物料扩展属性预留字段31
     */
    @ExcelProperty(value = "m_Str31")
    @Field(type = FieldType.Text)
    private String m_Str31;

    /**
     * 物料扩展属性预留字段32
     */
    @ExcelProperty(value = "m_Str32")
    @Field(type = FieldType.Text)
    private String m_Str32;

    /**
     * 物料扩展属性预留字段33
     */
    @ExcelProperty(value = "m_Str33")
    @Field(type = FieldType.Text)
    private String m_Str33;

    /**
     * 物料扩展属性预留字段34
     */
    @ExcelProperty(value = "m_Str34")
    @Field(type = FieldType.Text)
    private String m_Str34;

    /**
     * 物料扩展属性预留字段35
     */
    @ExcelProperty(value = "m_Str35")
    @Field(type = FieldType.Text)
    private String m_Str35;

    /**
     * 物料扩展属性预留字段36
     */
    @ExcelProperty(value = "m_Str36")
    @Field(type = FieldType.Text)
    private String m_Str36;

    /**
     * 物料扩展属性预留字段37
     */
    @ExcelProperty(value = "m_Str37")
    @Field(type = FieldType.Text)
    private String m_Str37;

    /**
     * 物料扩展属性预留字段38
     */
    @ExcelProperty(value = "m_Str38")
    @Field(type = FieldType.Text)
    private String m_Str38;

    /**
     * 物料扩展属性预留字段39
     */
    @ExcelProperty(value = "m_Str39")
    @Field(type = FieldType.Text)
    private String m_Str39;

    /**
     * 物料扩展属性预留字段40
     */
    @ExcelProperty(value = "m_Str40")
    @Field(type = FieldType.Text)
    private String m_Str40;

    /**
     * 创建人ID
     */
    @ExcelProperty(value = "creatorId")
    @Field(type = FieldType.Text)
    private Object creatorId;

    /**
     * 创建人名称
     */
    @ExcelProperty(value = "creatorName")
    private String creatorName;

    /**
     * 最新修改人ID
     */
    @ExcelProperty(value = "lastModifierId")
    @Field(type = FieldType.Text)
    private Object lastModifierId;

    /**
     * 最新修改人名称
     */
    @ExcelProperty(value = "lastModifierName")
    @Field(type = FieldType.Text)
    private String lastModifierName;

    /**
     * 创建时间戳13位
     */
    @ExcelProperty(value = "creationTime")
    @Field(name = "creationTime", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime creationTime;

    /**
     * 修改时间戳13位
     */
    @ExcelProperty(value = "lastModificationTime")
    @Field(name = "lastModificationTime", index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime lastModificationTime;

    /**
     * 入库时间
     */
    @ExcelProperty(value = "inboundTime")
    @Field(index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime inboundTime;

    /**
     * 生产时间
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'||yyyy-MM-dd HH:mm:ss.S||yyyy-MM-dd HH:mm:ss.SS||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
     */
    @ExcelProperty(value = "productTime")
    @Field(index = true, store = true, type = FieldType.Date, format = DateFormat.custom, pattern = pattern)
    private LocalDateTime productTime;

    /**
     * 货载在托盘上的码放工位号
     */
    @ExcelProperty(value = "positionLevel")
    @Field(type = FieldType.Keyword)
    private String positionCode;

    /**
     * 货载在托盘上的码放层级
     */
    @ExcelProperty(value = "positionLevel")
    @Field(type = FieldType.Short)
    private Integer positionLevel;

    /**
     * 包装方式
     */
    @ExcelProperty(value = "packageMethod")
    @Field(type = FieldType.Keyword)
    private String packageMethod;
    //endregion

    @ExcelProperty(value = "deleted")
    @Field(type = FieldType.Integer)
    private Integer deleted = 0; // 数值默认值
}
