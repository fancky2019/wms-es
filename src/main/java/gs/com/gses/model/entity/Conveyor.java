package gs.com.gses.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName Conveyor
 */
@TableName(value ="Conveyor")
@Data
public class Conveyor {
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
     * 该入库口入库时的货架列优先排序，默认从小到大，1从大到小
     */
    @TableField(value = "PutawayColoumSoring")
    private Integer putawayColoumSoring;

    /**
     * 该入库口入库时的货架列优先还是层优先
     */
    @TableField(value = "PutawayColoumFirstThenLevel")
    private Integer putawayColoumFirstThenLevel;

    /**
     * 该入库口入库时的货架排优先排序，默认从小到大，1从大到小
     */
    @TableField(value = "PutawayRackSoring")
    private Integer putawayRackSoring;

    /**
     * 该入库口入库时的货架列范围集合如：1~5;10~20;40~65格式
     */
    @TableField(value = "PutawayColumnRange")
    private String putawayColumnRange;

    /**
     * 该入库口入库时的货架层优先排序，默认从低到高，1从高到低
     */
    @TableField(value = "PutawayLevelSorting")
    private Integer putawayLevelSorting;

    /**
     * 该入库口入库时的货架排范围集合如：1~5;10~20;40~65格式
     */
    @TableField(value = "PutawayRackRange")
    private String putawayRackRange;

    /**
     * 可达性类型 0全部 1仅入库 2仅出库
     */
    @TableField(value = "ConveyorLanewayType")
    private Integer conveyorLanewayType;

    /**
     * 该入库口入库时巷道的优先级,默认平均分配，格式3,2,1
     */
    @TableField(value = "PutawayLanewayOrder")
    private String putawayLanewayOrder;

    /**
     * 入库是否变轨策略
     */
    @TableField(value = "PutawayIsTrajectoryChange")
    private Boolean putawayIsTrajectoryChange;

    /**
     * 是否自动补充空托盘
     */
    @TableField(value = "IsAutoSupplyEmptyPallet")
    private Boolean isAutoSupplyEmptyPallet;
}