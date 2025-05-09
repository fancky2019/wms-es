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
 * @TableName ShipPickOrderItem
 */
@TableName(value ="ShipPickOrderItem")
@Data
public class ShipPickOrderItem implements Serializable {
    /**
     * 
     */
    @TableId(value = "Id")
    private Long id;

    /**
     * 拣货主表单id
     */
    @TableField(value = "ShipPickOrderId")
    private Long shipPickOrderId;

    /**
     * 发货单明细表id（标识这个拣货明细对应的发货单明细行项）
     */
    @TableField(value = "ShipOrderItemId")
    private Long shipOrderItemId;

    /**
     * 计划拣货数量
     */
    @TableField(value = "PlanPkgQuantity")
    private BigDecimal planPkgQuantity;

    /**
     * 分配数量
     */
    @TableField(value = "AllocatedPkgQuantity")
    private BigDecimal allocatedPkgQuantity;

    /**
     * 已下架数量
     */
    @TableField(value = "MovedPkgQuantity")
    private BigDecimal movedPkgQuantity;

    /**
     * 起点位置ID
     */
    @TableField(value = "FromLocId")
    private Long fromLocId;

    /**
     * 起点位置编号
     */
    @TableField(value = "FromLocCode")
    private String fromLocCode;

    /**
     * 终点位置ID
     */
    @TableField(value = "ToLocId")
    private Long toLocId;

    /**
     * 终点位置编号
     */
    @TableField(value = "ToLocCode")
    private String toLocCode;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}