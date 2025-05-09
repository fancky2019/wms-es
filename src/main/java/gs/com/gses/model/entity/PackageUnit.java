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
 * @TableName PackageUnit
 */
@TableName(value ="PackageUnit")
@Data
public class PackageUnit implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 物料Id
     */
    @TableField(value = "MaterialId")
    private Long materialId;

    /**
     * 单位名称
     */
    @TableField(value = "Unit")
    private String unit;

    /**
     * 
     */
    @TableField(value = "ConvertFigureSmallUnit")
    private BigDecimal convertFigureSmallUnit;

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
     * 重量
     */
    @TableField(value = "Weight")
    private BigDecimal weight;

    /**
     * 体积
     */
    @TableField(value = "Volume")
    private BigDecimal volume;

    /**
     * 满托数量
     */
    @TableField(value = "FullPalletQuantity")
    private BigDecimal fullPalletQuantity;

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
    @TableField(value = "IsMainUnit")
    private Boolean isMainUnit;

    /**
     * 包装方式
     */
    @TableField(value = "PackageMethod")
    private String packageMethod;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}