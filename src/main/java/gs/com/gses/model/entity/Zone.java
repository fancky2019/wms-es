package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName Zone
 */
@TableName(value ="Zone")
@Data
public class Zone implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 代码
     */
    @TableField(value = "XCode")
    private String XCode;

    /**
     * 名称
     */
    @TableField(value = "XName")
    private String XName;

    /**
     * 状态（DISABLED 0禁用 ENABLED 1正常，Exception2异常，NOEXIST -1不存在）
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 所属仓库ID
     */
    @TableField(value = "WarehouseId")
    private Long warehouseId;

    /**
     * 
     */
    @TableField(value = "InIsAuto")
    private Boolean inIsAuto;

    /**
     * 
     */
    @TableField(value = "OutIsAuto")
    private Boolean outIsAuto;

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
     * 库区类型(1立库，2平库，3地堆，4虚拟)
     */
    @TableField(value = "XType")
    private Integer XType;

    /**
     * 
     */
    @TableField(value = "ForbidInLevels")
    private String forbidInLevels;

    /**
     * 
     */
    @TableField(value = "ForbidOutLevels")
    private String forbidOutLevels;

    /**
     * 
     */
    @TableField(value = "InIsAutoComplete")
    private Boolean inIsAutoComplete;

    /**
     * 
     */
    @TableField(value = "OutIsAutoComplete")
    private Boolean outIsAutoComplete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}