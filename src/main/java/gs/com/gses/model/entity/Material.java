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
 * @TableName Material
 */
@TableName(value ="Material")
@Data
public class Material implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 编码（编码去重）
     */
    @TableField(value = "XCode")
    private String XCode;

    /**
     * 名称
     */
    @TableField(value = "XName")
    private String XName;

    /**
     * 物料大类目录id
     */
    @TableField(value = "MaterialCategoryId")
    private Long materialCategoryId;

    /**
     * 简称
     */
    @TableField(value = "ShortName")
    private String shortName;

    /**
     * 条码值
     */
    @TableField(value = "Barcode")
    private String barcode;

    /**
     * 助记码
     */
    @TableField(value = "MnemonicCode")
    private String mnemonicCode;

    /**
     * 库存上限
     */
    @TableField(value = "Upper")
    private BigDecimal upper;

    /**
     * 库存下限
     */
    @TableField(value = "Lower")
    private BigDecimal lower;

    /**
     * 规格
     */
    @TableField(value = "Spec")
    private String spec;

    /**
     * 有效期天数
     */
    @TableField(value = "Days")
    private Integer days;

    /**
     * 最小单位
     */
    @TableField(value = "SmallestUnit")
    private String smallestUnit;

    /**
     * 备注
     */
    @TableField(value = "Comments")
    private String comments;

    /**
     * 是否禁用
     */
    @TableField(value = "IsForbidden")
    private Boolean isForbidden;

    /**
     * 禁用说明
     */
    @TableField(value = "ForbiddenComments")
    private String forbiddenComments;

    /**
     * 禁用人
     */
    @TableField(value = "ForbiddenUserId")
    private Object forbiddenUserId;

    /**
     * 预留字段1
     */
    @TableField(value = "Str1")
    private String str1;

    /**
     * 
     */
    @TableField(value = "Str2")
    private String str2;

    /**
     * 
     */
    @TableField(value = "Str3")
    private String str3;

    /**
     * 
     */
    @TableField(value = "Str4")
    private String str4;

    /**
     * 
     */
    @TableField(value = "Str5")
    private String str5;

    /**
     * 
     */
    @TableField(value = "Str6")
    private String str6;

    /**
     * 
     */
    @TableField(value = "Str7")
    private String str7;

    /**
     * 
     */
    @TableField(value = "Str8")
    private String str8;

    /**
     * 
     */
    @TableField(value = "Str9")
    private String str9;

    /**
     * 
     */
    @TableField(value = "Str10")
    private String str10;

    /**
     * 
     */
    @TableField(value = "Str11")
    private String str11;

    /**
     * 
     */
    @TableField(value = "Str12")
    private String str12;

    /**
     * 
     */
    @TableField(value = "Str13")
    private String str13;

    /**
     * 
     */
    @TableField(value = "Str14")
    private String str14;

    /**
     * 
     */
    @TableField(value = "Str15")
    private String str15;

    /**
     * 
     */
    @TableField(value = "Str16")
    private String str16;

    /**
     * 
     */
    @TableField(value = "Str17")
    private String str17;

    /**
     * 
     */
    @TableField(value = "Str18")
    private String str18;

    /**
     * 
     */
    @TableField(value = "Str19")
    private String str19;

    /**
     * 
     */
    @TableField(value = "Str20")
    private String str20;

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
     * ERP账号标记
     */
    @TableField(value = "ErpAccountSet")
    private String erpAccountSet;

    /**
     * 
     */
    @TableField(value = "DisplayCode")
    private String displayCode;

    /**
     * 
     */
    @TableField(value = "ErpUniqueCode")
    private String erpUniqueCode;

    /**
     * 
     */
    @TableField(value = "PicUrl")
    private String picUrl;

    /**
     * 
     */
    @TableField(value = "CloseToExpiredDays")
    private Integer closeToExpiredDays;

    /**
     * 
     */
    @TableField(value = "StorageWarningDays")
    private Integer storageWarningDays;

    /**
     * 入库层级，用于特殊需求指定物料在立库的层级
     */
    @TableField(value = "InboundLevel")
    private Integer inboundLevel;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}