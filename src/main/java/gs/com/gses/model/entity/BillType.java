package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName BillType
 */
@TableName(value ="BillType")
@Data
public class BillType implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 单据类型代码
     */
    @TableField(value = "XCode")
    private String XCode;

    /**
     * 单据类型名称
     */
    @TableField(value = "XName")
    private String XName;

    /**
     * 状态
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 所属仓库id
     */
    @TableField(value = "WarehouseId")
    private Long warehouseId;

    /**
     * 关联策略
     */
    @TableField(value = "BasicStrategyId")
    private Long basicStrategyId;

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
     * 类型:1收货 2发货 3移位
     */
    @TableField(value = "BillTypeType")
    private Integer billTypeType;

    /**
     * 排序
     */
    @TableField(value = "SortNum")
    private Integer sortNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}