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
 * @TableName Laneway
 */
@TableName(value ="Laneway")
@Data
public class Laneway implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 巷道代码
     */
    @TableField(value = "XCode")
    private Integer XCode;

    /**
     * 巷道名称
     */
    @TableField(value = "XName")
    private String XName;

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
     * 禁止wcs上报改变状态（默认允许）
     */
    @TableField(value = "DisallowWcsToClearError")
    private Boolean disallowWcsToClearError;

    /**
     * 巷道已使用货位数
     */
    @TableField(value = "LoadLocationCount")
    private Integer loadLocationCount;

    /**
     * 关联设备编号（如受某堆垛机影响）
     */
    @TableField(value = "RelateDeviceCode")
    private String relateDeviceCode;

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
     * 巷道货位总数
     */
    @TableField(value = "TotalLocationCount")
    private Integer totalLocationCount;

    /**
     * 巷道使用率
     */
    @TableField(value = "UsageRate")
    private BigDecimal usageRate;

    /**
     * 巷道使用率上限
     */
    @TableField(value = "UsageRateUpper")
    private BigDecimal usageRateUpper;

    /**
     * 类型（1单申单货位，2双申单货位，3单申双叉双货位，4双申双叉双货位，5多申位地推）
     */
    @TableField(value = "XType")
    private Integer XType;

    /**
     * 状态（DISABLED 0禁用 ENABLED 1正常，Exception2异常，NOEXIST -1不存在）
     */
    @TableField(value = "XStatus")
    private Integer XStatus;

    /**
     * 所属库区ID
     */
    @TableField(value = "ZoneId")
    private Long zoneId;

    /**
     * 入库最大缓存数
     */
    @TableField(value = "InMaxCacheCount")
    private Integer inMaxCacheCount;

    /**
     * 出库最大缓存数
     */
    @TableField(value = "OutMaxCacheCount")
    private Integer outMaxCacheCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}